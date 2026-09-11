package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

import com.appfinace.api.dto.category.CategoryResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Dados da despesa fixa")
public record FixedExpenseResponseDto(
                @Schema(description = "ID único") UUID id,
                @Schema(description = "Descrição da despesa") String description,
                @Schema(description = "Valor da despesa") BigDecimal amount,
                @Schema(description = "Dia de pagamento da despesa") Integer dueDay,
                @Schema(description = "Infomativo se a despesa está ativa") Boolean active,
                @Schema(description = "Categoria da despesa", implementation = CategoryResponseDto.class) CategoryResponseDto category) {
}
