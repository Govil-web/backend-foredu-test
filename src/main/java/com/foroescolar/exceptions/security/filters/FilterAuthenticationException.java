package com.foroescolar.exceptions.security.filters;


public class FilterAuthenticationException extends RuntimeException {
    public FilterAuthenticationException(String message) {
        super(message);
    }

    public FilterAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
