package com.appfinace.api.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appfinace.api.domain.FixedExpensePayment;

public interface FixedExpensePaymentRepository extends JpaRepository<FixedExpensePayment, UUID> {
    public boolean existsByFixedExpenseIdAndMonthAndYear(UUID fixedExpenseId, Integer month, Integer year);

    public List<FixedExpensePayment> findByFixedExpenseIdOrderByYearDescMonthDesc(UUID fixedExpenseId);
}
