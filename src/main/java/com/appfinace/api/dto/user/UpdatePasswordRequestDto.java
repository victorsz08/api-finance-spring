package com.appfinace.api.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "Dados de atualização da senha do usuário")
public record UpdatePasswordRequestDto(
                @Schema(description = "Senha atual") @NotBlank String currentPassword,
                @Schema(description = "Nova senha - Deve conter no mínimo 8 caracteres, letras maiúscula, minúscula, números e carateres especiais") @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "A nova senha deve conter no mínimo 8 caracteres, letras maiúsculas, letras minúsculas, números e caracteres especiais") String newPassword) {
}
