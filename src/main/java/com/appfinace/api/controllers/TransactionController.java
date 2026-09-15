package com.appfinace.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.transaction.MonthlySummaryResponseDto;
import com.appfinace.api.dto.transaction.TransactionRequestDto;
import com.appfinace.api.dto.transaction.TransactionResponseDto;
import com.appfinace.api.infra.exception.ExceptionResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transações", description = "Endpoits de gerenciamento de transações")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Criar transação", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Categoria não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody TransactionRequestDto data) {
        UUID userId = user.getUser().getId();
        this.transactionService.create(data, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar transações filtradas", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> listByMonth(
            @Parameter(description = "Filtro parcial por mês", example = "9") @RequestParam Integer month,
            @Parameter(description = "Filtro parcial por ano", example = "2026") @RequestParam Integer year,
            @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        List<TransactionResponseDto> transactions = this.transactionService.listByMonth(month, year, userId);

        return ResponseEntity.ok(transactions);
    }

    @Operation(summary = "Buscar resumo mensal", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponseDto> getMonthlySummary(
            @Parameter(description = "Filtro por mês", example = "9") @RequestParam(required = true) Integer month,
            @Parameter(description = "Filtro por ano", example = "2026") @RequestParam(required = true) Integer year,
            @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        MonthlySummaryResponseDto summary = this.transactionService.getMonthlySummary(month, year, userId);

        return ResponseEntity.ok(summary);
    }
}
