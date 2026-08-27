package com.appfinace.api.infra.exception;

import java.time.Instant;

public record ExceptionResponseDto(
    Instant timestamp,
    int statusCode,
    String message
) {
    public ExceptionResponseDto(int statusCode, String message) {
        this(Instant.now(), statusCode, message);
    }
}
