package com.appfinace.api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.FixedExpensePayment;
import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.fixed_expense.FixedExpense;
import com.appfinace.api.domain.transaction.Transaction;
import com.appfinace.api.domain.transaction.TransactionType;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.dto.fixed_expense.FixedExpensePaymentResponseDto;
import com.appfinace.api.dto.fixed_expense.FixedExpenseRequestDto;
import com.appfinace.api.dto.fixed_expense.FixedExpenseResponseDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.FixedExpensePaymentRepository;
import com.appfinace.api.repositories.FixedExpenseRepository;
import com.appfinace.api.repositories.TransactionRepository;
import com.appfinace.api.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class FixedExpenseService {

        private final FixedExpenseRepository fixedExpenseRepository;
        private final UserRepository userRepository;
        private final CategoryRepository categoryRepository;
        private final TransactionRepository transactionRepository;
        private final FixedExpensePaymentRepository fixedExpensePaymentRepository;

        public FixedExpenseService(
                        FixedExpenseRepository fixedExpenseRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository,
                        TransactionRepository transactionRepository,
                        FixedExpensePaymentRepository fixedExpensePaymentRepository) {
                this.fixedExpenseRepository = fixedExpenseRepository;
                this.userRepository = userRepository;
                this.categoryRepository = categoryRepository;
                this.transactionRepository = transactionRepository;
                this.fixedExpensePaymentRepository = fixedExpensePaymentRepository;
        }

        public void create(FixedExpenseRequestDto data, UUID userId) {
                User user = this.userRepository.findById(userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Usuário não localizado"));

                Category category = this.categoryRepository.findByIdAndUserId(data.categoryId(), userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Categoria não localizada"));

                FixedExpense fixedExpense = new FixedExpense();

                fixedExpense.setDescription(data.description());
                fixedExpense.setAmount(data.amount());
                fixedExpense.setDueDay(data.dueDay());
                fixedExpense.setActive(true);
                fixedExpense.setUser(user);
                fixedExpense.setCategory(category);

                this.fixedExpenseRepository.save(fixedExpense);
        }

        public List<FixedExpenseResponseDto> listByFiltred(
                        int page,
                        int size,
                        BigDecimal startAmout,
                        BigDecimal endAmout,
                        Integer startDueDay,
                        Integer endDueDay,
                        Boolean active,
                        UUID categoryId,
                        UUID userId) {
                Pageable pageable = PageRequest.of(page, size);

                Page<FixedExpense> fixedExpenseFiltred = this.fixedExpenseRepository.getFiltredFixedExpenses(userId,
                                categoryId,
                                startAmout, endAmout, startDueDay, endDueDay, active, pageable);

                return fixedExpenseFiltred.map(f -> new FixedExpenseResponseDto(
                                f.getId(),
                                f.getDescription(),
                                f.getAmount(),
                                f.getDueDay(),
                                f.getActive(),
                                new CategoryResponseDto(
                                                f.getCategory().getId(),
                                                f.getCategory().getName(),
                                                f.getCategory().getType())))
                                .toList();
        }

        public FixedExpenseResponseDto findById(UUID id, UUID userId) {
                FixedExpense fixedExpense = this.fixedExpenseRepository.findByIdAndUserId(id, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Despesa não localizada"));

                return new FixedExpenseResponseDto(
                                fixedExpense.getId(),
                                fixedExpense.getDescription(),
                                fixedExpense.getAmount(),
                                fixedExpense.getDueDay(),
                                fixedExpense.getActive(),
                                new CategoryResponseDto(
                                                fixedExpense.getCategory().getId(),
                                                fixedExpense.getCategory().getName(),
                                                fixedExpense.getCategory().getType()));
        }

        public void update(UUID id, FixedExpenseRequestDto data, UUID userId) {
                FixedExpense fixedExpense = this.fixedExpenseRepository.findByIdAndUserId(id, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Despesa não localizada"));

                Category category = this.categoryRepository.findByIdAndUserId(data.categoryId(), userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Categoria não localizada"));

                fixedExpense.setDescription(data.description());
                fixedExpense.setAmount(data.amount());
                fixedExpense.setDueDay(data.dueDay());
                fixedExpense.setCategory(category);

                this.fixedExpenseRepository.save(fixedExpense);
        }

        public void updateActive(UUID id, Boolean active, UUID userId) {
                FixedExpense fixedExpense = this.fixedExpenseRepository.findByIdAndUserId(id, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Despesa não localizada"));
                fixedExpense.setActive(active);

                this.fixedExpenseRepository.save(fixedExpense);
        }

        public void delete(UUID id, UUID userId) {
                FixedExpense fixedExpense = this.fixedExpenseRepository.findByIdAndUserId(id, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Despesa não localizada"));

                this.fixedExpenseRepository.delete(fixedExpense);
        }

        @Transactional
        public void markAsPaid(UUID id, Integer month, Integer year, UUID userId) {
                FixedExpense fixedExpense = this.fixedExpenseRepository.findByIdAndUserId(id, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Despesa não localizada"));

                if (!fixedExpense.getActive()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Despesa já está inativa");
                }

                Boolean alreadyPaid = fixedExpensePaymentRepository.existsByFixedExpenseIdAndMonthAndYear(id, month,
                                year);

                if (alreadyPaid) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Despesa já paga neste mês");
                }

                FixedExpensePayment expensePayment = new FixedExpensePayment();
                expensePayment.setMonth(month);
                expensePayment.setYear(year);
                expensePayment.setPaidAt(LocalDate.now());
                expensePayment.setFixedExpense(fixedExpense);
                fixedExpensePaymentRepository.save(expensePayment);

                fixedExpense.setActive(false);
                this.fixedExpenseRepository.save(fixedExpense);

                Transaction transaction = new Transaction();
                transaction.setDescription(fixedExpense.getDescription());
                transaction.setAmount(fixedExpense.getAmount());
                transaction.setDate(LocalDate.now());
                transaction.setType(TransactionType.EXPENSE);
                transaction.setUser(fixedExpense.getUser());
                transaction.setCategory(fixedExpense.getCategory());

                this.transactionRepository.save(transaction);
        }

        public List<FixedExpensePaymentResponseDto> listPayments(UUID fixedExpenseId, UUID userId) {
                fixedExpenseRepository.findByIdAndUserId(fixedExpenseId, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "Despesa não localizada"));

                List<FixedExpensePayment> payments = fixedExpensePaymentRepository
                                .findByFixedExpenseIdOrderByYearDescMonthDesc(fixedExpenseId);

                return payments.stream().map(p -> new FixedExpensePaymentResponseDto(
                                p.getId(),
                                p.getMonth(),
                                p.getYear(),
                                p.getPaidAt())).toList();
        }
}
