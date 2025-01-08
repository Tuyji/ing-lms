package com.ing.loan.model.dto;

import java.math.BigDecimal;

public record PaymentRequestDTO (
        BigDecimal amount
) {}