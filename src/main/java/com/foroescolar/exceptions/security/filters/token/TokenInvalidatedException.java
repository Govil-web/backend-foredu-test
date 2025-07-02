package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;

public class TokenInvalidatedException extends TokenException {
    public TokenInvalidatedException(String message) {
        super(message, ErrorCode.TOKEN_BLACKLISTED);
    }

}
