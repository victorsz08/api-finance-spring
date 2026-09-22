package com.appfinace.api.dto.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.installment.InstallmentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Compra parcelada - Parcela")
public record InstallmentResponseDto(
                @Schema(description = "Identificador único da parcela") UUID id,
                @Schema(description = "Número da parcela") Integer number,
                @Schema(description = "Valor da parcela") BigDecimal amount,
                @Schema(description = "Data de vencimento da parcela") LocalDate dueDate,
                @Schema(description = "Status da parcela - PAID|EXPENSE", example = "PAID (PAGA)") InstallmentStatus status) {
}
