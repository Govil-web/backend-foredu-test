package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;

public class TokenMalformedException extends TokenException {
    public TokenMalformedException(String message) {
        super(message, ErrorCode.TOKEN_MALFORMED);
    }
}
