package com.appfinace.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.installment.InstallmentResponseDto;
import com.appfinace.api.infra.exception.ExceptionResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.InstallmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/installments")
@Tag(name = "Parcelas", description = "Endpoints de gerenciamento de parcelas")
public class InstallmentController {

    public final InstallmentService installmentService;

    public InstallmentController(InstallmentService installmentService) {
        this.installmentService = installmentService;
    }

    @Operation(summary = "Marca parcela como paga", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Parcela não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PatchMapping("/{id}/pay")
    public ResponseEntity<Void> payInstallment(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        this.installmentService.payInstallment(id, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lista parcelas pendentes", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Informe mês e ano juntos, ou nenhum dos dois", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @GetMapping("/pending")
    public ResponseEntity<List<InstallmentResponseDto>> listPending(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Parameter(description = "Filtro parcial por mês da parcela", example = "10") @RequestParam(required = false) Integer month,
            @Parameter(description = "Filtro parcial pelo ano da parcela", example = "2026") @RequestParam(required = false) Integer year) {
        UUID userId = user.getUser().getId();
        List<InstallmentResponseDto> data = this.installmentService
                .listPending(userId, month, year);

        return ResponseEntity.ok(data);
    }
}
