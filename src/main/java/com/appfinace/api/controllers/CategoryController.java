package com.appfinace.api.controllers;

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
import org.springframework.web.bind.annotation.RestController;

import com.appfinace.api.dto.category.CategoryRequestDto;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.infra.exception.ExceptionResponseDto;
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categorias", description = "Endpoints de gerenciamento das categorias")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Criação da categoria", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "201", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Usuário não localizado", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PostMapping()
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @Valid @RequestBody CategoryRequestDto body) {
        UUID userId = user.getUser().getId();
        this.categoryService.createCategory(body, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Listar categorias", description = "Lista as categorias do usuário", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> list(@AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        List<CategoryResponseDto> data = this.categoryService.listCategories(userId);

        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Busca categoria pelo ID", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Categoria não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> find(@AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable UUID id) {
        UUID userId = user.getUser().getId();
        CategoryResponseDto data = this.categoryService.findCategory(id, userId);

        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Atualiza categoria", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Categoria não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados incorretos", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable UUID id,
            @Valid @RequestBody CategoryRequestDto body) {
        UUID userId = user.getUser().getId();
        this.categoryService.update(id, body.name(), body.type(), userId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Excluir categoria", security = @SecurityRequirement(name = "cookieAuth"), responses = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Categoria não localizada", content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable UUID id) {
        UUID userId = user.getUser().getId();
        this.categoryService.delete(id, userId);

        return ResponseEntity.ok().build();
    }
}
