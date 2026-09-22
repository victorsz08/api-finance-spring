package com.appfinace.api.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
public class InstallmentPurchaseRepositoryTest {

    @Autowired
    private InstallmentPurchaseRepository purchaseRepository;

    @Autowired
    private InstallmentRepository installmentRepository;

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
        category.setName("Eletronicos");
        category.setType("EXPENSE");
        category.setUser(user);
        categoryRepository.save(category);

        otherCategory = new Category();
        otherCategory.setName("Viagem");
        otherCategory.setType("EXPENSE");
        otherCategory.setUser(user);
        categoryRepository.save(otherCategory);
    }

    private InstallmentPurchase createPurchase(User owner, Category cat, String description,
            BigDecimal totalAmount, LocalDate purchaseDate, int totalInstallments) {
        InstallmentPurchase purchase = new InstallmentPurchase();
        purchase.setDescription(description);
        purchase.setTotalAmount(totalAmount);
        purchase.setPurchaseDate(purchaseDate);
        purchase.setTotalInstallments(totalInstallments);
        purchase.setUser(owner);
        purchase.setCategory(cat);
        return purchaseRepository.save(purchase);
    }

    private void createInstallment(InstallmentPurchase purchase, int number, InstallmentStatus status) {
        Installment installment = new Installment();
        installment.setNumber(number);
        installment.setAmount(new BigDecimal("100.00"));
        installment.setDueDate(purchase.getPurchaseDate().plusMonths(number));
        installment.setStatus(status);
        installment.setPurchase(purchase);
        installmentRepository.save(installment);
    }

    @Test
    public void shouldReturnPurchasesByUserId() {
        createPurchase(user, category, "Notebook", new BigDecimal("3000.00"), LocalDate.of(2026, 1, 10), 3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<InstallmentPurchase> result = purchaseRepository.getPurcharsesFiltred(
                user.getId(), null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Notebook");
    }

    @Test
    public void shouldNotReturnPurchasesFromOtherUsers() {
        createPurchase(user, category, "Notebook", new BigDecimal("3000.00"), LocalDate.of(2026, 1, 10), 3);
        createPurchase(otherUser, category, "Celular", new BigDecimal("2000.00"), LocalDate.of(2026, 1, 10), 3);

        Pageable pageable = PageRequest.of(0, 10);

        Page<InstallmentPurchase> result = purchaseRepository.getPurcharsesFiltred(
                user.getId(), null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Notebook");
    }

    @Test
    public void shouldFilterByCategoryId() {
        createPurchase(user, category, "Notebook", new BigDecimal("3000.00"), LocalDate.of(2026, 1, 10), 3);
        createPurchase(user, otherCategory, "Celular", new BigDecimal("2000.00"), LocalDate.of(2026, 1, 10), 3);

        Pageable pageable = PageRequest.of(0, 10);
        Page<InstallmentPurchase> result = purchaseRepository.getPurcharsesFiltred(
                user.getId(), null, null, category.getId(), null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Notebook");
    }

    @Test
    public void shouldFilterByDateRange() {
        createPurchase(user, category, "Compra janeiro", new BigDecimal("1000.00"), LocalDate.of(2026, 1, 15), 2);
        createPurchase(user, category, "Compra março", new BigDecimal("1000.00"), LocalDate.of(2026, 3, 15), 2);
        createPurchase(user, category, "Compra maio", new BigDecimal("1000.00"), LocalDate.of(2026, 5, 15), 2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<InstallmentPurchase> result = purchaseRepository.getPurcharsesFiltred(
                user.getId(), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 30), null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Compra março");
    }

    @Test
    public void shouldExcludeFullyPaidPurchasesWhenOnlyOpenIsTrue() {
        InstallmentPurchase purchasePaid = createPurchase(
                user, category, "Quitada", new BigDecimal("200.00"), LocalDate.of(2026, 1, 10), 2);
        createInstallment(purchasePaid, 1, InstallmentStatus.PAID);
        createInstallment(purchasePaid, 2, InstallmentStatus.PAID);

        InstallmentPurchase purchaseIsOpen = createPurchase(
                user, category, "Em aberto", new BigDecimal("300.00"), LocalDate.of(2026, 1, 10), 3);
        createInstallment(purchaseIsOpen, 1, InstallmentStatus.PAID);
        createInstallment(purchaseIsOpen, 2, InstallmentStatus.PENDING);
        createInstallment(purchaseIsOpen, 3, InstallmentStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<InstallmentPurchase> result = purchaseRepository.getPurcharsesFiltred(
                user.getId(), null, null, null, true, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).isEqualTo("Em aberto");
    }

    @Test
    public void shouldReturnAllPurchasesWhenOnlyOpenIsFalse() {
        InstallmentPurchase purchasePaid = createPurchase(
                user, category, "Quitada", new BigDecimal("200.00"), LocalDate.of(2026, 1, 10), 1);
        createInstallment(purchasePaid, 1, InstallmentStatus.PAID);

        InstallmentPurchase purchaseIsOpen = createPurchase(
                user, category, "Em aberto", new BigDecimal("300.00"), LocalDate.of(2026, 1, 10), 1);
        createInstallment(purchaseIsOpen, 1, InstallmentStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<InstallmentPurchase> result = purchaseRepository.getPurcharsesFiltred(
                user.getId(), null, null, null, false, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    public void shouldReturnAllPurchasesWhenOnlyOpenIsNull() {
        InstallmentPurchase purchasePaid = createPurchase(
                user, category, "Quitada", new BigDecimal("200.00"), LocalDate.of(2026, 1, 10), 1);
        createInstallment(purchasePaid, 1, InstallmentStatus.PAID);

        InstallmentPurchase purchaseIsOpen = createPurchase(
                user, category, "Em aberto", new BigDecimal("300.00"), LocalDate.of(2026, 1, 10), 1);
        createInstallment(purchaseIsOpen, 1, InstallmentStatus.PENDING);

        Pageable pageable = PageRequest.of(0, 10);
        Page<InstallmentPurchase> result = purchaseRepository.getPurcharsesFiltred(
                user.getId(), null, null, null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}
