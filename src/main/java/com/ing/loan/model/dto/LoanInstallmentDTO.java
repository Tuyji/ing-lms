package com.ing.loan.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanInstallmentDTO(
        Long installmentId,
        BigDecimal amount,
        BigDecimal paidAmount,
        LocalDate dueDate,
        LocalDate paymentDate,
        Boolean isPaid
) {}