package com.foroescolar.exceptions.security.filters.token;

import com.foroescolar.exceptions.security.ErrorCode;

import java.util.Date;

public class TokenExpiredException extends TokenException {

    public TokenExpiredException(String message, Date date) {
        super(message, ErrorCode.TOKEN_EXPIRED);
    }

}

