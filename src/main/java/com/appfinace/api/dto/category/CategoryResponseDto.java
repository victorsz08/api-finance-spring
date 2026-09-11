package com.appfinace.api.dto.category;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Dados de resposta da categoria")
public record CategoryResponseDto(
        @Schema(description = "ID unico") UUID id,
        @Schema(description = "Nome da categoria") String name,
        @Schema(description = "Tipo da categoria") String type) {

}
