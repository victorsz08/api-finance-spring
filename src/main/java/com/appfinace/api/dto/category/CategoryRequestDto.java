package com.appfinace.api.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryRequestDto(
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "EXPENSE|INCOME", message = "O tipo deve ser Despesa ou Entrada") String type) {

}
