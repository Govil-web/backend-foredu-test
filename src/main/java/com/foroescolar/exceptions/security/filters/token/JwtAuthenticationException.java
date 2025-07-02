package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;

public class JwtAuthenticationException extends TokenException{
    public JwtAuthenticationException(String message) {
        super(message, ErrorCode.JWT_AUTHENTICATION_FAILED);
    }
}
