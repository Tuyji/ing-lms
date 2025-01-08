package com.ing.loan.repository;

import com.ing.loan.model.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {

    List<LoanInstallment> findByLoanId(Long loanId);
    List<LoanInstallment> findByLoanIdAndIsPaidFalse(Long loanId);

}
