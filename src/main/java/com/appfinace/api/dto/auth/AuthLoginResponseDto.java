package com.appfinace.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Autenticação - Login Resposta")
public record AuthLoginResponseDto(
        @Schema(description = "JWT token de acesso") String token) {
}
