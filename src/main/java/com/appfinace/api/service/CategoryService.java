package com.appfinace.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryRequestDto;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.UserRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public void createCategory(CategoryRequestDto data, UUID userId) {
        Category aCategory = new Category();

        User user = this.userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não localizado com esse id"));

        aCategory.setName(data.name());
        aCategory.setType(data.type());
        aCategory.setIcon(data.icon());
        aCategory.setUser(user);

        this.categoryRepository.save(aCategory);
    }

    public CategoryResponseDto findCategory(UUID id, UUID userId) {
        Category category = this.categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoria não localizada com esse id"));

        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon()
            );
    }

    public List<CategoryResponseDto> listCategories(UUID userId) {
        List<Category> categories = this.categoryRepository.findByUserId(userId);

        return categories.stream().map(c -> new CategoryResponseDto(
                c.getId(),
                c.getName(),
                c.getType(),
                c.getIcon()
            )).toList();
    }

    public void update(UUID id, UUID userId, CategoryRequestDto data) {
        Category category = this.categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoria não localizada com esse id"));

        category.setName(data.name());
        category.setType(data.type());
        category.setIcon(data.icon());

        this.categoryRepository.save(category);
    }

    public void delete(UUID id, UUID userId) {
        Category category = this.categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoria não localizada com esse id"));

        this.categoryRepository.delete(category);
    }
}
