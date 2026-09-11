package com.appfinace.api.dto.user;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserRequestDto(
        @NotBlank @Email String email,
        @NotBlank String name,
        MultipartFile profileImage) {
}