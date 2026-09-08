package com.appfinace.api.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import com.appfinace.api.domain.installment.Installment;
import com.appfinace.api.domain.installment.InstallmentPurchase;
import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.domain.transaction.Transaction;
import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.installment.InstallmentResponseDto;
import com.appfinace.api.repositories.InstallmentRepository;
import com.appfinace.api.repositories.TransactionRepository;
import com.appfinace.api.service.InstallmentService;

@ExtendWith(MockitoExtension.class)
public class InstallmentServiceTest {

    @Mock
    private InstallmentRepository installmentRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private InstallmentService installmentService;

    private UUID installmentId;
    private UUID userId;
    private User existingUser;
    private Category existingCategory;
    private InstallmentPurchase existingPurchase;
    private Installment existingInstallment;

    @BeforeEach
    public void setUp() {
        userId = UUID.randomUUID();
        installmentId = UUID.randomUUID();

        existingUser = new User();
        existingUser.setId(userId);

        existingCategory = new Category();
        existingCategory.setId(UUID.randomUUID());
        existingCategory.setName("Eletrônicos");
        existingCategory.setType("EXPENSE");

        existingPurchase = new InstallmentPurchase();
        existingPurchase.setId(UUID.randomUUID());
        existingPurchase.setDescription("Notebook");
        existingPurchase.setTotalAmount(new BigDecimal("3000.00"));
        existingPurchase.setTotalInstallments(3);
        existingPurchase.setPurchaseDate(LocalDate.of(2026, 1, 10));
        existingPurchase.setUser(existingUser);
        existingPurchase.setCategory(existingCategory);

        existingInstallment = new Installment();
        existingInstallment.setId(installmentId);
        existingInstallment.setNumber(1);
        existingInstallment.setAmount(new BigDecimal("1000.00"));
        existingInstallment.setDueDate(LocalDate.of(2026, 2, 10));
        existingInstallment.setStatus(InstallmentStatus.PENDING);
        existingInstallment.setPurchase(existingPurchase);
    }

    @Test
    public void shouldPayInstallmentSuccessfully() {
        when(installmentRepository.findByIdAndPurchaseUserId(installmentId, userId))
                .thenReturn(Optional.of(existingInstallment));

        installmentService.payInstallment(installmentId, userId);

        ArgumentCaptor<Installment> installmentCaptor = ArgumentCaptor.forClass(Installment.class);
        verify(installmentRepository).save(installmentCaptor.capture());
        assertThat(installmentCaptor.getValue().getStatus()).isEqualTo(InstallmentStatus.PAID);

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getDescription()).isEqualTo("Notebook - parcela 1/3");
        assertThat(savedTransaction.getAmount()).isEqualByComparingTo("1000");
        assertThat(savedTransaction.getDate()).isEqualTo(LocalDate.now());
        assertThat(savedTransaction.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(savedTransaction.getInstallment()).isEqualTo(existingInstallment);
        assertThat(savedTransaction.getCategory()).isEqualTo(existingCategory);
        assertThat(savedTransaction.getUser()).isEqualTo(existingUser);
    }

    @Test
    public void shouldThrowNotFoundWhenInstallmentNotFoundOnPay() {
        when(installmentRepository.findByIdAndPurchaseUserId(installmentId, userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> installmentService.payInstallment(installmentId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Parcela não localizada");

        verify(installmentRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    public void shouldThrowConflictWhenInstallmentAlreadyPaid() {
        existingInstallment.setStatus(InstallmentStatus.PAID);
        when(installmentRepository.findByIdAndPurchaseUserId(installmentId, userId))
                .thenReturn(Optional.of(existingInstallment));

        assertThatThrownBy(() -> installmentService.payInstallment(installmentId, userId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Parcela já está paga");

        verify(installmentRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    public void shouldListPendingInstallmentsByMonthAndYear() {
        when(installmentRepository.findPendingByMonth(userId, 2, 2026))
                .thenReturn(List.of(existingInstallment));

        List<InstallmentResponseDto> result = installmentService.listPending(userId, 2, 2026);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).number()).isEqualTo(1);
        assertThat(result.get(0).amount()).isEqualByComparingTo("1000.00");
        assertThat(result.get(0).status()).isEqualTo(InstallmentStatus.PENDING);

        verify(installmentRepository, never()).findAllPending(any());
    }

    @Test
    public void shouldListAllPendingInstallmentsWhenMonthAndYearAreNull() {
        when(installmentRepository.findAllPending(userId))
                .thenReturn(List.of(existingInstallment));

        List<InstallmentResponseDto> result = installmentService.listPending(userId, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(InstallmentStatus.PENDING);

        verify(installmentRepository, never()).findPendingByMonth(any(), anyInt(), anyInt());
    }

    @Test
    public void shouldReturnEmptyListWhenNoPendingInstallments() {
        when(installmentRepository.findAllPending(userId))
                .thenReturn(List.of());

        List<InstallmentResponseDto> result = installmentService.listPending(userId, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowBadRequestWhenOnlyMonthProvided() {
        assertThatThrownBy(() -> installmentService.listPending(userId, 3, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Informe mês e ano juntos, ou nenhum dos dois");

        verify(installmentRepository, never()).findPendingByMonth(any(), anyInt(), anyInt());
        verify(installmentRepository, never()).findAllPending(any());
    }

    @Test
    void shouldThrowBadRequestWhenOnlyYearProvided() {
        assertThatThrownBy(() -> installmentService.listPending(userId, null, 2026))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Informe mês e ano juntos, ou nenhum dos dois");

        verify(installmentRepository, never()).findPendingByMonth(any(), anyInt(), anyInt());
        verify(installmentRepository, never()).findAllPending(any());
    }
}
