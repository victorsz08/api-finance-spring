package com.appfinace.api.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.fixed_expense.FixedExpenseRequestDto;
import com.appfinace.api.dto.fixed_expense.FixedExpenseResponseDto;
import com.appfinace.api.infra.exception.ExceptionResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.FixedExpenseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fixed-expenses")
@Tag(name = "Despesa Fixa", description = "Endpoint de gerenciamento de despesas fixas")
public class FixedExpenseController {

    private final FixedExpenseService fixedExpenseService;

    public FixedExpenseController(FixedExpenseService fixedExpenseService) {
        this.fixedExpenseService = fixedExpenseService;
    }

    @Operation(summary = "Criar despesa fixa", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PostMapping
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody FixedExpenseRequestDto body) {
        UUID userId = user.getUser().getId();

        this.fixedExpenseService.create(body, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Lista despesas fixas filtradas", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<FixedExpenseResponseDto>> listFiltred(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Parameter(description = "Número da pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de resultados por página") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Fitro por ID da categoria") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Filtro pacial por valor mínimo da despesa") @RequestParam(required = false) BigDecimal startAmount,
            @Parameter(description = "Filtro parcial por valor máximo da despesa") @RequestParam(required = false) BigDecimal endAmount,
            @Parameter(description = "Filtro parcial pela data mínima da despesa") @RequestParam(required = false) Integer startDueDay,
            @Parameter(description = "Filtro parcial pela data máxima da despesa") @RequestParam(required = false) Integer endDueDay,
            @Parameter(description = "Filtro parcial de despesa ativa ou inativa") @RequestParam(required = false) Boolean active) {
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

    @Operation(summary = "Busca despesa fixa pelo ID", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Despesa fixa não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FixedExpenseResponseDto> find(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID id) {
        UUID userId = user.getUser().getId();
        FixedExpenseResponseDto data = this.fixedExpenseService.findById(id, userId);

        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Atualiza despesa fixa", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Despesa fixa não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable UUID id,
            @Valid @RequestBody FixedExpenseRequestDto body) {
        UUID userId = user.getUser().getId();
        this.fixedExpenseService.update(id, body, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Ativa ou desativa despesa fixa", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Despesa fixa não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PutMapping("/active/{id}")
    public ResponseEntity<Void> updateActivEntity(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable UUID id,
            @Parameter(description = "Informa um booleano para ativar ou desativar despesa fixa") @RequestParam Boolean active) {
        UUID userId = user.getUser().getId();
        this.fixedExpenseService.updateActive(id, active, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Excluir despesa fixa", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Despesa fixa não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable UUID id) {
        UUID userId = user.getUser().getId();
        this.fixedExpenseService.delete(id, userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Marcar despesa fixa como paga", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Despesa fixa não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Despesa já está inativa", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PatchMapping("/{id}/pay")
    public ResponseEntity<Void> markAsPaid(@PathVariable UUID id, @AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        this.fixedExpenseService.markAsPaid(id, userId);

        return ResponseEntity.ok().build();
    }
}
