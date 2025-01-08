package com.ing.loan.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentDTO(
        Long installmentId,
        BigDecimal amount,
        LocalDate dueDate,
        Boolean isPaid
) {
}