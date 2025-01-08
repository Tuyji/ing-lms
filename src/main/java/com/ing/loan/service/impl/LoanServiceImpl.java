package com.ing.loan.service.impl;

import com.ing.loan.aspect.AuthorizeAction;
import com.ing.loan.errorhandling.InvalidRequestException;
import com.ing.loan.errorhandling.ResourceNotFoundException;
import com.ing.loan.model.dto.*;
import com.ing.loan.model.entity.Customer;
import com.ing.loan.model.entity.Loan;
import com.ing.loan.model.entity.LoanInstallment;
import com.ing.loan.repository.CustomerRepository;
import com.ing.loan.repository.LoanInstallmentRepository;
import com.ing.loan.repository.LoanRepository;
import com.ing.loan.service.LoanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanServiceImpl implements LoanService {

    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;


    /**
     * Creates a loan for a customer based on the provided loan request.
     * The loan amount is calculated based on the amount and interest rate provided
     * in the loan request.
     * The total loan amount is calculated as amount * (1 + interest rate).
     * The customer's credit limit is checked to ensure that it is enough to get the loan.
     * If the credit limit is not enough, an InvalidRequestException is thrown.
     * The loan is saved to the database along with the generated installments.
     * The customer's used credit limit is updated to reflect the new loan amount.
     * The response contains the details of the created loan.
     *
     * @param loanRequest the loan request containing the details for the loan
     * @return a LoanResponseDTO containing the details of the created loan
     * @throws InvalidRequestException if the loan request parameters are invalid
     * @throws ResourceNotFoundException if the customer is not found
     */
    @AuthorizeAction
    @Transactional
    @Override
    public LoanResponseDTO createLoan(LoanRequestDTO loanRequest) {
        log.info("Starting loan creation process for customer ID: {}", loanRequest.customerId());

        // Step 1: Validate request parameters
        log.debug("Validating loan request parameters: {}", loanRequest);
        validateLoanRequest(loanRequest);

        // Step 2: Retrieve customer
        log.debug("Retrieving customer with ID: {}", loanRequest.customerId());
        Customer customer = customerRepository.findById(loanRequest.customerId())
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", loanRequest.customerId());
                    return new ResourceNotFoundException("Customer", "ID", loanRequest.customerId());
                });

        // Step 3: Calculate total loan amount and check credit limit
        log.debug("Calculating total loan amount for customer ID: {}", loanRequest.customerId());
        BigDecimal totalLoanAmount = loanRequest.amount().multiply(BigDecimal.valueOf(1 + loanRequest.interestRate()));
        log.info("Total loan amount calculated: {}", totalLoanAmount);
        if (customer.getCreditLimit().subtract(customer.getUsedCreditLimit()).compareTo(totalLoanAmount) < 0) {
            log.warn("Insufficient credit limit for customer ID: {}. Credit limit: {}, Used: {}, Required: {}",
                    loanRequest.customerId(), customer.getCreditLimit(), customer.getUsedCreditLimit(), totalLoanAmount);
            throw new InvalidRequestException("Customer's credit limit is not enough to get this loan.");
        }

        // Step 4: Create loan and save
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(totalLoanAmount);
        loan.setNumberOfInstallment(loanRequest.numberOfInstallments());
        loan.setCreateDate(LocalDate.now());
        loan.setIsPaid(false);
        loan = loanRepository.save(loan);
        log.info("Loan created and saved with ID: {}", loan.getId());

        // Step 5: Generate installments
        List<LoanInstallment> installments = createInstallments(loan, loanRequest.numberOfInstallments(), totalLoanAmount);
        loanInstallmentRepository.saveAll(installments);
        log.info("Loan installments saved for loan ID: {}", loan.getId());

        // Step 6: Update customer's used credit limit
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(totalLoanAmount));
        customerRepository.save(customer);
        log.info("Customer's used credit limit updated for customer ID: {}", customer.getId());

        // Step 7: Return response
        log.info("Loan creation process completed for customer ID: {}. Loan ID: {}", loanRequest.customerId(), loan.getId());
        return new LoanResponseDTO(loan.getId(), totalLoanAmount, loan.getIsPaid());
    }

    /**
     * Lists all loans for a specific customer based on the provided filters.
     * The filters include whether the loan is paid or not, and the number of installments.
     * The customer is validated to ensure that they exist.
     * The loans are retrieved from the database based on the provided filters.
     * The loans are then mapped to LoanSummaryDTOs for response.
     *
     * @param customerId the ID of the customer to list loans for
     * @param isPaid the filter for paid status of the loans
     * @param numberOfInstallments the filter for the number of installments
     * @return a list of LoanSummaryDTOs containing the details of the loans
     * @throws ResourceNotFoundException if the customer is not found
     */
    @AuthorizeAction
    @Override
    public List<LoanSummaryDTO> listLoans(Long customerId, Boolean isPaid, Integer numberOfInstallments) {
        log.info("Listing loans for customer ID: {}. Filters - isPaid: {}, numberOfInstallments: {}", customerId, isPaid, numberOfInstallments);

        // Step 1: Validate customer exists
        log.debug("Validating existence of customer with ID: {}", customerId);
        boolean customerExists = loanRepository.existsByCustomerId(customerId);
        if (!customerExists) {
            log.error("Customer not found with ID: {}", customerId);
            throw new ResourceNotFoundException("Customer", "ID", customerId);
        }

        // Step 2: Retrieve loans with optional filters
        List<Loan> loans = loanRepository.findAllByCustomerIdAndFilters(customerId, isPaid, numberOfInstallments);
        log.info("Retrieved {} loans for customer ID: {}.", loans.size(), customerId);

        // Step 3: Map loans to DTOs
        List<LoanSummaryDTO> loanSummaries = loans.stream()
                .map(loan -> new LoanSummaryDTO(
                        loan.getId(),
                        loan.getLoanAmount(),
                        loan.getIsPaid(),
                        loan.getNumberOfInstallment()
                ))
                .collect(Collectors.toList());

        log.info("Loan listing process completed for customer ID: {}", customerId);
        return loanSummaries;
    }

    /**
     * Lists all installments for a specific loan.
     * The loan is validated to ensure that it exists.
     * The installments are retrieved from the database for the loan.
     * The installments are then mapped to LoanInstallmentDTOs for response.
     *
     * @param loanId the ID of the loan to list installments for
     * @return a list of LoanInstallmentDTOs containing the details of the installments
     * @throws ResourceNotFoundException if the loan is not found
     */
    @Override
    @AuthorizeAction
    public List<LoanInstallmentDTO> listInstallments(Long loanId) {
        log.info("Listing installments for loan ID: {}", loanId);

        // Step 1: Validate loan exists
        log.debug("Validating existence of loan with ID: {}", loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.error("Loan not found with ID: {}", loanId);
                    return new ResourceNotFoundException("Loan", "ID", loanId);
                });
        log.info("Loan with ID: {} exists.", loanId);

        // Step 2: Retrieve all installments for the loan
        List<LoanInstallment> installments = loanInstallmentRepository.findByLoanId(loan.getId());
        log.info("Retrieved {} installments for loan ID: {}.", installments.size(), loanId);

        // Step 3: Map installments to DTOs
        List<LoanInstallmentDTO> installmentDTOs = installments.stream()
                .map(installment -> new LoanInstallmentDTO(
                        installment.getId(),
                        installment.getAmount(),
                        installment.getPaidAmount(),
                        installment.getDueDate(),
                        installment.getPaymentDate(),
                        installment.getIsPaid()
                ))
                .collect(Collectors.toList());

        log.info("Installment listing process completed for loan ID: {}", loanId);
        return installmentDTOs;
    }


    /**
     * Pays the installments for a specific loan based on the provided payment amount.
     * The installments that can be paid are those that are due within the next 3 months.
     * The installments are paid in order of due date until the payment amount is exhausted.
     * The customer's used credit limit is updated to reflect the paid amount.
     * The response contains the number of installments paid, the total amount paid,
     * and a flag indicating if the loan is fully paid.
     *
     * @param loanId the ID of the loan to pay
     * @param paymentAmount the amount to pay
     * @return a PaymentResponseDTO containing the payment details
     * @throws InvalidRequestException if the loan is already fully paid or no installments are available for payment
     * @throws ResourceNotFoundException if the loan is not found
     */
    @AuthorizeAction
    @Transactional
    @Override
    public PaymentResponseDTO payLoan(Long loanId, BigDecimal paymentAmount) {
        log.info("Initiating payment for loan ID: {} with payment amount: {}", loanId, paymentAmount);

        // Step 1: Validate loan
        log.debug("Validating loan existence for loan ID: {}", loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.error("Loan not found with ID: {}", loanId);
                    return new ResourceNotFoundException("Loan", "ID", loanId);
                });

        if (loan.getIsPaid()) {
            log.warn("Loan with ID: {} is already fully paid.", loanId);
            throw new InvalidRequestException("Loan is already fully paid.");
        }

        // Step 2: Retrieve installments that can be paid (due within 3 months)
        LocalDate maxDueDate = LocalDate.now().plusMonths(3);
        List<LoanInstallment> unpaidInstallments = loanInstallmentRepository
                .findByLoanIdAndIsPaidFalse(loanId).stream()
                .filter(inst -> !inst.getDueDate().isAfter(maxDueDate))
                .sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate()))
                .collect(Collectors.toList());

        if (unpaidInstallments.isEmpty()) {
            log.warn("No installments available for payment for loan ID: {} within the next 3 months.", loanId);
            throw new InvalidRequestException("No installments available for payment within the next 3 months.");
        }

        log.info("Found {} unpaid installments for loan ID: {} eligible for payment.", unpaidInstallments.size(), loanId);

        // Step 3: Pay installments
        int installmentsPaid = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (LoanInstallment installment : unpaidInstallments) {
            log.debug("Processing installment with due date: {} and amount: {}", installment.getDueDate(), installment.getAmount());
            if (paymentAmount.compareTo(installment.getAmount()) >= 0) {
                // Pay this installment
                paymentAmount = paymentAmount.subtract(installment.getAmount());
                totalPaid = totalPaid.add(installment.getAmount());
                installment.setPaidAmount(installment.getAmount());
                installment.setPaymentDate(LocalDate.now());
                installment.setIsPaid(true);
                loanInstallmentRepository.save(installment);

                installmentsPaid++;
                log.info("Paid installment with due date: {} for loan ID: {}. Remaining payment amount: {}",
                        installment.getDueDate(), loanId, paymentAmount);
            } else {
                log.info("Insufficient payment amount to cover the next installment. Stopping payments for loan ID: {}.", loanId);
                break;
            }
        }

        // Step 4: Check if the loan is fully paid
        boolean isLoanPaid = loanInstallmentRepository.findByLoanIdAndIsPaidFalse(loanId).isEmpty();
        if (isLoanPaid) {
            loan.setIsPaid(true);
            loanRepository.save(loan);
            log.info("Loan with ID: {} is fully paid.", loanId);
        } else {
            log.info("Loan with ID: {} is not fully paid. Remaining installments exist.", loanId);
        }

        // Step 5: Update customer's used credit limit
        Customer customer = loan.getCustomer();
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().subtract(totalPaid));
        customerRepository.save(customer);
        log.info("Updated customer's used credit limit after payment. Customer ID: {}", customer.getId());

        // Step 6: Return response
        log.info("Payment process completed for loan ID: {}. Installments paid: {}, Total paid: {}, Loan fully paid: {}",
                loanId, installmentsPaid, totalPaid, isLoanPaid);
        return new PaymentResponseDTO(installmentsPaid, totalPaid, isLoanPaid);
    }


    /**
     * Validates the loan request parameters.
     * The number of installments must be one of 6, 9, 12, or 24.
     * The interest rate must be between 0.1 and 0.5.
     * If the parameters are invalid, an InvalidRequestException is thrown.
     * @param loanRequest
     */
    private void validateLoanRequest(LoanRequestDTO loanRequest) {
        // Validate number of installments
        List<Integer> validInstallments = List.of(6, 9, 12, 24);
        if (!validInstallments.contains(loanRequest.numberOfInstallments())) {
            throw new InvalidRequestException("Invalid number of installments. Allowed values are: 6, 9, 12, 24.");
        }

        // Validate interest rate
        if (loanRequest.interestRate() < 0.1 || loanRequest.interestRate() > 0.5) {
            throw new InvalidRequestException("Invalid interest rate. Allowed range is: 0.1 - 0.5.");
        }
    }


    /**
     * Creates a list of loan installments for a loan based on the total loan amount.
     * The number of installments is determined by the loan request.
     * The installment amount is calculated as total loan amount divided by the number of installments.
     * The first due date is set to the first day of the next month.
     * Each installment is initialized with zero paid amount and is not paid.
     * @param loan the loan for which to create installments
     * @param numberOfInstallments the number of installments to create
     * @param totalLoanAmount the total loan amount
     * @return a list of LoanInstallment objects
     */
    private List<LoanInstallment> createInstallments(Loan loan, int numberOfInstallments, BigDecimal totalLoanAmount) {
        List<LoanInstallment> installments = new ArrayList<>();
        BigDecimal installmentAmount = totalLoanAmount.divide(BigDecimal.valueOf(numberOfInstallments), BigDecimal.ROUND_HALF_EVEN);
        LocalDate firstDueDate = LocalDate.now().withDayOfMonth(1).plusMonths(1);

        for (int i = 0; i < numberOfInstallments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount(installmentAmount);
            installment.setDueDate(firstDueDate.plusMonths(i));
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setIsPaid(false);
            installments.add(installment);
        }
        return installments;
    }

}









