package com.ing.loan;

import com.ing.loan.controller.LoanApiController;
import com.ing.loan.model.dto.*;
import com.ing.loan.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoanApiControllerTest {

    @InjectMocks
    private LoanApiController loanApiController;

    @Mock
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateLoan_Success() {
        // Arrange
        LoanRequestDTO loanRequest = new LoanRequestDTO(1L, BigDecimal.valueOf(10000), 5.5D, 12);
        LoanResponseDTO loanResponse = new LoanResponseDTO(1L, BigDecimal.valueOf(11000), false);

        when(loanService.createLoan(loanRequest)).thenReturn(loanResponse);

        // Act
        ResponseEntity<LoanResponseDTO> response = loanApiController.createLoan(loanRequest);

        // Assert
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(loanService, times(1)).createLoan(loanRequest);
    }

    @Test
    void testListLoans_Success() {
        // Arrange
        Long customerId = 1L;
        Boolean isPaid = false;
        Integer numberOfInstallments = 12;

        LoanSummaryDTO loan1 = new LoanSummaryDTO(1L, BigDecimal.valueOf(5000), false, 12);
        LoanSummaryDTO loan2 = new LoanSummaryDTO(2L, BigDecimal.valueOf(10000), true, 24);

        when(loanService.listLoans(customerId, isPaid, numberOfInstallments)).thenReturn(List.of(loan1, loan2));

        // Act
        ResponseEntity<List<LoanSummaryDTO>> response = loanApiController.listLoans(customerId, isPaid, numberOfInstallments);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(loanService, times(1)).listLoans(customerId, isPaid, numberOfInstallments);
    }

    @Test
    void testListInstallments_Success() {
        // Arrange
        Long loanId = 1L;

        LoanInstallmentDTO installment1 = new LoanInstallmentDTO(1L, BigDecimal.valueOf(500), BigDecimal.ZERO, LocalDate.of(2023, 11, 1), null, false);
        LoanInstallmentDTO installment2 = new LoanInstallmentDTO(2L, BigDecimal.valueOf(500), BigDecimal.ZERO, LocalDate.of(2023, 12, 1), null, true);

        when(loanService.listInstallments(loanId)).thenReturn(List.of(installment1, installment2));

        // Act
        ResponseEntity<List<LoanInstallmentDTO>> response = loanApiController.listInstallments(loanId);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(loanService, times(1)).listInstallments(loanId);
    }

    @Test
    void testPayLoan_Success() {
        // Arrange
        Long loanId = 1L;
        PaymentRequestDTO paymentRequest = new PaymentRequestDTO(BigDecimal.valueOf(500));
        PaymentResponseDTO paymentResponse = new PaymentResponseDTO(200, BigDecimal.valueOf(500), true);

        when(loanService.payLoan(loanId, paymentRequest.amount())).thenReturn(paymentResponse);

        // Act
        ResponseEntity<PaymentResponseDTO> response = loanApiController.payLoan(loanId, paymentRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        verify(loanService, times(1)).payLoan(loanId, paymentRequest.amount());
    }

    @Test
    void testCreateLoan_ValidationError() {
        // Arrange
        LoanRequestDTO loanRequest = new LoanRequestDTO(null, BigDecimal.valueOf(10000), null, 12);
        doThrow(new RuntimeException("Invalid input")).when(loanService).createLoan(loanRequest);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> loanApiController.createLoan(loanRequest));
        assertEquals("Invalid input", exception.getMessage());

        verify(loanService, times(1)).createLoan(loanRequest);
    }

    @Test
    void testListInstallments_ResourceNotFound() {
        // Arrange
        Long loanId = 1L;

        when(loanService.listInstallments(loanId)).thenThrow(new RuntimeException("Loan not found"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> loanApiController.listInstallments(loanId));
        assertEquals("Loan not found", exception.getMessage());
        verify(loanService, times(1)).listInstallments(loanId);
    }
}
