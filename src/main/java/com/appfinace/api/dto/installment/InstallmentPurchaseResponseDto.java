package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.appfinace.api.dto.category.CategoryResponseDto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Compra parcelada - Resposta")
public record InstallmentPurchaseResponseDto(
                @Schema(description = "Identificador único da compra parcelada") UUID id,
                @Schema(description = "Descrição da compra parcelada") String description,
                @Schema(description = "Valor total da compra") BigDecimal totalAmount,
                @Schema(description = "Total de parcelas") Integer totalInstallments,
                @Schema(description = "Data da compra") LocalDate purcharseDate,
                @Schema(description = "Categoria vinculada a compra", implementation = CategoryResponseDto.class) CategoryResponseDto category,
                @Schema(description = "Lista de parcelas geradas da compra", implementation = InstallmentResponseDto.class) List<InstallmentResponseDto> installments) {
}
