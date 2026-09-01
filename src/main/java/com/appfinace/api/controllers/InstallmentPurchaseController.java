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
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.installment.InstallmentPurchaseRequestDto;
import com.appfinace.api.dto.installment.InstallmentPurchaseResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.InstallmentPurchaseService;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/installment-purchase")
public class InstallmentPurchaseController {

    private final InstallmentPurchaseService purchaseService;

    public InstallmentPurchaseController(InstallmentPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody InstallmentPurchaseRequestDto body,
            @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        purchaseService.create(body, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<InstallmentPurchaseResponseDto>> listFiltred(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String onlyOpen,
            @AuthenticationPrincipal UserDetailsImpl user) {
        Boolean onlyOpenBool = onlyOpen != null ? Boolean.parseBoolean(onlyOpen) : null;
        UUID userId = user.getUser().getId();

        List<InstallmentPurchaseResponseDto> purchases = purchaseService.listByUser(page, size, categoryId, startDate,
                endDate, userId, onlyOpenBool);

        return ResponseEntity.ok(purchases);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstallmentPurchaseResponseDto> find(@PathVariable UUID id) {
        InstallmentPurchaseResponseDto purchase = purchaseService.findOne(id);

        return ResponseEntity.ok(purchase);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.purchaseService.delete(id);

        return ResponseEntity.ok().build();
    }
}
