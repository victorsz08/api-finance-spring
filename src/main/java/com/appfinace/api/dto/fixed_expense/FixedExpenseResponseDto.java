package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

import com.appfinace.api.dto.category.CategoryResponseDto;

public record FixedExpenseResponseDto(UUID id, String description, BigDecimal amount, Integer dueDay, Boolean active,
        CategoryResponseDto category) {
}
