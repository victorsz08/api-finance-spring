package com.appfinace.api.dto.user;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Dados de resposta do usuário")
public record UserResponseDto(
        @Schema(description = "ID único do usuário") UUID id,
        @Schema(description = "Email do usuário") String email,
        @Schema(description = "Nome completo do usuário") String name,
        @Schema(description = "Url da imagem de perfil do usuário") String currentProfileImgUrl) {

}
