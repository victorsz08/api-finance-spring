package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

public record FixedExpenseRequestDto(String description, BigDecimal amount, Integer dueDay, UUID categoryId) {
}
