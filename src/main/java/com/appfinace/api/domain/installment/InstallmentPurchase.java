package com.appfinace.api.domain.installment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "installment_purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPurchase {

    @Id
    @GeneratedValue
    private UUID id;

    private String description;

    private BigDecimal totalAmount;

    private Integer totalInstallments;

    private LocalDate purchaseDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
