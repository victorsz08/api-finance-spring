package com.appfinace.api.domain.fixed_expense;

import java.math.BigDecimal;
import java.util.UUID;

import com.appfinace.api.domain.category.Category;
import com.appfinace.api.domain.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fixed_expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FixedExpense {

    @Id
    @GeneratedValue
    private UUID id;

    private String description;

    private BigDecimal amount;

    private Integer dueDay;

    private Boolean active;

    @OneToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
