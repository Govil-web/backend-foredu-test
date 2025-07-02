package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;

public class AuthenticationFailedException extends TokenException {
    public AuthenticationFailedException(String message) {
        super(message, ErrorCode.AUTHENTICATION_FAILED);
    }
}