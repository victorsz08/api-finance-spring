package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record FixedExpenseRequestDto(
        @NotBlank String description,
        @NotBlank @Positive BigDecimal amount,
        @NotBlank @Positive @Min(1) Integer dueDay,
        @NotBlank UUID categoryId) {
}
