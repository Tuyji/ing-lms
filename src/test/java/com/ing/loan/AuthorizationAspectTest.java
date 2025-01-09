package com.ing.loan;

import com.ing.loan.aspect.AuthorizationAspect;
import com.ing.loan.errorhandling.ResourceNotFoundException;
import com.ing.loan.model.dto.LoanRequestDTO;
import com.ing.loan.model.entity.Customer;
import com.ing.loan.model.entity.Loan;
import com.ing.loan.repository.CustomerRepository;
import com.ing.loan.repository.LoanRepository;
import com.ing.loan.service.LoanService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationAspectTest {

    @InjectMocks
    private AuthorizationAspect authorizationAspect;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private JoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void checkAuthorization_AdminAccess() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        assertDoesNotThrow(() -> authorizationAspect.checkAuthorization(joinPoint));
    }

    @Test
    void checkAuthorization_UserAccess_Success() throws NoSuchMethodException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user1");
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        // Mock the CustomerRepository to return a customer with ID 1L
        Customer mockedCustomer = mock(Customer.class);
        when(customerRepository.findByName("user1")).thenReturn(Optional.of(mockedCustomer));
        when(mockedCustomer.getId()).thenReturn(1L);

        // Mock the JoinPoint and its MethodSignature
        Method method = TestClass.class.getMethod("listLoans", Long.class);
        MethodSignature methodSignature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{1L});

        // Call the method and assert no exception is thrown
        assertDoesNotThrow(() -> authorizationAspect.checkAuthorization(joinPoint));
    }


    @Test
    void checkAuthorization_UserAccess_Failure() throws NoSuchMethodException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user1");
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Mock the CustomerRepository to return a customer with ID 1L
        Customer mockedCustomer = mock(Customer.class);
        when(customerRepository.findByName("user1")).thenReturn(Optional.of(mockedCustomer));
        when(mockedCustomer.getId()).thenReturn(1L);

        // Mock the JoinPoint and its MethodSignature
        Method method = TestClass.class.getMethod("listLoans", Long.class);
        MethodSignature methodSignature = mock(MethodSignature.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{2L}); // Simulate customerId = 2L as the method argument

        assertThrows(AccessDeniedException.class, () -> authorizationAspect.checkAuthorization(joinPoint));
    }


    @Test
    void extractCustomerId_createLoan() throws NoSuchMethodException {
        LoanRequestDTO loanRequestDTO = mock(LoanRequestDTO.class);
        when(loanRequestDTO.customerId()).thenReturn(1L);

        Method method = TestClass.class.getMethod("createLoan", LoanRequestDTO.class);
        setupJoinPointMock(method, loanRequestDTO);

        Long customerId = authorizationAspect.extractCustomerId(joinPoint);
        assertEquals(1L, customerId);
    }

    @Test
    void extractCustomerId_listLoans() throws NoSuchMethodException {
        Method method = TestClass.class.getMethod("listLoans", Long.class);
        setupJoinPointMock(method, 1L);

        Long customerId = authorizationAspect.extractCustomerId(joinPoint);
        assertEquals(1L, customerId);
    }

    @Test
    void extractCustomerId_listInstallments() throws NoSuchMethodException {
        Loan loan = mock(Loan.class);
        Customer customer = mock(Customer.class);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loan.getCustomer()).thenReturn(customer);
        when(customer.getId()).thenReturn(1L);

        Method method = TestClass.class.getMethod("listInstallments", Long.class);
        setupJoinPointMock(method, 1L);

        Long customerId = authorizationAspect.extractCustomerId(joinPoint);
        assertEquals(1L, customerId);
    }

    @Test
    void getLoggedInCustomerId_Success() {
        Customer customer = mock(Customer.class);

        when(customerRepository.findByName("user1")).thenReturn(Optional.of(customer));
        when(customer.getId()).thenReturn(1L);

        Long customerId = authorizationAspect.getLoggedInCustomerId("user1");
        assertEquals(1L, customerId);
    }

    @Test
    void getLoggedInCustomerId_NotFound() {
        when(customerRepository.findByName("user1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authorizationAspect.getLoggedInCustomerId("user1"));
    }

    private void setupJoinPointMock(Method method, Object... args) {
        MethodSignature methodSignature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(args);
    }

    // Dummy class to simulate method signatures for testing
    static class TestClass {
        public void createLoan(LoanRequestDTO loanRequestDTO) {}
        public void listLoans(Long customerId) {}
        public void listInstallments(Long loanId) {}
    }

}
