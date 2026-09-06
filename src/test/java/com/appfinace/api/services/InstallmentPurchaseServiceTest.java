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
import com.appfinace.api.domain.installment.Installment;
import com.appfinace.api.domain.installment.InstallmentPurchase;
import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.installment.InstallmentPurchaseRequestDto;
import com.appfinace.api.dto.installment.InstallmentPurchaseResponseDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.InstallmentPurchaseRepository;
import com.appfinace.api.repositories.InstallmentRepository;
import com.appfinace.api.repositories.UserRepository;
import com.appfinace.api.service.InstallmentPurchaseService;

@ExtendWith(MockitoExtension.class)
public class InstallmentPurchaseServiceTest {

        @Mock
        private InstallmentPurchaseRepository purchaseRepository;

        @Mock
        private InstallmentRepository installmentRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private CategoryRepository categoryRepository;

        @InjectMocks
        private InstallmentPurchaseService purchaseService;

        private UUID purchaseId;
        private UUID userId;
        private UUID categoryId;
        private InstallmentPurchase existingsPurchase;
        private User existingsUser;
        private Category existingsCategory;

        @BeforeEach
        public void setUp() {
                purchaseId = UUID.randomUUID();
                userId = UUID.randomUUID();
                categoryId = UUID.randomUUID();

                existingsUser = new User();
                existingsUser.setId(userId);

                existingsCategory = new Category();
                existingsCategory.setId(categoryId);
                existingsCategory.setName("Eletronicos");
                existingsCategory.setType("EXPENSE");

                existingsPurchase = new InstallmentPurchase();
                existingsPurchase.setId(purchaseId);
                existingsPurchase.setDescription("Notebook");
                existingsPurchase.setTotalAmount(new BigDecimal("2500.00"));
                existingsPurchase.setPurchaseDate(LocalDate.of(2026, 10, 9));
                existingsPurchase.setTotalInstallments(10);
                existingsPurchase.setCategory(existingsCategory);
                existingsPurchase.setUser(existingsUser);
        }

        @Test
        public void shouldCreateInstallmentPurchaseSuccessfully() {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "Notebook", new BigDecimal("100.00"), 3, LocalDate.of(2026, 1, 10), categoryId);

                when(userRepository.findById(userId)).thenReturn(Optional.of(existingsUser));
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingsCategory));

                purchaseService.create(dto, userId);

                ArgumentCaptor<InstallmentPurchase> purchaseCaptor = ArgumentCaptor.forClass(InstallmentPurchase.class);
                verify(purchaseRepository).save(purchaseCaptor.capture());

                InstallmentPurchase purchase = purchaseCaptor.getValue();
                assertThat(purchase.getDescription()).isEqualTo("Notebook");
                assertThat(purchase.getTotalAmount()).isEqualByComparingTo("100.00");
                assertThat(purchase.getPurchaseDate()).isEqualTo(LocalDate.of(2026, 1, 10));
                assertThat(purchase.getTotalInstallments()).isEqualTo(3);
                assertThat(purchase.getCategory().getName()).isEqualTo("Eletronicos");
                assertThat(purchase.getUser()).isEqualTo(existingsUser);

                ArgumentCaptor<Installment> installmentCaptor = ArgumentCaptor.forClass(Installment.class);
                verify(installmentRepository, times(3)).save(installmentCaptor.capture());

                List<Installment> installments = installmentCaptor.getAllValues();

                assertThat(installments.get(0).getAmount()).isEqualByComparingTo("33.33");
                assertThat(installments.get(0).getNumber()).isEqualTo(1);
                assertThat(installments.get(0).getDueDate()).isEqualTo(LocalDate.of(2026, 2, 10));
                assertThat(installments.get(0).getStatus()).isEqualTo(InstallmentStatus.PENDING);

                assertThat(installments.get(1).getNumber()).isEqualTo(2);
                assertThat(installments.get(1).getAmount()).isEqualByComparingTo("33.33");
                assertThat(installments.get(1).getDueDate()).isEqualTo(LocalDate.of(2026, 3, 10));

                assertThat(installments.get(2).getNumber()).isEqualTo(3);
                assertThat(installments.get(2).getAmount()).isEqualByComparingTo("33.34");
                assertThat(installments.get(2).getDueDate()).isEqualTo(LocalDate.of(2026, 4, 10));

                @SuppressWarnings("null")
                BigDecimal sum = installments.stream().map(Installment::getAmount).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                assertThat(sum).isEqualByComparingTo("100.00");
        }

        @Test
        public void shouldThrowNotFoundWhenUserNotFoundOnCreate() {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "Notebook", new BigDecimal("100.00"), 3, LocalDate.of(2026, 1, 10), categoryId);

                when(userRepository.findById(userId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> purchaseService.create(dto, userId))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Usuário não localizado");

                verify(purchaseRepository, never()).save(any());
                verify(installmentRepository, never()).save(any());
        }

        @Test
        public void shouldThrowNotFoundWhenCategoryNotFoundOnCreate() {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "Notebook", new BigDecimal("100.00"), 3, LocalDate.of(2026, 1, 10), categoryId);

                when(userRepository.findById(userId)).thenReturn(Optional.of(existingsUser));
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> purchaseService.create(dto, userId))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Categoria não localizada");

                verify(purchaseRepository, never()).save(any());
                verify(installmentRepository, never()).save(any());
        }

        @Test
        public void shouldGenerateSingleInstallmentWhenTotalInstallmentsIsOne() {
                InstallmentPurchaseRequestDto dto = new InstallmentPurchaseRequestDto(
                                "Fone de Ouvido", new BigDecimal("250.00"), 1, LocalDate.of(2026, 1, 10), categoryId);

                when(userRepository.findById(userId)).thenReturn(Optional.of(existingsUser));
                when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingsCategory));

                purchaseService.create(dto, userId);

                ArgumentCaptor<Installment> installmentCaptor = ArgumentCaptor.forClass(Installment.class);
                verify(installmentRepository, times(1)).save(installmentCaptor.capture());

                Installment installment = installmentCaptor.getValue();
                assertThat(installment.getAmount()).isEqualByComparingTo("250.00");
                assertThat(installment.getDueDate()).isEqualTo(LocalDate.of(2026, 2, 10));
                assertThat(installment.getNumber()).isEqualTo(1);
        }

        @Test
        public void shouldListPurchasesByUserSuccessfully() {
                Pageable pageable = PageRequest.of(0, 10);

                Page<InstallmentPurchase> page = new PageImpl<>(List.of(existingsPurchase), pageable, 1);

                when(purchaseRepository.getPurcharsesFiltred(userId, null, null, null, null, pageable))
                                .thenReturn(page);
                when(installmentRepository.findByPurchaseId(purchaseId)).thenReturn(List.of());

                List<InstallmentPurchaseResponseDto> result = purchaseService.listByUser(
                                0, 10, null, null, null, userId, null);

                assertThat(result).hasSize(1);
                assertThat(result.get(0).description()).isEqualTo("Notebook");
                assertThat(result.get(0).category().name()).isEqualTo("Eletronicos");
        }

        @Test
        public void shouldReturnEmptyListWhenUserHasNoPurchases() {
                Pageable pageable = PageRequest.of(0, 10);
                Page<InstallmentPurchase> emptyPage = new PageImpl<>(List.of(), pageable, 0);

                when(purchaseRepository.getPurcharsesFiltred(userId, null, null, null, null, pageable))
                                .thenReturn(emptyPage);

                List<InstallmentPurchaseResponseDto> result = purchaseService.listByUser(
                                0, 10, null, null, null, userId, null);

                assertThat(result).isEmpty();
        }

        @Test
        public void shouldFindPurchaseByIdSuccessfully() {
                when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(existingsPurchase));
                when(installmentRepository.findByPurchaseId(purchaseId)).thenReturn(List.of());

                InstallmentPurchaseResponseDto purchase = purchaseService.findOne(purchaseId);

                assertThat(purchase.description()).isEqualTo("Notebook");
                assertThat(purchase.totalAmount()).isEqualByComparingTo("2500.00");
                assertThat(purchase.totalInstallments()).isEqualTo(10);
                assertThat(purchase.category().name()).isEqualTo("Eletronicos");
        }

        @Test
        public void shouldThrowNotFoundWhenPurchaseNotFoundOnFindOne() {
                when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> purchaseService.findOne(purchaseId))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Despesa não localizada");
        }

        @Test
        public void shouldDeletePurchaseSuccessfully() {
                when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(existingsPurchase));

                purchaseService.delete(purchaseId);

                verify(purchaseRepository, times(1)).delete(existingsPurchase);
        }

        @Test
        public void shouldThrowNotFoundWhenPurchaseNotFoundOnDelete() {
                when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> purchaseService.delete(purchaseId))
                                .isInstanceOf(ResponseStatusException.class)
                                .hasMessageContaining("Despesa não localizada");

                verify(purchaseRepository, never()).delete(any());
        }
}
