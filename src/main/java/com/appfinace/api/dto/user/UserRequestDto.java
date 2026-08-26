package com.appfinace.api.dto.user;

import org.springframework.web.multipart.MultipartFile;

public record UserRequestDto(String email, String firstName, String lastName, MultipartFile profileImage, String password) {
}
