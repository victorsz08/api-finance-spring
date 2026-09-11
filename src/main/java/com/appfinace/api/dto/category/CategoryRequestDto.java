package com.appfinace.api.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "Dados de criação de categoria")
public record CategoryRequestDto(
                @Schema(description = "Nome da categoria", example = "Moradia") @NotBlank String name,
                @Schema(description = "Tipo da categoria - EXPENSE (Despesa) ou INCOME (Entrada)") @NotBlank @Pattern(regexp = "EXPENSE|INCOME", message = "O tipo deve ser Despesa ou Entrada") String type) {

}
