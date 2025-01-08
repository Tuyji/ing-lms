package com.ing.loan.config;

import com.ing.loan.model.entity.Customer;
import com.ing.loan.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    // create some customers and save them to the repository
    @Bean
    public CommandLineRunner loadCustomers(CustomerRepository customerRepository) {
        return args -> {
            // Create a few customers
            Customer customer1 = new Customer();
            customer1.setName("John");
            customer1.setSurname("Doe");
            customer1.setCreditLimit(BigDecimal.valueOf(50000));

            Customer customer2 = new Customer();
            customer2.setName("Jane");
            customer2.setSurname("Smith");
            customer2.setCreditLimit(BigDecimal.valueOf(75000));

            Customer customer3 = new Customer();
            customer3.setName("Alice");
            customer3.setSurname("Brown");
            customer3.setCreditLimit(BigDecimal.valueOf(100000));

            // Save customers to the repository
            Customer savedCustomer1 = customerRepository.save(customer1);
            Customer savedCustomer2 = customerRepository.save(customer2);
            Customer savedCustomer3 = customerRepository.save(customer3);

            System.out.println("Customer1 ID: " + savedCustomer1.getId());
            System.out.println("Customer2 ID: " + savedCustomer2.getId());
            System.out.println("Customer3 ID: " + savedCustomer3.getId());

            System.out.println("Customers initialized successfully.");
        };
    }
}
