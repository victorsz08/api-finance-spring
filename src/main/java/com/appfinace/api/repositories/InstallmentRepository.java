package com.appfinace.api.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.appfinace.api.domain.installment.Installment;

public interface InstallmentRepository extends JpaRepository<Installment, UUID> {
    public List<Installment> findByPurchaseId(UUID purchaseId);

    @Query("SELECT i FROM Installment i WHERE i.purchase.user.id = :userId AND i.status = 'PENDING'")
    List<Installment> findAllPending(@Param("userId") UUID userId);

    @Query("SELECT i FROM Installment i WHERE i.purchase.user.id = :userId AND i.status = 'PENDING' " +
            "AND EXTRACT(MONTH FROM i.dueDate) = :month AND EXTRACT(YEAR FROM i.dueDate) = :year")
    List<Installment> findPendingByMonth(@Param("userId") UUID userId, @Param("month") int month,
            @Param("year") int year);
}
