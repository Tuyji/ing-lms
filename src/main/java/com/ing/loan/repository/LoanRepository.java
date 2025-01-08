package com.ing.loan.repository;

import com.ing.loan.model.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByCustomerId(Long customerId);

    @Query("SELECT l FROM Loan l WHERE l.customer.id = :customerId " +
            "AND (:isPaid IS NULL OR l.isPaid = :isPaid) " +
            "AND (:numberOfInstallments IS NULL OR l.numberOfInstallment = :numberOfInstallments)")
    List<Loan> findAllByCustomerIdAndFilters(
            @Param("customerId") Long customerId,
            @Param("isPaid") Boolean isPaid,
            @Param("numberOfInstallments") Integer numberOfInstallments
    );
}
