package com.appfinace.api.repositories;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.appfinace.api.domain.fixed_expense.FixedExpense;

public interface FixedExpenseRepository extends JpaRepository<FixedExpense, UUID> {

        @Query("SELECT f FROM FixedExpense f " +
                        "WHERE f.id = :id AND f.user.id = :userId")
        public Optional<FixedExpense> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

        @Query("SELECT f FROM FixedExpense f " +
                        "JOIN FETCH f.category c " +
                        "WHERE f.user.id = :userId AND " +
                        "(:startAmount IS NULL OR f.amount >= :startAmount) AND " +
                        "(:endAmount IS NULL OR f.amount <= :endAmount) AND " +
                        "(:startDueDay IS NULL OR f.dueDay >= :startDueDay) AND " +
                        "(:endDueDay IS NULL OR f.dueDay <= :endDueDay) AND " +
                        "(:active IS NULL OR f.active = :active) AND " +
                        "(:categoryId IS NULL OR f.category.id = :categoryId)")
        public Page<FixedExpense> getFiltredFixedExpenses(
                        @Param("userId") UUID userId,
                        @Param("categoryId") UUID categoryId,
                        @Param("startAmount") BigDecimal startAmount,
                        @Param("endAmount") BigDecimal endAmount,
                        @Param("startDueDay") Integer startDueDay,
                        @Param("endDueDay") Integer endDueDay,
                        @Param("active") Boolean active,
                        Pageable pageable);

}
