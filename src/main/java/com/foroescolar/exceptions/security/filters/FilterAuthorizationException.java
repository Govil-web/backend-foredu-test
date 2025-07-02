package com.foroescolar.exceptions.security.filters;


public class FilterAuthorizationException extends RuntimeException {
    public FilterAuthorizationException(String message) {
        super(message);
    }

    public FilterAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}