package com.appfinace.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.dto.category.CategoryResponseDto;

public record TransactionResponseDto(UUID id, String description, BigDecimal amount, TransactionType type,
        LocalDate date,
        CategoryResponseDto category) {

}
