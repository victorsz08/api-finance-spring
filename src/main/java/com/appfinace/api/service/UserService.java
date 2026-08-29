package com.appfinace.api.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.user.ProfileImages;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.user.FindUserResponseDto;
import com.appfinace.api.dto.user.ProfileImagesResponseDto;
import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.infra.S3StoragePort;
import com.appfinace.api.repositories.ProfileImagesRepository;
import com.appfinace.api.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final S3StoragePort s3StoragePort;

    private final PasswordEncoder passwordEncoder;

    private final ProfileImagesRepository profileImagesRepository;

    public UserService(
            UserRepository userRepository,
            S3StoragePort s3StoragePort,
            PasswordEncoder passwordEncoder,
            ProfileImagesRepository profileImagesRepository) {
        this.userRepository = userRepository;
        this.s3StoragePort = s3StoragePort;
        this.passwordEncoder = passwordEncoder;
        this.profileImagesRepository = profileImagesRepository;
    }

    public void createUser(UserRequestDto data) {
        String currentProfileImage = null;

        if (this.userRepository.existsByEmail(data.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        if (data.profileImage() != null) {
            currentProfileImage = s3StoragePort.uploadImage(data.profileImage());
        }
        String passwordHashed = this.passwordEncoder.encode(data.password());

        User aUser = new User();
        ProfileImages profileImages = new ProfileImages();

        aUser.setEmail(data.email());
        aUser.setName(data.name());
        aUser.setCurrentProfileImgUrl(currentProfileImage);
        aUser.setPassword(passwordHashed);

        this.userRepository.save(aUser);

        if (currentProfileImage != null) {
            profileImages.setProfileImageUrl(currentProfileImage);
            profileImages.setUser(aUser);

            this.profileImagesRepository.save(profileImages);
        }
    }

    public FindUserResponseDto findUser(UUID id) {
        Optional<User> optionalUser = this.userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        User user = optionalUser.get();

        return new FindUserResponseDto(user.getId(), user.getEmail(), user.getName(), user.getCurrentProfileImgUrl());
    };

    public List<FindUserResponseDto> listUsers(int page, int size, String email, String name) {
        name = (name != null) ? name : "";
        email = (email != null) ? email : "";

        Pageable pageable = PageRequest.of(page, size);

        Page<User> usersFiltred = this.userRepository.getFiltredUsers(name, email, pageable);

        return usersFiltred.map(user -> new FindUserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCurrentProfileImgUrl())).stream().toList();
    }

    public void updateUser(UUID id, String name, String email, MultipartFile profileImage) {
        Optional<User> optionalUser = this.userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        User user = optionalUser.get();

        if (email != null && !email.equals(user.getEmail()) && this.userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        ProfileImages profileImages = new ProfileImages();

        if (profileImage != null) {
            String newProfileImageUrl = this.s3StoragePort.uploadImage(profileImage);

            profileImages.setProfileImageUrl(newProfileImageUrl);
            profileImages.setUser(user);

            this.profileImagesRepository.save(profileImages);
            user.setCurrentProfileImgUrl(newProfileImageUrl);
        }

        user.setId(id);
        user.setName(name);
        user.setEmail(email);

        this.userRepository.save(user);
    }

    public void updatePassword(UUID id, String currentPassword, String newPassword) {
        Optional<User> optionalUser = this.userRepository.findById(id);

        if (optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        User user = optionalUser.get();

        if (!this.passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
        }

        String newPasswordHashed = this.passwordEncoder.encode(newPassword);

        user.setPassword(newPasswordHashed);

        this.userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        if (!this.userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        this.userRepository.deleteById(id);
    }

    public List<ProfileImagesResponseDto> getProfileImagesByUser(UUID id) {
        List<ProfileImages> profileImages = this.profileImagesRepository.findByUserId(id);

        return profileImages.stream()
                .map(image -> new ProfileImagesResponseDto(
                        image.getId(),
                        image.getProfileImageUrl()))
                .toList();
    }
}
