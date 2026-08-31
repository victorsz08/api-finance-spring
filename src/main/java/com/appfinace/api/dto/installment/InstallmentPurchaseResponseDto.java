package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.appfinace.api.dto.category.CategoryResponseDto;

public record InstallmentPurchaseResponseDto(
        UUID id,
        String description,
        BigDecimal totalAmount,
        Integer totalInstallments,
        LocalDate purcharseDate,
        CategoryResponseDto category,
        List<InstallmentResponseDto> installments) {
}
