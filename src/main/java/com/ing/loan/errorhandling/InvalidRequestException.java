package com.ing.loan.errorhandling;

public class InvalidRequestException extends RuntimeException {

    private String parameterName;
    private Object parameterValue;
    private String message;

    public InvalidRequestException(String message) {
        super(message);
        this.message = message;
    }

    public InvalidRequestException(String parameterName, Object parameterValue, String message) {
        super(String.format("Invalid request for %s with value '%s': %s", parameterName, parameterValue, message));
        this.parameterName = parameterName;
        this.parameterValue = parameterValue;
        this.message = message;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Object getParameterValue() {
        return parameterValue;
    }

    @Override
    public String getMessage() {
        return message;
    }
}