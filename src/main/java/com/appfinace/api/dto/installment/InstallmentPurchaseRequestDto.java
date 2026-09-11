package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InstallmentPurchaseRequestDto(
        @NotBlank String description,
        @NotNull @Positive BigDecimal totalAmount,
        @NotNull @Min(1) Integer totalInstallments,
        @NotNull LocalDate purchaseDate,
        @NotNull UUID categoryId) {
}
