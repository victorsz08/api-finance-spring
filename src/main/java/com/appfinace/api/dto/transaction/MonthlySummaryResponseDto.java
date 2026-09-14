package com.appfinace.api.dto.transaction;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Transações - Resumo mensal")
public record MonthlySummaryResponseDto(
        @Schema(description = "Mês do resumo") int month,
        @Schema(description = "Ano do resumo") int year,
        @Schema(description = "Renda total") BigDecimal totalIncome,
        @Schema(description = "Despesa total") BigDecimal totalExpense,
        @Schema(description = "Saldo restante") BigDecimal balance) {
}
