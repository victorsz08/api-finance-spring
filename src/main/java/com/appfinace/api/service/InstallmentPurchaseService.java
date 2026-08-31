package com.appfinace.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.installment.Installment;
import com.appfinace.api.domain.installment.InstallmentPurchase;
import com.appfinace.api.domain.installment.InstallmentStatus;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.dto.installment.InstallmentPurchaseRequestDto;
import com.appfinace.api.dto.installment.InstallmentPurchaseResponseDto;
import com.appfinace.api.dto.installment.InstallmentResponseDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.InstallmentPurchaseRepository;
import com.appfinace.api.repositories.InstallmentRepository;
import com.appfinace.api.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class InstallmentPurchaseService {

    private final InstallmentPurchaseRepository purchaseRepository;
    private final InstallmentRepository installmentRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public InstallmentPurchaseService(
            InstallmentPurchaseRepository purchaseRepository,
            InstallmentRepository installmentRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.purchaseRepository = purchaseRepository;
        this.installmentRepository = installmentRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(InstallmentPurchaseRequestDto data, UUID userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não localizado"));

        Category category = this.categoryRepository.findById(data.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada"));

        InstallmentPurchase purchase = new InstallmentPurchase();
        purchase.setDescription(data.description());
        purchase.setTotalAmount(data.totalAmount());
        purchase.setPurchaseDate(data.purchaseDate());
        purchase.setTotalInstallments(data.totalInstallments());
        purchase.setUser(user);
        purchase.setCategory(category);

        this.purchaseRepository.save(purchase);

        this.generateInstallments(purchase);
    }

    private void generateInstallments(InstallmentPurchase purchase) {
        int total = purchase.getTotalInstallments();
        BigDecimal baseAmount = purchase.getTotalAmount()
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);

        BigDecimal accumulated = BigDecimal.ZERO;

        for (int i = 1; i <= total; i++) {
            BigDecimal amount = (i < total)
                    ? baseAmount
                    : purchase.getTotalAmount().subtract(accumulated);

            Installment installment = new Installment();

            installment.setNumber(i);
            installment.setAmount(amount);
            installment.setDueDate(purchase.getPurchaseDate().plusMonths(i));
            installment.setStatus(InstallmentStatus.PENDING);
            installment.setPurchase(purchase);

            this.installmentRepository.save(installment);

            accumulated = accumulated.add(amount);
        }
    }

    public List<InstallmentPurchaseResponseDto> listByUser(
            int page,
            int size,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            UUID userId,
            Boolean onlyOpen) {
        Pageable pageable = PageRequest.of(page, size);

        Page<InstallmentPurchase> data = this.purchaseRepository.getPurcharsesFiltred(
                userId, startDate, endDate, categoryId, onlyOpen, pageable);

        return data.map(p -> this.toResponseDto(p)).stream().toList();
    }

    public InstallmentPurchaseResponseDto findOne(UUID id) {
        InstallmentPurchase purchase = this.purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"));

        return this.toResponseDto(purchase);
    }

    @Transactional
    public void delete(UUID id) {
        InstallmentPurchase purchase = this.purchaseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Despesa não localizada"));

        this.purchaseRepository.delete(purchase);
    }

    private InstallmentPurchaseResponseDto toResponseDto(InstallmentPurchase purchase) {
        List<InstallmentResponseDto> installments = this.installmentRepository.findByPurchaseId(purchase.getId())
                .stream()
                .map(i -> new InstallmentResponseDto(
                        i.getId(),
                        i.getNumber(),
                        i.getAmount(),
                        i.getDueDate(),
                        i.getStatus()))
                .toList();

        return new InstallmentPurchaseResponseDto(
                purchase.getId(),
                purchase.getDescription(),
                purchase.getTotalAmount(),
                purchase.getTotalInstallments(),
                purchase.getPurchaseDate(),
                new CategoryResponseDto(
                        purchase.getCategory().getId(),
                        purchase.getCategory().getName(),
                        purchase.getCategory().getType()),
                installments);
    }
}
