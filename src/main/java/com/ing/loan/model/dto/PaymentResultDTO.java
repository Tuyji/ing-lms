package com.ing.loan.model.dto;

import java.math.BigDecimal;

public record PaymentResultDTO (
        BigDecimal totalAmountPaid,
        Integer installmentsPaid,
        Boolean loanFullyPaid
) {
}