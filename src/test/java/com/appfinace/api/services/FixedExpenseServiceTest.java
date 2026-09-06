package com.appfinace.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.fixed_expense.FixedExpense;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.fixed_expense.FixedExpenseRequestDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.FixedExpenseRepository;
import com.appfinace.api.repositories.TransactionRepository;
import com.appfinace.api.repositories.UserRepository;
import com.appfinace.api.service.FixedExpenseService;

@ExtendWith(MockitoExtension.class)
public class FixedExpenseServiceTest {

    @Mock 
    private FixedExpenseRepository fixedExpenseRepository;

    @Mock 
    private UserRepository userRepository;

    @Mock 
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private UUID fixedExpenseId;
    private UUID userId;
    private UUID categoryId;
    private FixedExpense existingFixedExpense;
    private User existingUser;
    private Category existsCategory;

    @InjectMocks 
    private FixedExpenseService fixedExpenseService;

    @BeforeEach
    public void setUp() {
        fixedExpenseId = UUID.randomUUID();
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        existingUser = new User();
        existsCategory = new Category();
        existingFixedExpense = new FixedExpense();

        existingUser.setId(userId);
        existsCategory.setId(categoryId);
        existingFixedExpense.setId(fixedExpenseId);
        existingFixedExpense.setActive(true);
        existingFixedExpense.setAmount(BigDecimal.valueOf(120.00));
        existingFixedExpense.setDescription("teste");
        existingFixedExpense.setDueDay(10);
        existingFixedExpense.setUser(existingUser);
        existingFixedExpense.setCategory(existsCategory);
    }


    @Test 
    public void shouldCreateFixedExpenseSuccessfully() {
        FixedExpenseRequestDto dto = 
            new FixedExpenseRequestDto("teste", BigDecimal.valueOf(120.00), 10, categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existsCategory));

        fixedExpenseService.create(dto, userId);

        ArgumentCaptor<FixedExpense> captor = ArgumentCaptor.forClass(FixedExpense.class);
        verify(fixedExpenseRepository, times(1)).save(captor.capture());
        
        FixedExpense expenseSaved = captor.getValue();

        assertThat(expenseSaved.getAmount()).isEqualTo(BigDecimal.valueOf(120.00));
        assertThat(expenseSaved.getActive()).isEqualTo(true);
        assertThat(expenseSaved.getDescription()).isEqualTo("teste");
        assertThat(expenseSaved.getDueDay()).isEqualTo(10);
    }

    @Test
    public void shouldThrowWhenCreateWithUserNotExists() {
        FixedExpenseRequestDto dto = 
            new FixedExpenseRequestDto("teste", BigDecimal.valueOf(120.00), 10, categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.create(dto, userId))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Usuário não localizado");

        verify(fixedExpenseRepository, never()).save(any());
    }
}