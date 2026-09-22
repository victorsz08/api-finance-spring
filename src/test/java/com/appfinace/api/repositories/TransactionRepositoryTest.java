package com.appfinace.api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.transaction.Transaction;
import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.domain.user.User;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private User otherUser;
    private Category category;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setName("João");
        user.setEmail("joao@teste.com");
        user.setPassword("123456");
        user = userRepository.save(user);

        otherUser = new User();
        otherUser.setName("Maria");
        otherUser.setEmail("maria@teste.com");
        otherUser.setPassword("123456");
        otherUser = userRepository.save(otherUser);

        category = new Category();
        category.setName("Alimentação");
        category.setType("EXPENSE");
        category.setUser(user);
        category = categoryRepository.save(category);
    }

    private Transaction createTransaction(User owner, BigDecimal amount, TransactionType type, LocalDate date) {
        Transaction transaction = new Transaction();
        transaction.setDescription("Transação teste");
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDate(date);
        transaction.setUser(owner);
        transaction.setCategory(category);
        return transactionRepository.save(transaction);
    }

    @Test
    public void shouldFindTransactionsWithinDateRange() {
        createTransaction(user, new BigDecimal("100.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 10));
        createTransaction(user, new BigDecimal("200.00"), TransactionType.INCOME, LocalDate.of(2026, 3, 20));
        createTransaction(user, new BigDecimal("300.00"), TransactionType.EXPENSE, LocalDate.of(2026, 4, 5));

        List<Transaction> result = transactionRepository.findByUserAndMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).hasSize(2);
    }

    @Test
    public void shouldOrderTransactionsByDateDescending() {
        createTransaction(user, new BigDecimal("100.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 25));
        createTransaction(user, new BigDecimal("200.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 15));
        createTransaction(user, new BigDecimal("300.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 5));

        List<Transaction> result = transactionRepository.findByUserAndMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 3, 25));
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(result.get(2).getDate()).isEqualTo(LocalDate.of(2026, 3, 5));
    }

    @Test
    public void shouldNotReturnTransactionsFromOtherUsers() {
        createTransaction(user, new BigDecimal("100.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 25));
        createTransaction(otherUser, new BigDecimal("200.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 15));

        List<Transaction> result = transactionRepository.findByUserAndMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    public void shouldReturnEmptyListWhenNoTransactionsInRange() {
        createTransaction(user, new BigDecimal("100.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 25));

        List<Transaction> result = transactionRepository.findByUserAndMonth(
                user.getId(), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldSumIncomeCorrectly() {
        createTransaction(user, new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2026, 3, 25));
        createTransaction(user, new BigDecimal("200.00"), TransactionType.INCOME, LocalDate.of(2026, 3, 25));
        createTransaction(user, new BigDecimal("300.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 25));

        BigDecimal result = transactionRepository.sumIncomeByMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).isEqualByComparingTo("300.00");
    }

    @Test
    public void shouldReturnZeroWhenNoIncomeInMonth() {
        createTransaction(user, new BigDecimal("300.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 25));

        BigDecimal result = transactionRepository.sumIncomeByMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    public void shouldReturnZeroWhenNoTransactionsExistAtAll() {
        BigDecimal result = transactionRepository.sumIncomeByMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).isNotNull();
        assertThat(result).isEqualByComparingTo("0");
    }

    @Test
    public void shouldSumExpenseCorrectly() {
        createTransaction(user, new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2026, 3, 25));
        createTransaction(user, new BigDecimal("200.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 25));
        createTransaction(user, new BigDecimal("300.00"), TransactionType.EXPENSE, LocalDate.of(2026, 3, 25));

        BigDecimal result = transactionRepository.sumExpenseByMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).isEqualByComparingTo("500.00");
    }

    @Test
    public void shouldReturnZeroWhenNoExpenseInMonth() {
        createTransaction(user, new BigDecimal("100.00"), TransactionType.INCOME, LocalDate.of(2026, 3, 25));

        BigDecimal result = transactionRepository.sumExpenseByMonth(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(result).isEqualByComparingTo("0");
    }
}
