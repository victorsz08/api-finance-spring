package com.appfinace.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.transaction.TransactionType;

public record TransactionRequestDto(String description, BigDecimal amount, TransactionType type, LocalDate date,
        UUID categoryId) {

}
