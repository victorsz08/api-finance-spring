package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.installment.InstallmentStatus;

public record InstallmentResponseDto(
        UUID id,
        Integer number,
        BigDecimal amount,
        LocalDate dueDate,
        InstallmentStatus status) {
}
