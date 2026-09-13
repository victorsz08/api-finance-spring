package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "Compras parcelada - Requisição")
public record InstallmentPurchaseRequestDto(
                @Schema(description = "Descrição da compra parcelada", example = "Notebook") @NotBlank String description,
                @Schema(description = "Valor total da compra", example = "3000.00") @NotNull @Positive BigDecimal totalAmount,
                @Schema(description = "Total de parcelas", example = "10") @NotNull @Min(1) Integer totalInstallments,
                @Schema(description = "Data da compra") @NotNull LocalDate purchaseDate,
                @Schema(description = "Identificador único da categoria vinculada") @NotNull UUID categoryId) {
}
