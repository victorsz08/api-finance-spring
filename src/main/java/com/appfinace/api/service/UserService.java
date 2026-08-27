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

import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.user.FindUserResponseDto;
import com.appfinace.api.dto.user.UserRequestDto;
import com.appfinace.api.infra.S3StoragePort;
import com.appfinace.api.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final S3StoragePort s3StoragePort;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, S3StoragePort s3StoragePort, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.s3StoragePort = s3StoragePort;
        this.passwordEncoder = passwordEncoder;
    }



    public void createUser(UserRequestDto data) {
        String profileImg = null;

        if(this.userRepository.existByEmail(data.email())) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if(data.profileImage() != null) {
            profileImg = s3StoragePort.uploadImage(data.profileImage());
        }
        String passwordHashed = this.passwordEncoder.encode(data.password());

        User aUser = new User();

        aUser.setEmail(data.email());
        aUser.setName(data.name());
        aUser.setProfileImageUrl(profileImg);
        aUser.setPassword(passwordHashed);

        this.userRepository.save(aUser);
    }

    public FindUserResponseDto findUser(UUID id) {
        Optional<User> optionalUser = this.userRepository.findById(id);

        if(optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        User user = optionalUser.get();

        return new FindUserResponseDto(user.getId(), user.getEmail(), user.getName(), user.getProfileImageUrl());
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
            user.getProfileImageUrl()
        )).stream().toList();
    }

    public void updateUser(UUID id, String name, String email, MultipartFile profileImage) {
        Optional<User> optionalUser = this.userRepository.findById(id);

        if(optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        User user = optionalUser.get();

        if(!email.equals(user.getEmail()) && this.userRepository.existByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if(profileImage != null) {
            String profileImageUrl = this.s3StoragePort.uploadImage(profileImage);
            user.setProfileImageUrl(profileImageUrl);
        }

        user.setName(name);
        user.setEmail(email);

        this.userRepository.save(user);
    }

    public void updatePassword(UUID id, String currentPassword, String newPassword) {
        Optional<User> optionalUser = this.userRepository.findById(id);

        if(optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        User user = optionalUser.get();

        if(!this.passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha atual incorreta");
        }

        String newPasswordHashed = this.passwordEncoder.encode(newPassword);

        user.setPassword(newPasswordHashed);

        this.userRepository.save(user);
    }

    public void deleteUser(UUID id) {
        if(!this.userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }

        this.userRepository.deleteById(id);
    }
}
