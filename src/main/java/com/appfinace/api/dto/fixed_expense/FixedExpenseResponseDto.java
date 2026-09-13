package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

import com.appfinace.api.dto.category.CategoryResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Despesa fixa - Resposta")
public record FixedExpenseResponseDto(
        @Schema(description = "Identificador único da despesa fixa") UUID id,
        @Schema(description = "Descrição da despesa fixa") String description,
        @Schema(description = "Valor da despesa") BigDecimal amount,
        @Schema(description = "Dia do mês em que a despesa vence (1 a 31)") Integer dueDay,
        @Schema(description = "Infomativo se a despesa está ativa") Boolean active,
        @Schema(description = "Categoria vinculada a despesa fixa", implementation = CategoryResponseDto.class) CategoryResponseDto category) {
}
