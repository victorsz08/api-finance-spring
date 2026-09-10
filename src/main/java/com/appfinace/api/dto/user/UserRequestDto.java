package com.appfinace.api.dto.user;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequestDto(
        @NotBlank @Email String email,
        @NotBlank String name,
        MultipartFile profileImage,
        @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "A senha deve conter no mínimo 8 caracteres, letras maiúsculas, letras minusculas, números e catacteres especiais") String password) {
}
