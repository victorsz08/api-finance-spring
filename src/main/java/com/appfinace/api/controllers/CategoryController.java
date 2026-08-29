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
import com.appfinace.api.infra.security.UserDetailsImpl;
import com.appfinace.api.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping()
    public ResponseEntity<Void> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody CategoryRequestDto body) {
        UUID userId = user.getUser().getId();
        this.categoryService.createCategory(body, userId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> list(@AuthenticationPrincipal UserDetailsImpl user) {
        UUID userId = user.getUser().getId();
        List<CategoryResponseDto> data = this.categoryService.listCategories(userId);

        return ResponseEntity.ok(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> find(@PathVariable UUID id) {
        CategoryResponseDto data = this.categoryService.findCategory(id);

        return ResponseEntity.ok(data);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody CategoryRequestDto body) {
        this.categoryService.update(id, body.name(), body.type());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        this.categoryService.delete(id);

        return ResponseEntity.ok().build();
    }
}
