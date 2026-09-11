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
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody TransactionRequestDto data) {
        UUID userId = user.getUser().getId();
        this.transactionService.create(data, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> listByMonth(@RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        List<TransactionResponseDto> transactions = this.transactionService.listByMonth(month, year, userId);

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponseDto> getMonthlySummary(@RequestParam(required = true) Integer month,
            @RequestParam(required = true) Integer year,
            @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        MonthlySummaryResponseDto summary = this.transactionService.getMonthlySummary(month, year, userId);

        return ResponseEntity.ok(summary);
    }
}
