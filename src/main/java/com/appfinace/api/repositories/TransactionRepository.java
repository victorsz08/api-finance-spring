package com.appfinace.api.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.appfinace.api.domain.transaction.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @EntityGraph
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND " +
            "t.date BETWEEN :startDate AND :endDate ORDER BY t.date DESC")
    public List<Transaction> findByUserAndMonth(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND " +
            "t.type = 'INCOME' AND t.date BETWEEN :startDate AND :endDate")
    public BigDecimal sumIncomeByMonth(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND " +
            "t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate")
    public BigDecimal sumExpenseByMonth(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
