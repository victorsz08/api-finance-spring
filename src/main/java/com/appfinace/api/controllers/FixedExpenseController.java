package com.appfinace.api.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.fixed_expense.FixedExpenseRequestDto;
import com.appfinace.api.dto.fixed_expense.FixedExpenseResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.FixedExpenseService;

@RestController
@RequestMapping("/api/fixed-expenses")
public class FixedExpenseController {

    private final FixedExpenseService fixedExpenseService;

    public FixedExpenseController(FixedExpenseService fixedExpenseService) {
        this.fixedExpenseService = fixedExpenseService;
    }

    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody FixedExpenseRequestDto body) {
        UUID userId = user.getUser().getId();

        this.fixedExpenseService.create(body, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<FixedExpenseResponseDto>> listFiltred(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) BigDecimal startAmount,
            @RequestParam(required = false) BigDecimal endAmount,
            @RequestParam(required = false) Integer startDueDay,
            @RequestParam(required = false) Integer endDueDay,
            @RequestParam(required = false) Boolean active) {
        UUID userId = user.getUser().getId();

        List<FixedExpenseResponseDto> data = this.fixedExpenseService.listByFiltred(
                page,
                size,
                startAmount,
                endAmount,
                startDueDay,
                endDueDay,
                active,
                categoryId,
                userId);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FixedExpenseResponseDto> find(@PathVariable UUID id) {
        FixedExpenseResponseDto data = this.fixedExpenseService.findById(id);

        return ResponseEntity.ok(data);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id,
            @RequestBody FixedExpenseRequestDto body) {
        this.fixedExpenseService.update(id, body);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/active/{id}")
    public ResponseEntity<Void> updateActivEntity(@PathVariable UUID id, @RequestParam Boolean active) {
        this.fixedExpenseService.updateActive(id, active);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.fixedExpenseService.delete(id);

        return ResponseEntity.ok().build();
    }
}
