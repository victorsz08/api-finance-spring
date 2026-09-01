package com.appfinace.api.dto.transaction;

import java.math.BigDecimal;

public record MonthlySummaryResponseDto(int month, int year, BigDecimal totalIncome, BigDecimal totalExpense,
                BigDecimal balance) {
}
