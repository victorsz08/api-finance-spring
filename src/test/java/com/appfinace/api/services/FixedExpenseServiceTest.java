package com.appfinace.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.fixed_expense.FixedExpense;
import com.appfinace.api.domain.transaction.Transaction;
import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.fixed_expense.FixedExpenseRequestDto;
import com.appfinace.api.dto.fixed_expense.FixedExpenseResponseDto;
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
        existsCategory.setName("Moradia");
        existsCategory.setType("EXPENSE");
        existingFixedExpense.setId(fixedExpenseId);
        existingFixedExpense.setActive(true);
        existingFixedExpense.setAmount(new BigDecimal(1200));
        existingFixedExpense.setDescription("teste");
        existingFixedExpense.setDueDay(10);
        existingFixedExpense.setUser(existingUser);
        existingFixedExpense.setCategory(existsCategory);
    }

    @Test
    public void shouldCreateFixedExpenseSuccessfully() {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("teste", new BigDecimal(1200), 10, categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(existsCategory));

        fixedExpenseService.create(dto, userId);

        ArgumentCaptor<FixedExpense> captor = ArgumentCaptor.forClass(FixedExpense.class);
        verify(fixedExpenseRepository, times(1)).save(captor.capture());

        FixedExpense expenseSaved = captor.getValue();

        assertThat(expenseSaved.getAmount()).isEqualTo(new BigDecimal(1200));
        assertThat(expenseSaved.getActive()).isEqualTo(true);
        assertThat(expenseSaved.getDescription()).isEqualTo("teste");
        assertThat(expenseSaved.getDueDay()).isEqualTo(10);
    }

    @Test
    public void shouldThrowWhenCreateWithUserNotExists() {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("teste", new BigDecimal(1200), 10, categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.create(dto, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário não localizado");

        verify(fixedExpenseRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenCreateWithCategoryNotExists() {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("teste", new BigDecimal(1200), 10, categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.create(dto, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada");

        verify(fixedExpenseRepository, never()).save(any());
    }

    @Test
    public void shouldReturnListFixedExpenseByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FixedExpense> page = new PageImpl<>(List.of(existingFixedExpense), pageable, 1);

        when(fixedExpenseRepository
                .getFiltredFixedExpenses(
                        userId,
                        categoryId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10)))
                .thenReturn(page);

        List<FixedExpenseResponseDto> result = fixedExpenseService.listByFiltred(
                0, 10, null, null, null, null, null, categoryId, userId);

        FixedExpenseResponseDto dto = result.get(0);

        assertThat(dto.description()).isEqualTo("teste");
        assertThat(dto.amount()).isEqualTo(new BigDecimal(1200));
        assertThat(dto.dueDay()).isEqualTo(10);
        assertThat(dto.active()).isEqualTo(true);
        assertThat(dto.category().name()).isEqualTo("Moradia");
        assertThat(dto.category().type()).isEqualTo("EXPENSE");
    }

    @Test
    public void shouldReturnEmptyListWhenNoFixedExpensesMatchFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<FixedExpense> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(fixedExpenseRepository
                .getFiltredFixedExpenses(
                        userId,
                        categoryId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10)))
                .thenReturn(emptyPage);

        List<FixedExpenseResponseDto> result = fixedExpenseService.listByFiltred(
                0, 10, null, null, null, null, null, categoryId, userId);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnFindFixedExpenseWithId() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId))
                .thenReturn(Optional.of(existingFixedExpense));

        FixedExpenseResponseDto result = fixedExpenseService.findById(fixedExpenseId, userId);

        assertThat(result.description()).isEqualTo("teste");
        assertThat(result.amount()).isEqualTo(new BigDecimal(1200));
        assertThat(result.category().name()).isEqualTo("Moradia");
        assertThat(result.category().type()).isEqualTo("EXPENSE");
        assertThat(result.dueDay()).isEqualTo(10);
        assertThat(result.active()).isEqualTo(true);
    }

    @Test
    public void shouldThrowNotFountFixedExpenseWithId() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.findById(fixedExpenseId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Despesa não localizada");
    }

    @Test
    public void shouldUpdateFixedExpenseSuccessfully() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId))
                .thenReturn(Optional.of(existingFixedExpense));

        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("test-updated", new BigDecimal(1500), 15, categoryId);

        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.of(existsCategory));

        fixedExpenseService.update(fixedExpenseId, dto, userId);

        ArgumentCaptor<FixedExpense> captor = ArgumentCaptor.forClass(FixedExpense.class);
        verify(fixedExpenseRepository, times(1)).save(captor.capture());

        FixedExpense fixedExpenseUpdated = captor.getValue();

        assertThat(fixedExpenseUpdated.getDescription()).isEqualTo("test-updated");
        assertThat(fixedExpenseUpdated.getAmount()).isEqualTo(new BigDecimal(1500));
        assertThat(fixedExpenseUpdated.getDueDay()).isEqualTo(15);
    }

    @Test
    public void shouldThrowWhenUpdateFixedExpenseNotFoundWithId() {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("test-updated", new BigDecimal(1500), 15, categoryId);
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.update(fixedExpenseId, dto, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Despesa não localizada");

        verify(fixedExpenseRepository, never()).save(any());
    }

    @Test
    public void shouldThrowWhenUpdateFixedExpenseWithCategoryNotFound() {
        FixedExpenseRequestDto dto = new FixedExpenseRequestDto("test-updated", new BigDecimal(1500), 15, categoryId);

        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId))
                .thenReturn(Optional.of(existingFixedExpense));
        when(categoryRepository.findByIdAndUserId(categoryId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.update(fixedExpenseId, dto, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada");

        verify(fixedExpenseRepository, never()).save(any());
    }

    @Test
    public void shouldUpdateActiveFixedExpenseSuccessfully() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId))
                .thenReturn(Optional.of(existingFixedExpense));

        fixedExpenseService.updateActive(fixedExpenseId, false, userId);
        ArgumentCaptor<FixedExpense> captor = ArgumentCaptor.forClass(FixedExpense.class);
        verify(fixedExpenseRepository, times(1)).save(captor.capture());

        FixedExpense fixedExpenseUpdated = captor.getValue();

        assertThat(fixedExpenseUpdated.getActive()).isEqualTo(false);
    }

    @Test
    public void shouldThrowWhenUpdateActiveFixedExpenseNotFound() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.updateActive(fixedExpenseId, false, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Despesa não localizada");

        verify(fixedExpenseRepository, never()).save(any());
    }

    @Test
    public void shouldDeleteFixedExpenseSuccessfully() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId))
                .thenReturn(Optional.of(existingFixedExpense));

        fixedExpenseService.delete(fixedExpenseId, userId);
        verify(fixedExpenseRepository, times(1)).delete(existingFixedExpense);
    }

    @Test
    public void shouldThrowWhenDeleteFixedExpendeNotFound() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.delete(fixedExpenseId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Despesa não localizada");

        verify(fixedExpenseRepository, never()).delete(any());
    }

    @Test
    public void shouldMarkAsPaidFixedExpenseSuccessfully() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId))
                .thenReturn(Optional.of(existingFixedExpense));

        fixedExpenseService.markAsPaid(fixedExpenseId, userId);

        ArgumentCaptor<FixedExpense> captorExpense = ArgumentCaptor.forClass(FixedExpense.class);
        verify(fixedExpenseRepository, times(1)).save(captorExpense.capture());
        assertThat(captorExpense.getValue().getActive()).isFalse();

        ArgumentCaptor<Transaction> captorTransaction = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captorTransaction.capture());

        Transaction transactionSaved = captorTransaction.getValue();
        assertThat(transactionSaved.getDescription()).isEqualTo("teste");
        assertThat(transactionSaved.getAmount()).isEqualByComparingTo("1200");
        assertThat(transactionSaved.getDate()).isEqualTo(LocalDate.now());
        assertThat(transactionSaved.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(transactionSaved.getCategory()).isEqualTo(existsCategory);
        assertThat(transactionSaved.getUser()).isEqualTo(existingUser);
    }

    @Test
    public void shouldThrowNotFoundWhenMarkAsPaidFixedExpenseNotFound() {
        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fixedExpenseService.markAsPaid(fixedExpenseId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Despesa não localizada");

        verify(fixedExpenseRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    public void shouldThrowConflictWhenFixedExpenseAlreadyInactive() {
        existingFixedExpense.setActive(false);

        when(fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId))
                .thenReturn(Optional.of(existingFixedExpense));

        assertThatThrownBy(() -> fixedExpenseService.markAsPaid(fixedExpenseId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Despesa já está inativa");

        verify(fixedExpenseRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}