package com.appfinace.api.dto.user;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Dados de resposta de imagens de perfil do usuário")
public record ProfileImagesResponseDto(
        @Schema(description = "Id unico da imagem") UUID id,
        @Schema(description = "URL da imagem do usuário") String profileImageUrl) {

}
