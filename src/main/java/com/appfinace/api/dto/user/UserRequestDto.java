package com.appfinace.api.dto.user;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(name = "Dados de criação do usuário", description = "Dados para criação de um novo usuário")
public record UserRequestDto(
                @Schema(description = "Email do usuário", example = "joao@email.com") @NotBlank @Email String email,
                @Schema(description = "Nome completo", example = "Joao da Silva") @NotBlank String name,
                @Schema(description = "Imagem do perfil (opcional)") MultipartFile profileImage,
                @Schema(description = "Senha - Deve conter no mínimo 8 caracteres, letras maiúscula, minúscula, números e carateres especiais") @NotBlank @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$", message = "A senha deve conter no mínimo 8 caracteres, letras maiúsculas, letras minusculas, números e catacteres especiais") String password) {
}
