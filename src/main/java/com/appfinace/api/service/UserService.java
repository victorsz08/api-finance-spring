package com.appfinace.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.user.ProfileImages;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.user.UserResponseDto;
import com.appfinace.api.dto.user.ProfileImagesResponseDto;
import com.appfinace.api.dto.user.UpdatePasswordRequestDto;
import com.appfinace.api.dto.user.UpdateUserRequestDto;
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

    public UserResponseDto findUser(UUID id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        return new UserResponseDto(user.getId(), user.getEmail(), user.getName(), user.getCurrentProfileImgUrl());
    };

    public List<UserResponseDto> listUsers(int page, int size, String email, String name) {
        Pageable pageable = PageRequest.of(page, size);

        Page<User> usersFiltred = this.userRepository.getFiltredUsers(name, email, pageable);

        return usersFiltred.map(user -> new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getCurrentProfileImgUrl())).stream().toList();
    }

    public void updateUser(UUID id, UpdateUserRequestDto data) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (data.email() != null && !data.email().equals(user.getEmail())
                && this.userRepository.existsByEmail(data.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }

        ProfileImages profileImages = new ProfileImages();

        if (data.profileImage() != null) {
            String newProfileImageUrl = this.s3StoragePort.uploadImage(data.profileImage());

            profileImages.setProfileImageUrl(newProfileImageUrl);
            profileImages.setUser(user);

            this.profileImagesRepository.save(profileImages);
            user.setCurrentProfileImgUrl(newProfileImageUrl);
        }

        user.setId(id);
        user.setName(data.name());
        user.setEmail(data.email());

        this.userRepository.save(user);
    }

    public void updatePassword(UUID id, UpdatePasswordRequestDto data) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!this.passwordEncoder.matches(data.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
        }

        String newPasswordHashed = this.passwordEncoder.encode(data.newPassword());

        user.setPassword(newPasswordHashed);

        this.userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        userRepository.delete(user);
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
