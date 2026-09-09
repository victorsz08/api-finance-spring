package com.appfinace.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryRequestDto;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.UserRepository;
import com.appfinace.api.service.CategoryService;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    private UUID categoryId;
    private UUID userId;
    private UUID otherUserId;
    private Category existingsCategory;
    private User user;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    public void setUp() {
        categoryId = UUID.randomUUID();
        userId = UUID.randomUUID();
        otherUserId = UUID.randomUUID();

        user = new User();
        user.setId(userId);

        existingsCategory = new Category();
        existingsCategory.setId(categoryId);
        existingsCategory.setName("Teste");
        existingsCategory.setType("EXPENSE");
        existingsCategory.setUser(user);
    }

    @Test
    public void shouldCreateCategorySuccessfully() {
        CategoryRequestDto dto = new CategoryRequestDto("Alimentação", "EXPENSE");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        categoryService.createCategory(dto, userId);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository, times(1)).save(captor.capture());

        Category categorySaved = captor.getValue();

        assertThat(categorySaved.getName()).isEqualTo("Alimentação");
        assertThat(categorySaved.getType()).isEqualTo("EXPENSE");
        assertThat(categorySaved.getUser()).isEqualTo(user);
    }

    @Test
    public void shouldThrowWhenCreateCategoryWithUserNotExists() {
        CategoryRequestDto dto = new CategoryRequestDto("Alimentação", "EXPENSE");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.createCategory(dto, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário não localizado com esse id");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void shouldReturnCategoryWithId() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(existingsCategory));

        CategoryResponseDto response = categoryService.findCategory(categoryId, userId);

        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo("Teste");
        assertThat(response.type()).isEqualTo("EXPENSE");
    }

    @Test
    public void shouldThrowCategoryNotFoundWithId() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findCategory(categoryId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada com esse id");
    }

    @Test
    public void shouldThrowNotFoundWhenCategoryDoesNotBelongToUser() {
        when(categoryRepository.findByIdAndUserId(categoryId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findCategory(categoryId, otherUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada com esse id");
    }

    @Test
    public void shouldReturnListCategoryOfUser() {
        when(categoryRepository.findByUserId(userId)).thenReturn(List.of(existingsCategory));

        List<CategoryResponseDto> result = categoryService.listCategories(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(categoryId);
    }

    @Test
    public void shouldReturnAnEmptyListWhenTheUserHasNoCategories() {
        when(categoryRepository.findByUserId(userId)).thenReturn(List.of());

        List<CategoryResponseDto> result = categoryService.listCategories(userId);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldUpdateCategorySuccessfully() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(existingsCategory));

        categoryService.update(categoryId, "Transporte", "EXPENSE", userId);

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Transporte");
        assertThat(captor.getValue().getType()).isEqualTo("EXPENSE");
    }

    @Test
    public void shouldThrowUpdateCategoryWhenCategoryNotExists() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(categoryId, "Transporte", "Expense", userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada com esse id");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void shouldThrowNotFoundWhenUpdatingCategoryThatBelongsToAnotherUser() {
        when(categoryRepository.findByIdAndUserId(categoryId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(categoryId, "Hackeado", "EXPENSE", otherUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada com esse id");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    public void shouldDeleteCategorySuccessfully() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(existingsCategory));

        categoryService.delete(categoryId, userId);

        verify(categoryRepository, times(1)).delete(existingsCategory);
    }

    @Test
    public void shouldThrowWhenDeleteCategoryNotFoundWithId() {
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(categoryId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada com esse id");

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    public void shouldThrowNotFoundWhenDeletingCategoryThatBelongsToAnotherUser() {
        when(categoryRepository.findByIdAndUserId(categoryId, otherUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(categoryId, otherUserId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada com esse id");

        verify(categoryRepository, never()).delete(any());
    }
}
