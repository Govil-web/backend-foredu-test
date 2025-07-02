package com.foroescolar.exceptions.security;

import com.foroescolar.exceptions.BaseException;


import org.springframework.http.HttpStatus;

public class SecurityException extends BaseException {
    public SecurityException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.UNAUTHORIZED);
    }
}
