package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;
import com.foroescolar.exceptions.security.SecurityException;

public class TokenException extends SecurityException {
    public TokenException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public TokenException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, errorCode);
        initCause(cause);
    }
}
