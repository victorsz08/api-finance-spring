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
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.InstallmentService;

@RestController
@RequestMapping("/api/installments")
public class InstallmentController {

    public final InstallmentService installmentService;

    public InstallmentController(InstallmentService installmentService) {
        this.installmentService = installmentService;
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<Void> payInstallment(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        this.installmentService.payInstallment(id, userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InstallmentResponseDto>> listPending(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(required = false) int month,
            @RequestParam(required = false) int year) {
        UUID userId = user.getUser().getId();
        List<InstallmentResponseDto> data = this.installmentService.listPending(userId, month, year);

        return ResponseEntity.ok(data);
    }
}
