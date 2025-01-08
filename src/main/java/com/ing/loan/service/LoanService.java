package com.ing.loan.service;

import com.ing.loan.model.dto.*;

import java.math.BigDecimal;
import java.util.List;

public interface LoanService {

    LoanResponseDTO createLoan(LoanRequestDTO loanRequestDTO);

    List<LoanSummaryDTO> listLoans(Long customerId, Boolean isPaid, Integer numberOfInstallments);

    List<LoanInstallmentDTO> listInstallments(Long loanId);

    PaymentResponseDTO payLoan(Long loanId, BigDecimal paymentAmount);
}
