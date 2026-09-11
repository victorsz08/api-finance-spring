package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "Criação de despesa fixa")
public record FixedExpenseRequestDto(
        @Schema(description = "Descrição da despesa", example = "Aluguel") @NotBlank String description,
        @Schema(description = "Valor da despesa", example = "500.00") @NotNull @Positive BigDecimal amount,
        @Schema(description = "Dia de pagamento da despesa", example = "10") @NotNull @Min(1) @Max(31) Integer dueDay,
        @Schema(description = "ID de uma categoria") @NotNull UUID categoryId) {
}
