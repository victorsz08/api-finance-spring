package com.appfinace.api.dto.fixed_expense;

import java.time.LocalDate;
import java.util.UUID;

public record FixedExpensePaymentResponseDto(UUID id, int month, int year, LocalDate paidAt) {
}
