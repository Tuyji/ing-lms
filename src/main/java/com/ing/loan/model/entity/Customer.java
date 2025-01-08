package com.ing.loan.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Customer {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String surname;
    private BigDecimal creditLimit; // The maximum amount of credit the customer can use
    private BigDecimal usedCreditLimit = BigDecimal.ZERO; // The amount of credit the customer has already used
}