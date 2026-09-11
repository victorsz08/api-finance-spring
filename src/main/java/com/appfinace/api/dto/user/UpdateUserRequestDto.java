package com.appfinace.api.dto.user;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "Dados de atualização do usuário")
public record UpdateUserRequestDto(
                @Schema(description = "Email do usuário") @NotBlank @Email String email,
                @Schema(description = "Nome completo") @NotBlank String name,
                @Schema(description = "Imagem do perfil (Opcional)") MultipartFile profileImage) {
}