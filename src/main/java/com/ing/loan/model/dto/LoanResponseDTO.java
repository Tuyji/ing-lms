package com.ing.loan.model.dto;

import java.math.BigDecimal;

public record LoanResponseDTO (
        Long loanId,
        BigDecimal totalAmount,
        Boolean isPaid
) {}