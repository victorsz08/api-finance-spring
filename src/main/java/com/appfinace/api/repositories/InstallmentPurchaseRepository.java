package com.appfinace.api.repositories;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.appfinace.api.domain.installment.InstallmentPurchase;

public interface InstallmentPurchaseRepository extends JpaRepository<InstallmentPurchase, UUID> {
        @Query("SELECT p FROM InstallmentPurchase p " +
                        "JOIN FETCH p.category c WHERE p.user.id = :userId AND " +
                        "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
                        "(:startDate IS NULL OR p.purchaseDate >= :startDate) AND " +
                        "(:endDate IS NULL OR p.purchaseDate <= :endDate) AND " +
                        "(:onlyOpen IS NULL OR :onlyOpen = false OR EXISTS (" +
                        " SELECT 1 FROM Installment i WHERE i.purchase = p AND i.status != 'PAID'" +
                        "))")
        public Page<InstallmentPurchase> getPurcharsesFiltred(
                        @Param("userId") UUID userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("categoryId") UUID categoryId,
                        @Param("onlyOpen") Boolean onlyOpen,
                        Pageable pageable);
}
