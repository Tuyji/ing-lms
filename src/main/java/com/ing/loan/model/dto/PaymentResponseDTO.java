package com.ing.loan.model.dto;

import java.math.BigDecimal;

public record PaymentResponseDTO(
        int installmentsPaid,
        BigDecimal totalPaid,
        boolean isLoanFullyPaid
) {}