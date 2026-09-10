package com.appfinace.api.dto.user;

import java.util.UUID;

public record UserResponseDto(UUID id, String email, String name, String currentProfileImgUrl) {

}
