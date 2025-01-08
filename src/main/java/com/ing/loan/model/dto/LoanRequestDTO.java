package com.ing.loan.model.dto;

import java.math.BigDecimal;

public record LoanRequestDTO(
        Long customerId,
        BigDecimal amount,
        Double interestRate,
        Integer numberOfInstallments
) {
}