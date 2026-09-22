package com.appfinace.api.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.transaction.Transaction;
import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.transaction.MonthlySummaryResponseDto;
import com.appfinace.api.dto.transaction.TransactionRequestDto;
import com.appfinace.api.dto.transaction.TransactionResponseDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.TransactionRepository;
import com.appfinace.api.repositories.UserRepository;
import com.appfinace.api.service.TransactionService;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID userId;
    private UUID categoryId;
    private User existUser;
    private Category existCategory;
    private Transaction existTransaction;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        existUser = new User();
        existUser.setId(userId);

        existCategory = new Category();
        existCategory.setId(categoryId);
        existCategory.setName("Alimentação");
        existCategory.setType("EXPENSE");
        existCategory.setUser(existUser);

        existTransaction = new Transaction();
        existTransaction.setId(UUID.randomUUID());
        existTransaction.setDescription("Mercado");
        existTransaction.setAmount(new BigDecimal("250.00"));
        existTransaction.setDate(LocalDate.of(2026, 3, 15));
        existTransaction.setType(TransactionType.EXPENSE);
        existTransaction.setUser(existUser);
        existTransaction.setCategory(existCategory);
    }

    @Test
    public void shouldCreateTransactionSuccessfully() {
        TransactionRequestDto dto = new TransactionRequestDto(
                "Mercado", new BigDecimal("250.00"), TransactionType.EXPENSE, LocalDate.of(2026, 7, 15), categoryId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existUser));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existCategory));

        transactionService.create(dto, userId);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction saved = captor.getValue();

        assertThat(saved.getAmount()).isEqualByComparingTo("250.00");
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(saved.getDescription()).isEqualTo("Mercado");
        assertThat(saved.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(saved.getCategory()).isEqualTo(existCategory);
        assertThat(saved.getUser()).isEqualTo(existUser);
    }

    @Test
    public void shouldThrowNotFoundWhenCategoryNotFoundOnCreate() {
        TransactionRequestDto dto = new TransactionRequestDto(
                "Mercado", new BigDecimal("250.00"), TransactionType.EXPENSE, LocalDate.of(2026, 7, 15), categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(dto, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Categoria não localizada");

        verify(transactionRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    public void shouldThrowNotFoundWhenUserNotFoundOnCreate() {
        TransactionRequestDto dto = new TransactionRequestDto(
                "Mercado", new BigDecimal("250.00"), TransactionType.EXPENSE, LocalDate.of(2026, 7, 15), categoryId);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existCategory));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(dto, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Usuário não localizado");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    public void shouldListTransactionsByMonthSuccessfully() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate emdDate = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findByUserAndMonth(userId, startDate, emdDate))
                .thenReturn(List.of(existTransaction));

        List<TransactionResponseDto> result = transactionService.listByMonth(3, 2026, userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).amount()).isEqualByComparingTo("250.00");
        assertThat(result.get(0).description()).isEqualTo("Mercado");
        assertThat(result.get(0).category().name()).isEqualTo("Alimentação");
    }

    @Test
    public void shouldReturnEmptyListWhenNoTransactionsInMonth() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate emdDate = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findByUserAndMonth(userId, startDate, emdDate))
                .thenReturn(List.of());

        List<TransactionResponseDto> result = transactionService.listByMonth(3, 2026, userId);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldCalculateCorrectDateRangeForFebruaryLeapYear() {
        LocalDate startDate = LocalDate.of(2028, 2, 1);
        LocalDate emdDate = LocalDate.of(2028, 2, 29);

        when(transactionRepository.findByUserAndMonth(userId, startDate, emdDate))
                .thenReturn(List.of());

        transactionService.listByMonth(2, 2028, userId);

        verify(transactionRepository).findByUserAndMonth(userId, startDate, emdDate);
    }

    @Test
    public void shouldCalculateMonthlySummaryWithPositiveBalance() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);

        when(transactionRepository.sumIncomeByMonth(userId, startDate, endDate))
                .thenReturn(new BigDecimal("5000.00"));

        when(transactionRepository.sumExpenseByMonth(userId, startDate, endDate))
                .thenReturn(new BigDecimal("3000.00"));

        MonthlySummaryResponseDto result = transactionService.getMonthlySummary(3, 2026, userId);

        assertThat(result.month()).isEqualTo(3);
        assertThat(result.year()).isEqualTo(2026);
        assertThat(result.totalIncome()).isEqualByComparingTo("5000.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("3000.00");
        assertThat(result.balance()).isEqualByComparingTo("2000.00");
    }

    @Test
    public void shouldCalculateMonthlySummaryWithNegativeBalance() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);

        when(transactionRepository.sumIncomeByMonth(userId, startDate, endDate))
                .thenReturn(new BigDecimal("1000.00"));

        when(transactionRepository.sumExpenseByMonth(userId, startDate, endDate))
                .thenReturn(new BigDecimal("1500.00"));

        MonthlySummaryResponseDto result = transactionService.getMonthlySummary(3, 2026, userId);

        assertThat(result.balance()).isEqualByComparingTo("-500.00");
    }

    @Test
    public void shouldCalculateMonthlySummaryWithZeroIncomeAndExpense() {
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);

        when(transactionRepository.sumIncomeByMonth(userId, startDate, endDate))
                .thenReturn(BigDecimal.ZERO);

        when(transactionRepository.sumExpenseByMonth(userId, startDate, endDate))
                .thenReturn(BigDecimal.ZERO);

        MonthlySummaryResponseDto result = transactionService.getMonthlySummary(3, 2026, userId);

        assertThat(result.balance()).isEqualByComparingTo("0.00");
    }

    @Test
    public void shouldReturnZeroBalanceWhenNoTransactionsExistInMonth() {
        when(transactionRepository.sumIncomeByMonth(any(), any(), any())).thenReturn(null);
        when(transactionRepository.sumExpenseByMonth(any(), any(), any())).thenReturn(null);

        MonthlySummaryResponseDto result = transactionService.getMonthlySummary(3, 2026, userId);

        assertThat(result.totalIncome()).isEqualByComparingTo("0");
        assertThat(result.totalExpense()).isEqualByComparingTo("0");
        assertThat(result.balance()).isEqualByComparingTo("0");
    }
}
