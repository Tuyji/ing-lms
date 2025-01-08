package com.ing.loan.model.dto;

import java.math.BigDecimal;

public record LoanSummaryDTO (
        Long loanId,
        BigDecimal loanAmount,
        Boolean isPaid,
        Integer numberOfInstallments
) {}