package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;

public class TokenOperationException extends TokenException {
    public TokenOperationException(String message, Throwable cause) {
        super(message, ErrorCode.TOKEN_OPERATION_FAILED, cause);
    }
}
