package com.appfinace.api.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.installment.InstallmentPurchaseRequestDto;
import com.appfinace.api.dto.installment.InstallmentPurchaseResponseDto;
import com.appfinace.api.infra.exception.ExceptionResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.InstallmentPurchaseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/installment-purchase")
@Tag(name = "Compras Parceladas", description = "Gerenciamento de compras parceladas")
public class InstallmentPurchaseController {

        private final InstallmentPurchaseService purchaseService;

        public InstallmentPurchaseController(
                        InstallmentPurchaseService purchaseService) {
                this.purchaseService = purchaseService;
        }

        @Operation(summary = "Criar Compra Parcelada", security = @SecurityRequirement(name = "cookieAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "OK"),
                        @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
        })
        @PostMapping
        public ResponseEntity<Void> create(
                        @Valid @RequestBody InstallmentPurchaseRequestDto body,
                        @AuthenticationPrincipal UserDetailsImpl user) {
                UUID userId = user.getUser().getId();
                purchaseService.create(body, userId);

                return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        @Operation(summary = "Lista despesas fixas filtradas", security = @SecurityRequirement(name = "cookieAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "OK"),
        })
        @GetMapping
        public ResponseEntity<List<InstallmentPurchaseResponseDto>> listFiltred(
                        @Parameter(description = "Número da página", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Número de resultados por página", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Filtro parcial por Id da categoria vinculada") @RequestParam(required = false) UUID categoryId,
                        @Parameter(description = "Filtro parcial pela data minima de vencimento") @RequestParam(required = false) LocalDate startDate,
                        @Parameter(description = "Filtro parcial pela data máxima de vencimento") @RequestParam(required = false) LocalDate endDate,
                        @Parameter(description = "Filtro parcial por despesas ativas") @RequestParam(required = false) String onlyOpen,
                        @AuthenticationPrincipal UserDetailsImpl user) {
                Boolean onlyOpenBool = onlyOpen != null ? Boolean.getBoolean(onlyOpen)
                                : null;
                UUID userId = user.getUser().getId();

                List<InstallmentPurchaseResponseDto> purchases = purchaseService
                                .listByUser(page,
                                                size, categoryId, startDate,
                                                endDate, userId, onlyOpenBool);

                return ResponseEntity.ok(purchases);
        }

        @Operation(summary = "Busca Despesa Parcelada pelo ID", security = @SecurityRequirement(name = "cookieAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "OK"),
                        @ApiResponse(responseCode = "404", description = "Despesa não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
        })
        @GetMapping("/{id}")
        public ResponseEntity<InstallmentPurchaseResponseDto> find(
                        @PathVariable UUID id) {
                InstallmentPurchaseResponseDto purchase = purchaseService.findOne(id);

                return ResponseEntity.ok(purchase);
        }

        @Operation(summary = "Excluir Despesa fixa", security = @SecurityRequirement(name = "cookieAuth"), responses = {
                        @ApiResponse(responseCode = "200", description = "OK"),
                        @ApiResponse(responseCode = "404", description = "Despesa não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> delete(@PathVariable UUID id) {
                this.purchaseService.delete(id);

                return ResponseEntity.ok().build();
        }
}
