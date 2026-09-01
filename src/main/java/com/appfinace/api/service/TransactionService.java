package com.appfinace.api.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.transaction.Transaction;
import com.appfinace.api.domain.user.User;
import com.appfinace.api.dto.category.CategoryResponseDto;
import com.appfinace.api.dto.transaction.MonthlySumaryResponseDto;
import com.appfinace.api.dto.transaction.TransactionRequestDto;
import com.appfinace.api.dto.transaction.TransactionResponseDto;
import com.appfinace.api.repositories.CategoryRepository;
import com.appfinace.api.repositories.TransactionRepository;
import com.appfinace.api.repositories.UserRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository,
            CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public void create(TransactionRequestDto data, UUID userId) {
        Category category = this.categoryRepository.findById(data.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não localizada"));

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não localizado"));

        Transaction transaction = new Transaction();

        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAmount(data.amount());
        transaction.setDate(data.date());
        transaction.setType(data.type());
        transaction.setDescription(data.description());

        this.transactionRepository.save(transaction);
    }

    public List<TransactionResponseDto> listByMonth(int month, int year, UUID userId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = this.transactionRepository.findByUserAndMonth(userId, startDate, endDate);

        return transactions.stream()
                .map(t -> new TransactionResponseDto(
                        t.getId(),
                        t.getDescription(),
                        t.getAmount(),
                        t.getType(),
                        t.getDate(),
                        new CategoryResponseDto(
                                t.getCategory().getId(),
                                t.getCategory().getName(),
                                t.getCategory().getType())))
                .toList();
    }

    public MonthlySumaryResponseDto getMonthySumary(int month, int year, UUID userId) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        var totalIncome = this.transactionRepository.sumIncomeByMonth(userId, startDate, endDate);
        var totalExpense = this.transactionRepository.sumExpenseByMonth(userId, startDate, endDate);
        var balance = totalIncome.subtract(totalExpense);

        return new MonthlySumaryResponseDto(month, year, totalIncome, totalExpense, balance);
    }
}
