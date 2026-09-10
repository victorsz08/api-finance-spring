package com.appfinace.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.transaction.TransactionType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record TransactionRequestDto(
                @NotBlank String description,
                @NotBlank @Positive BigDecimal amount,
                @NotBlank @Pattern(regexp = "EXPENSE|INCOME", message = "O tipo deve ser Despesa ou Entrada") TransactionType type,
                @NotBlank LocalDate date,
                @NotBlank UUID categoryId) {

}
