package com.ing.loan;

import com.ing.loan.errorhandling.InvalidRequestException;
import com.ing.loan.errorhandling.ResourceNotFoundException;
import com.ing.loan.model.dto.LoanInstallmentDTO;
import com.ing.loan.model.dto.LoanRequestDTO;
import com.ing.loan.model.dto.LoanResponseDTO;
import com.ing.loan.model.dto.LoanSummaryDTO;
import com.ing.loan.model.entity.Customer;
import com.ing.loan.model.entity.Loan;
import com.ing.loan.model.entity.LoanInstallment;
import com.ing.loan.repository.CustomerRepository;
import com.ing.loan.repository.LoanInstallmentRepository;
import com.ing.loan.repository.LoanRepository;
import com.ing.loan.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoanServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Customer mockCustomer;
    private Loan mockLoan;
    private LoanInstallment mockInstallment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockCustomer = new Customer();
        mockCustomer.setId(1L);
        mockCustomer.setName("John");
        mockCustomer.setSurname("Doe");
        mockCustomer.setCreditLimit(BigDecimal.valueOf(50000));
        mockCustomer.setUsedCreditLimit(BigDecimal.ZERO);

        mockLoan = new Loan();
        mockLoan.setId(1L);
        mockLoan.setCustomer(mockCustomer);
        mockLoan.setLoanAmount(BigDecimal.valueOf(10000));
        mockLoan.setNumberOfInstallment(12);
        mockLoan.setCreateDate(LocalDate.now());
        mockLoan.setIsPaid(false);

        mockInstallment = new LoanInstallment();
        mockInstallment.setId(1L);
        mockInstallment.setLoan(mockLoan);
        mockInstallment.setAmount(BigDecimal.valueOf(833.33));
        mockInstallment.setPaidAmount(BigDecimal.ZERO);
        mockInstallment.setDueDate(LocalDate.now().plusMonths(1));
        mockInstallment.setIsPaid(false);
    }

    @Test
    void testCreateLoan_Success() {
        Double interestRate = 0.1;
        mockLoan.setLoanAmount(BigDecimal.valueOf(11000.0));
        LoanRequestDTO requestDTO = new LoanRequestDTO(1L, BigDecimal.valueOf(10000), interestRate, 12);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanInstallmentRepository.saveAll(anyList())).thenReturn(new ArrayList<>());
        
        LoanResponseDTO response = loanService.createLoan(requestDTO);
        assertNotNull(response);
        assertEquals(mockLoan.getLoanAmount(), response.totalAmount()); // ???
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(loanInstallmentRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreateLoan_CreditLimitExceeded() {
        mockCustomer.setUsedCreditLimit(BigDecimal.valueOf(45000));
        LoanRequestDTO requestDTO = new LoanRequestDTO(1L, BigDecimal.valueOf(10000), 0.1, 12);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> loanService.createLoan(requestDTO));

        assertEquals("Customer's credit limit is not enough to get this loan.", exception.getMessage());
        verify(customerRepository, never()).save(any(Customer.class));
        verify(loanRepository, never()).save(any(Loan.class));
        verify(loanInstallmentRepository, never()).saveAll(anyList());
    }

    @Test
    void testListLoans_Success() {
        when(loanRepository.existsByCustomerId(1L)).thenReturn(true);
        when(loanRepository.findAllByCustomerIdAndFilters(1L, null, null)).thenReturn(List.of(mockLoan));

        List<LoanSummaryDTO> loans = loanService.listLoans(1L, null, null);

        assertNotNull(loans);
        assertEquals(1, loans.size());
        assertEquals(mockLoan.getLoanAmount(), loans.get(0).loanAmount());
        verify(loanRepository, times(1)).findAllByCustomerIdAndFilters(1L, null, null);
    }

    @Test
    void testListLoans_CustomerNotFound() {
        when(loanRepository.existsByCustomerId(1L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> loanService.listLoans(1L, null, null));

        assertEquals("Customer not found with ID : '1'", exception.getMessage());
        verify(loanRepository, never()).findAllByCustomerIdAndFilters(anyLong(), any(), any());
    }

    @Test
    void testListInstallments_Success() {
        // Arrange
        Long loanId = 1L;
        Loan loan = new Loan();
        loan.setId(loanId);

        LoanInstallment installment1 = new LoanInstallment();
        installment1.setId(1L);
        installment1.setAmount(BigDecimal.valueOf(500));
        installment1.setDueDate(LocalDate.of(2023, 11, 1));
        installment1.setIsPaid(false);

        LoanInstallment installment2 = new LoanInstallment();
        installment2.setId(2L);
        installment2.setAmount(BigDecimal.valueOf(500));
        installment2.setDueDate(LocalDate.of(2023, 12, 1));
        installment2.setIsPaid(true);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(List.of(installment1, installment2));

        // Act
        List<LoanInstallmentDTO> result = loanService.listInstallments(loanId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        LoanInstallmentDTO dto1 = result.get(0);
        assertEquals(BigDecimal.valueOf(500), dto1.amount());
        assertEquals(LocalDate.of(2023, 11, 1), dto1.dueDate());
        assertFalse(dto1.isPaid());

        LoanInstallmentDTO dto2 = result.get(1);
        assertEquals(BigDecimal.valueOf(500), dto2.amount());
        assertEquals(LocalDate.of(2023, 12, 1), dto2.dueDate());
        assertTrue(dto2.isPaid());

        verify(loanRepository, times(1)).findById(loanId);
        verify(loanInstallmentRepository, times(1)).findByLoanId(loanId);
    }

    @Test
    void testListInstallments_LoanNotFound() {
        // Arrange
        Long loanId = 1L;

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> loanService.listInstallments(loanId));

        assertEquals("Loan not found with ID : '1'", exception.getMessage());
        verify(loanRepository, times(1)).findById(loanId);
        verify(loanInstallmentRepository, never()).findByLoanId(anyLong());
    }

    @Test
    void testListInstallments_NoInstallmentsFound() {
        // Arrange
        Long loanId = 1L;
        Loan loan = new Loan();
        loan.setId(loanId);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanId(loanId)).thenReturn(List.of());

        // Act
        List<LoanInstallmentDTO> result = loanService.listInstallments(loanId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(loanRepository, times(1)).findById(loanId);
        verify(loanInstallmentRepository, times(1)).findByLoanId(loanId);
    }

    @Test
    void testPayLoan_Success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));
        when(loanInstallmentRepository.findByLoanIdAndIsPaidFalse(1L)).thenReturn(List.of(mockInstallment));
        when(loanInstallmentRepository.save(any(LoanInstallment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var paymentResponse = loanService.payLoan(1L, BigDecimal.valueOf(1000));

        assertNotNull(paymentResponse);
        assertEquals(1, paymentResponse.installmentsPaid());
        assertEquals(BigDecimal.valueOf(833.33), paymentResponse.totalPaid());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testPayLoan_NoInstallmentsAvailable() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(mockLoan));
        when(loanInstallmentRepository.findByLoanIdAndIsPaidFalse(1L)).thenReturn(new ArrayList<>());

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> loanService.payLoan(1L, BigDecimal.valueOf(1000)));

        assertEquals("No installments available for payment within the next 3 months.", exception.getMessage());
        verify(loanInstallmentRepository, never()).save(any(LoanInstallment.class));
        verify(customerRepository, never()).save(any(Customer.class));
    }
}
