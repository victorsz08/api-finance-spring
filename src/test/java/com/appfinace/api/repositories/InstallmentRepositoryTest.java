package com.appfinace.api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.installment.Installment;
import com.appfinace.api.domain.installment.InstallmentPurchase;
import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.domain.user.User;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class InstallmentRepositoryTest {

    @Autowired
    private InstallmentRepository installmentRepository;

    @Autowired
    private InstallmentPurchaseRepository purchaseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Category category;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setName("João");
        user.setEmail("joao@teste.com");
        user.setPassword("123456");
        user = userRepository.save(user);

        category = new Category();
        category.setName("Eletrônicos");
        category.setType("EXPENSE");
        category.setUser(user);
        category = categoryRepository.save(category);
    }

    private Installment createInstallment(LocalDate dueDate, InstallmentStatus status) {
        InstallmentPurchase purchase = new InstallmentPurchase();
        purchase.setDescription("Notebook");
        purchase.setTotalAmount(new BigDecimal("3000.00"));
        purchase.setTotalInstallments(3);
        purchase.setPurchaseDate(LocalDate.of(2026, 1, 10));
        purchase.setUser(user);
        purchase.setCategory(category);
        purchase = purchaseRepository.save(purchase);

        Installment installment = new Installment();
        installment.setNumber(1);
        installment.setAmount(new BigDecimal("1000.00"));
        installment.setDueDate(dueDate);
        installment.setStatus(status);
        installment.setPurchase(purchase);
        return installmentRepository.save(installment);
    }

    @Test
    public void shouldFindPendingInstallmentsByMonthAndYear() {
        createInstallment(LocalDate.of(2026, 1, 10), InstallmentStatus.PENDING);
        createInstallment(LocalDate.of(2026, 2, 10), InstallmentStatus.PENDING);
        createInstallment(LocalDate.of(2025, 1, 15), InstallmentStatus.PENDING);
        createInstallment(LocalDate.of(2026, 1, 15), InstallmentStatus.PAID);

        List<Installment> result = installmentRepository.findPendingByMonth(user.getId(), 1, 2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(InstallmentStatus.PENDING);
        assertThat(result.get(0).getDueDate()).isEqualTo(LocalDate.of(2026, 1, 10));
    }

    @Test
    public void shouldFindAllPendingInstallments() {
        createInstallment(LocalDate.of(2026, 1, 10), InstallmentStatus.PENDING);
        createInstallment(LocalDate.of(2026, 2, 10), InstallmentStatus.PENDING);
        createInstallment(LocalDate.of(2026, 1, 15), InstallmentStatus.PAID);

        List<Installment> result = installmentRepository.findAllPending(user.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    public void shouldFindByIdAndPurchaseUserId() {
        Installment installment = createInstallment(LocalDate.of(2026, 1, 10), InstallmentStatus.PENDING);

        Optional<Installment> result = installmentRepository.findByIdAndPurchaseUserId(
                installment.getId(), user.getId());

        assertThat(result).isPresent();
    }

    @Test
    public void shouldNotFindByIdWhenUserIdDoesNotMatch() {
        Installment installment = createInstallment(LocalDate.of(2026, 1, 10), InstallmentStatus.PENDING);

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setName("Maria");
        otherUser.setEmail("maria@email.com");
        otherUser.setPassword("12345");

        Optional<Installment> result = installmentRepository.findByIdAndPurchaseUserId(
                installment.getId(), otherUser.getId());

        assertThat(result).isEmpty();
    }
}
