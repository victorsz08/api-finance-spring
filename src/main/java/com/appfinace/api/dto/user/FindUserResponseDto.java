package com.appfinace.api.dto.user;

import java.util.UUID;

public record FindUserResponseDto(UUID id, String email, String name, String profileImageUrl) {

}
