package com.ing.loan.controller;

import com.ing.loan.model.dto.*;
import com.ing.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loans", description = "APIs for managing loans")
public class LoanApiController {

    private final LoanService loanService;

    /**
     * Create a new loan for a customer.
     */
    @PostMapping
    @Operation(
            summary = "Create a new loan",
            description = "Creates a loan for a given customer with a specified amount, interest rate, and installments."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<LoanResponseDTO> createLoan(@Valid @RequestBody LoanRequestDTO loanRequest) {
        LoanResponseDTO loanResponse = loanService.createLoan(loanRequest);
        return ResponseEntity.status(201).body(loanResponse);
    }

    /**
     * List all loans for a specific customer.
     */
    @GetMapping
    @Operation(summary = "List loans", description = "Fetch loans for a given customer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loans fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<List<LoanSummaryDTO>> listLoans(@RequestParam Long customerId,
                                                          @RequestParam(required = false) Boolean isPaid,
                                                          @RequestParam(required = false) Integer numberOfInstallments) {
        List<LoanSummaryDTO> loans = loanService.listLoans(customerId, isPaid, numberOfInstallments);
        return ResponseEntity.ok(loans);
    }

    /**
     * List all installments for a specific loan.
     */
    @GetMapping("/{loanId}/installments")
    @Operation(summary = "List installments", description = "Fetch installments for a given loan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Installments fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<List<LoanInstallmentDTO>> listInstallments(@PathVariable Long loanId) {
        List<LoanInstallmentDTO> loanInstallments = loanService.listInstallments(loanId);
        return ResponseEntity.ok(loanInstallments);
    }

    /**
     * Pay installments for a specific loan.
     */
    @PostMapping("/{loanId}/pay")
    @Operation(summary = "Pay loan", description = "Pay installments for a given loan")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Loan paid successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input provided"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<PaymentResponseDTO> payLoan(@PathVariable Long loanId,
                                                    @Valid @RequestBody PaymentRequestDTO paymentRequest) {
        PaymentResponseDTO paymentResult = loanService.payLoan(loanId, paymentRequest.amount());
        return ResponseEntity.ok(paymentResult);
    }
}
