package com.ing.loan.aspect;

import com.ing.loan.errorhandling.ResourceNotFoundException;
import com.ing.loan.model.dto.LoanRequestDTO;
import com.ing.loan.model.entity.Loan;
import com.ing.loan.repository.CustomerRepository;
import com.ing.loan.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthorizationAspect {

    private final LoanRepository loanRepository;
    private final CustomerRepository customerRepository;


    /**
     * This method checks if the logged-in user is authorized to perform the action.
     * It allows admins to perform any action, but regular users can only operate on their own data.
     * The method extracts the customer ID from the method arguments and compares it with the logged-in user's ID.
     * If the IDs do not match, an AccessDeniedException is thrown.
     *
     * @param joinPoint the join point representing the intercepted method
     */
    @Before("@annotation(com.ing.loan.aspect.AuthorizeAction) && execution(* *(..))")
    public void checkAuthorization(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // Admins have unrestricted access
            return;
        }

        Long loggedInCustomerId = getLoggedInCustomerId(username);
        Long customerId = extractCustomerId(joinPoint);

        if (!loggedInCustomerId.equals(customerId)) {
            throw new AccessDeniedException("You can only operate on your own data.");
        }
    }

    /**
     * Extracts the customer ID from the method arguments based on the method name.
     * This method handles different cases for different methods:
     * - For `createLoan`, it extracts the customer ID from the `LoanRequestDTO`.
     * - For `listLoans`, it directly uses the customer ID parameter.
     * - For `listInstallments` and `payLoan`, it fetches the loan entity using the loan ID and retrieves the customer ID.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the extracted customer ID
     * @throws ResourceNotFoundException if the loan or customer is not found
     * @throws IllegalArgumentException if the method is unsupported for authorization aspect
     */
    public Long extractCustomerId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        // Handle each method case
        if (method.getName().equals("createLoan")) {
            // Extract customerId from LoanRequestDTO
            LoanRequestDTO loanRequest = (LoanRequestDTO) args[0];
            return loanRequest.customerId();
        }

        if (method.getName().equals("listLoans")) {
            // Directly use the customerId parameter
            return (Long) args[0];
        }

        if (method.getName().equals("listInstallments") || method.getName().equals("payLoan")) {
            // Fetch the loan entity using loanId and retrieve the customer ID
            Long loanId = (Long) args[0];
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Loan", "ID", loanId));
            return loan.getCustomer().getId();
        }

        throw new IllegalArgumentException("Unsupported method for authorization aspect");
    }

    /**
     * Fetches the customer ID based on the logged-in username.
     *
     * @param username the username of the logged-in user
     * @return the customer ID
     * @throws ResourceNotFoundException if the customer is not found
     */
    public Long getLoggedInCustomerId(String username) {
        // Fetch customer ID based on the logged-in username (CUSTOMER role)
        return customerRepository.findByName(username)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "username", username))
                .getId();
    }
}

