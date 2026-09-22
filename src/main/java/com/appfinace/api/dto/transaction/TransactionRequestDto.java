package com.appfinace.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.transaction.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "Transações - Requisição")
public record TransactionRequestDto(
                @Schema(description = "Descrição da transação", example = "Jantar") @NotBlank String description,
                @Schema(description = "Valor da transação", example = "150.00") @NotNull @Positive BigDecimal amount,
                @Schema(description = "Tipo da transação [EXPENSE | INCOME]") @NotNull TransactionType type,
                @Schema(description = "Data da transação") @NotNull LocalDate date,
                @Schema(description = "Identificador da categoria vinculada") @NotNull UUID categoryId) {

}
