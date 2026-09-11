package com.appfinace.api.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "Dados de Login")
public record AuthLoginRequestDto(
                @Schema(description = "Email do usuário") @NotBlank(message = "Email é obrigatório") @Email String email,
                @Schema(description = "Senha do usuário") @NotBlank(message = "Senha é obrigatória") String password) {
}
