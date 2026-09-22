package com.appfinace.api.dto.fixed_expense;

import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Despesa Fixa - Pagemento")
public record FixedExpensePaymentResponseDto(
        @Schema(description = "Identificador único da despesa fixa") UUID id,
        @Schema(description = "Mês do pagamento", example = "3") int month,
        @Schema(description = "Ano do pagamento", example = "2026") int year,
        @Schema(description = "Data que o pagamento foi registrado") LocalDate paidAt) {
}
