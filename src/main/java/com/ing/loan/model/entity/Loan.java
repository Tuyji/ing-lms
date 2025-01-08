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
public class Loan {
    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Customer customer;
    private BigDecimal loanAmount; // The amount of money the customer borrowed
    private Integer numberOfInstallment; // The number of installments the loan will be paid in
    private LocalDate createDate;
    private Boolean isPaid = false; // Indicates whether the loan has been paid back
}