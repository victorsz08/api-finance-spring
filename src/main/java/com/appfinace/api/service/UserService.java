package com.appfinace.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.appfinace.api.domain.user.User;
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
}
