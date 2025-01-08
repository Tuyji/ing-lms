

package com.ing.loan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * This method configures the security filter chain for the application.
     * It disables CSRF protection, requires authentication for all endpoints under /api,
     * and permits all requests to Swagger and H2 URLs.
     * The method also configures basic authentication for simplicity.
     *
     * @param http the HttpSecurity object
     * @return the security filter chain
     * @throws Exception if an error occurs during the configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated() // All endpoints require authentication
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**", "h2-console/login.do/**").permitAll() // Permit all requests to Swagger and H2 URLs
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults()); // Use basic authentication for simplicity

        return http.build();
    }

    /**
     * This method creates a user details service with three users: admin, Jane, and John.
     * The admin user has the role ADMIN, while Jane and John have the role CUSTOMER.
     * The passwords for all users are encoded using the BCryptPasswordEncoder.
     *
     * @param passwordEncoder the password encoder
     * @return the in-memory user details manager
     */
    @Bean
    public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .roles("ADMIN")
                        .build(),
                User.withUsername("Jane")
                        .password(passwordEncoder.encode("customer123"))
                        .roles("CUSTOMER")
                        .build(),
                User.withUsername("John")
                        .password(passwordEncoder.encode("customer123"))
                        .roles("CUSTOMER")
                        .build()
        );
    }

    /**
     * This method creates a BCryptPasswordEncoder bean.
     *
     * @return the BCryptPasswordEncoder bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
