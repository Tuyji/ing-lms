package com.ing.loan.errorhandling;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@RequiredArgsConstructor
public class ErrorResponse {

    private final HttpStatus status;
    private final String message;

}