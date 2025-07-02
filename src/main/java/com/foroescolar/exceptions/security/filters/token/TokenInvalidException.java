package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;

public class TokenInvalidException extends TokenException {
    public TokenInvalidException(String message) {
        super(message, ErrorCode.TOKEN_INVALID);
    }
}