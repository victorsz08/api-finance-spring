package com.appfinace.api.infra.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ExceptionResponseDto> handleResponseException(ResponseStatusException ex) {
        ExceptionResponseDto body = new ExceptionResponseDto(
                ex.getStatusCode().value(),
                ex.getReason());

        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ExceptionResponseDto> handleMissingParams(MissingServletRequestParameterException ex) {
        ExceptionResponseDto body = new ExceptionResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro obrigatório ausente: " + ex.getParameterName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleGenericException(Exception ex) {
        ExceptionResponseDto body = new ExceptionResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno no servidor");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ":" + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ExceptionResponseDto body = new ExceptionResponseDto(HttpStatus.BAD_REQUEST.value(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
