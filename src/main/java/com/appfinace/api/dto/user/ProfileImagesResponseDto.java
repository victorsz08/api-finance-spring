package com.appfinace.api.dto.user;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Usuário - Imagens de perfil - Resposta")
public record ProfileImagesResponseDto(
                @Schema(description = "Id unico da imagem") UUID id,
                @Schema(description = "URL da imagem do usuário") String profileImageUrl) {

}
