package com.appfinace.api.infra.exception;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Resposta de Erro", description = "Informações sobre o erro.")
public record ExceptionResponseDto(
        @Schema(description = "Data e hora") Instant timestamp,
        @Schema(description = "Codigo do status") int statusCode,
        @Schema(description = "Mensagem de erro") String message) {
    public ExceptionResponseDto(int statusCode, String message) {
        this(Instant.now(), statusCode, message);
    }
}
