package com.appfinace.api.service;

import java.util.List;
import java.util.Optional;
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

        Optional<User> user = this.userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não localizado com esse id");
        }

        User aUser = user.get();

        aCategory.setName(data.name());
        aCategory.setType(data.type());
        aCategory.setUser(aUser);

        this.categoryRepository.save(aCategory);
    }

    public CategoryResponseDto findCategory(UUID id) {
        Optional<Category> data = this.categoryRepository.findById(id);

        if (data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada com esse id");
        }

        Category category = data.get();
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getType());
    }

    public List<CategoryResponseDto> listCategories(UUID userId) {
        List<Category> categories = this.categoryRepository.findByUserId(userId);

        return categories.stream().map(c -> new CategoryResponseDto(
                c.getId(),
                c.getName(),
                c.getType())).toList();
    }

    public void update(UUID id, String name, String type) {
        Optional<Category> data = this.categoryRepository.findById(id);

        if (data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada com esse id");
        }

        Category aCategory = data.get();

        aCategory.setName(name);
        aCategory.setType(type);

        this.categoryRepository.save(aCategory);
    }

    public void delete(UUID id) {
        Optional<Category> data = this.categoryRepository.findById(id);

        if (data.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada com esse id");
        }

        this.categoryRepository.delete(data.get());
    }
}
