package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record InstallmentPurchaseRequestDto(
                @NotBlank String description,
                @NotBlank BigDecimal totalAmount,
                @NotBlank @Positive Integer totalInstallments,
                @NotBlank LocalDate purchaseDate,
                @NotBlank UUID categoryId) {
}
