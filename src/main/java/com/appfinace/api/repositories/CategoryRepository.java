package com.appfinace.api.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appfinace.api.domain.category.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    public List<Category> findByUserId(UUID userId);

    public Optional<Category> findByIdAndUserId(UUID id, UUID userId);
}
