package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InstallmentPurchaseRequestDto(
        String description,
        BigDecimal totalAmount,
        Integer totalInstallments,
        LocalDate purchaseDate,
        UUID categoryId) {
}
