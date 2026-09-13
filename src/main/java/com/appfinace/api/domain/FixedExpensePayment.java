package com.appfinace.api.domain;

import java.time.LocalDate;
import java.util.UUID;

import com.appfinace.api.domain.fixed_expense.FixedExpense;

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
@Table(name = "fixed_expense_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FixedExpensePayment {

    @Id
    @GeneratedValue
    private UUID id;

    private Integer month;

    private Integer year;

    private LocalDate paidAt;

    @ManyToOne
    @JoinColumn(name = "fixed_expense_id")
    private FixedExpense fixedExpense;
}
