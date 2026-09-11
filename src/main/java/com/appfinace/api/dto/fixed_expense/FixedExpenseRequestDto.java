package com.appfinace.api.dto.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FixedExpenseRequestDto(
                @NotBlank String description,
                @NotNull @Positive BigDecimal amount,
                @NotNull @Min(1) @Max(31) Integer dueDay,
                @NotNull UUID categoryId) {
}
