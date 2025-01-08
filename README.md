# Loan API - Spring Boot Application

## Overview
The Loan API is a Spring Boot application designed for managing loans in a banking system. It provides functionality to create loans, list loans for customers, retrieve installment details, and process loan payments. The application is secured using Spring Security with role-based access control for ADMIN and CUSTOMER users.

---

## Features

1. **Create Loan**
   - Create a new loan for a customer.
   - Validates customer credit limits and loan constraints.
   - Calculates equal monthly installments with due dates.

2. **List Loans**
   - Retrieve loans for a specific customer.
   - Optional filters for installment count, payment status, etc.

3. **List Installments**
   - Retrieve installment details for a given loan.

4. **Pay Loan**
   - Process installment payments for loans.
   - Supports multiple installment payments based on the amount sent.
   - Enforces installment due date restrictions.

5. **Security**
   - Role-based access control (ADMIN and CUSTOMER roles).
   - ADMIN users can manage loans for all customers.
   - CUSTOMER users can only manage their own loans.

---

## Technologies Used

- **Spring Boot**
- **Spring Security**
- **Spring Data JPA**
- **H2 Database** (for development/testing)
- **Lombok** (for reducing boilerplate code)
- **Validation API** (for request validation)
- **Swagger/OpenAPI** (for API documentation)
- **Aspect-Oriented Programming (AOP)** (for authorization checks)

---

## Prerequisites

- Java 17+
- Maven 3.8+

---

## Setup and Run Instructions

### Clone the Repository
```bash
git clone https://github.com/your-repo/loan-api.git
cd loan-api
```

### Build and Run the Application

1. **Build the Project**:
   ```bash
   mvn clean install
   ```

2. **Run the Application**:
   ```bash
   mvn spring-boot:run
   ```

### Access the Application

- **API Base URL**: `http://localhost:8080`
- **H2 Console**: `http://localhost:8080/h2-console`
  - Username: `sa`
  - Password: (empty by default)

---

## API Endpoints

### Authentication

- **Basic Authentication**
  - Username and password are defined in the application configuration (e.g., `SecurityConfig`).

### Loan Management

#### 1. **Create Loan**

**POST** `/api/loans`

Request Body:
```json
{
  "customerId": 1,
  "amount": 10000,
  "interestRate": 0.2,
  "numberOfInstallments": 12
}
```

Response:
```json
{
  "loanId": 1,
  "totalAmount": 12000,
  "installments": [
    {
      "installmentId": 1,
      "amount": 1000,
      "dueDate": "2025-02-01",
      "isPaid": false
    },
    ...
  ]
}
```

#### 2. **List Loans**

**GET** `/api/loans?customerId=1`

Response:
```json
[
  {
    "loanId": 1,
    "amount": 10000,
    "isPaid": false
  }
]
```

#### 3. **List Installments**

**GET** `/api/loans/{loanId}/installments`

Response:
```json
[
  {
    "installmentId": 1,
    "amount": 1000,
    "dueDate": "2025-02-01",
    "isPaid": false
  }
]
```

#### 4. **Pay Loan**

**POST** `/api/loans/{loanId}/pay`

Request Body:
```json
{
  "amount": 3000
}
```

Response:
```json
{
  "installmentsPaid": 3,
  "totalAmountPaid": 3000,
  "isLoanFullyPaid": false
}
```

---

## Security Configuration

- **ADMIN Role**: Can manage loans for all customers.
- **CUSTOMER Role**: Can manage only their own loans.

### Example Users (In-Memory Authentication)

| Username   | Password   | Role     |
|------------|------------|----------|
| admin      | admin123   | ADMIN    |
| customer1  | customer123| CUSTOMER |

---

## Database Schema

### Customer Table
| Column           | Type        |
|------------------|-------------|
| id               | Long        |
| name             | String      |
| surname          | String      |
| creditLimit      | BigDecimal  |
| usedCreditLimit  | BigDecimal  |

### Loan Table
| Column             | Type        |
|--------------------|-------------|
| id                 | Long        |
| customerId         | Long        |
| loanAmount         | BigDecimal  |
| numberOfInstallment| Integer     |
| createDate         | LocalDate   |
| isPaid             | Boolean     |

### Loan Installment Table
| Column       | Type        |
|--------------|-------------|
| id           | Long        |
| loanId       | Long        |
| amount       | BigDecimal  |
| paidAmount   | BigDecimal  |
| dueDate      | LocalDate   |
| paymentDate  | LocalDate   |
| isPaid       | Boolean     |

---

## Testing

- Unit tests are written using JUnit and Mockito.
- Run tests using:
  ```bash
  mvn test
  ```

---

## Swagger/OpenAPI Documentation

- **Swagger UI**: `http://localhost:8090/swagger-ui/index.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

---

## Future Enhancements

1. Integrate OAuth2 for token-based authentication.
2. Add auditing for loan and payment transactions.
3. Implement a notification system for due installments.

---

## Technical Documentation

Please see the file here:
[Loan Management System Overview](./LoanManagementSystemOverview.docx)

## License

This project is licensed under the MIT License. See the LICENSE file for details.
