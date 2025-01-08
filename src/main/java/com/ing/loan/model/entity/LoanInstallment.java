package com.ing.loan.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Data
public class LoanInstallment {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Loan loan; // The loan this installment belongs to
    private BigDecimal amount; // The amount of money the customer has to pay for this installment
    private BigDecimal paidAmount = BigDecimal.ZERO; // The amount of money the customer has already paid for this installment
    private LocalDate dueDate; // The date by which the installment should be paid
    private LocalDate paymentDate; // The date when the installment was paid
    private Boolean isPaid = false; // Indicates whether the installment has been paid
}