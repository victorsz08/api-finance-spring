package com.appfinace.api.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.dto.category.CategoryResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Transação - Resposta")
public record TransactionResponseDto(
                @Schema(description = "Identificador único da transação") UUID id,
                @Schema(description = "Descrição da transação") String description,
                @Schema(description = "Valor da transação") BigDecimal amount,
                @Schema(description = "Tipo da transação - EXPENSE | INCOME") TransactionType type,
                @Schema(description = "Data da transação") LocalDate date,
                @Schema(description = "Categoria vinculada a transação", implementation = CategoryResponseDto.class) CategoryResponseDto category) {

}
