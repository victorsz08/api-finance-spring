package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;

public record FixedExpenseRequestDto(String description, BigDecimal amount, Integer dueDay) {
}
