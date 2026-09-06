package com.appfinace.api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.fixed_expense.FixedExpense;
import com.appfinace.api.domain.user.User;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class FixedExpenseRepositoryTest {

    @Autowired
    private FixedExpenseRepository fixedExpenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private User otherUser;
    private Category category;
    private Category otherCategory;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setName("Joao");
        user.setEmail("joao@email.com");
        user.setPassword("12345");
        userRepository.save(user);

        otherUser = new User();
        otherUser.setName("Maria");
        otherUser.setEmail("maria@email.com");
        otherUser.setPassword("54321");
        userRepository.save(otherUser);

        category = new Category();
        category.setName("Moradia");
        category.setType("EXPENSE");
        category.setUser(user);
        categoryRepository.save(category);

        otherCategory = new Category();
        otherCategory.setName("Transporte");
        otherCategory.setType("EXPENSE");
        otherCategory.setUser(user);
        categoryRepository.save(otherCategory);
    }

    private FixedExpense createFixedExpense(User owner, Category cat, BigDecimal amount, int dueDay, Boolean active) {
        FixedExpense fixedExpense = new FixedExpense();
        fixedExpense.setDescription("Despesa teste");
        fixedExpense.setAmount(amount);
        fixedExpense.setDueDay(dueDay);
        fixedExpense.setActive(active);
        fixedExpense.setUser(owner);
        fixedExpense.setCategory(cat);
        return fixedExpenseRepository.save(fixedExpense);
    }

    @Test
    public void shouldFindFixedExpensesByIdAndUserId() {
        FixedExpense saved = createFixedExpense(user, category, new BigDecimal("500.00"), 10, true);

        Optional<FixedExpense> result = fixedExpenseRepository.findByIdAndUserId(saved.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Despesa teste");
    }

    @Test
    public void shouldNotFindFixedExpenseWhenUserIdDoesNotMatch() {
        FixedExpense saved = createFixedExpense(user, category, new BigDecimal("500.00"), 10, true);

        Optional<FixedExpense> result = fixedExpenseRepository.findByIdAndUserId(saved.getId(), otherUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnAllFixedExpensesWhenNoFiltersApplied() {
        createFixedExpense(user, category, new BigDecimal("500.00"), 10, true);
        createFixedExpense(user, otherCategory, new BigDecimal("1200.00"), 15, true);

        Pageable pageable = PageRequest.of(0, 10);

        Page<FixedExpense> result = fixedExpenseRepository.getFiltredFixedExpenses(
                user.getId(), null, null, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    public void shouldFilterByAmountRange() {
        createFixedExpense(user, category, new BigDecimal("500.00"), 10, true);
        createFixedExpense(user, category, new BigDecimal("1500.00"), 10, true);
        createFixedExpense(user, category, new BigDecimal("3000.00"), 10, true);

        Pageable pageable = PageRequest.of(0, 10);

        Page<FixedExpense> result = fixedExpenseRepository.getFiltredFixedExpenses(
                user.getId(), null, new BigDecimal("1000.00"), new BigDecimal("2000.00"), null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAmount()).isEqualByComparingTo("1500.00");
    }

    @Test
    public void shouldFilterByDueDayRange() {
        createFixedExpense(user, category, new BigDecimal("500.00"), 5, true);
        createFixedExpense(user, category, new BigDecimal("1500.00"), 15, true);
        createFixedExpense(user, category, new BigDecimal("3000.00"), 25, true);

        Pageable pageable = PageRequest.of(0, 10);

        Page<FixedExpense> result = fixedExpenseRepository.getFiltredFixedExpenses(
                user.getId(), null, null, null, 10, 20, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDueDay()).isEqualTo(15);
    }

    @Test
    public void shouldFilterByActiveStatus() {
        createFixedExpense(user, category, new BigDecimal("500.00"), 5, true);
        createFixedExpense(user, category, new BigDecimal("1500.00"), 15, false);

        Pageable pageable = PageRequest.of(0, 10);

        Page<FixedExpense> result = fixedExpenseRepository.getFiltredFixedExpenses(
                user.getId(), null, null, null, null, null, true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getActive()).isTrue();
    }

    @Test
    public void shouldFilterByCategoryId() {
        createFixedExpense(user, category, new BigDecimal("500.00"), 5, true);
        createFixedExpense(user, otherCategory, new BigDecimal("1500.00"), 15, false);

        Pageable pageable = PageRequest.of(0, 10);

        Page<FixedExpense> result = fixedExpenseRepository.getFiltredFixedExpenses(
                user.getId(), category.getId(), null, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void shouldNotReturnFixedExpensesFromOtherUsers() {
        createFixedExpense(user, category, new BigDecimal("500.00"), 5, true);
        createFixedExpense(otherUser, category, new BigDecimal("1500.00"), 15, false);

        Pageable pageable = PageRequest.of(0, 10);

        Page<FixedExpense> result = fixedExpenseRepository.getFiltredFixedExpenses(
                user.getId(), null, null, null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    public void shouldRespectPagination() {
        for (int i = 0; i < 5; i++) {
            createFixedExpense(user, category, new BigDecimal("500.00"), 10, true);
        }

        Pageable pageable = PageRequest.of(0, 2);
        Page<FixedExpense> result = fixedExpenseRepository.getFiltredFixedExpenses(
                user.getId(), null, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }
}
