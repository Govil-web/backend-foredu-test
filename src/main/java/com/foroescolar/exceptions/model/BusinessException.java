package com.foroescolar.exceptions.model;

import com.foroescolar.exceptions.BaseException;
import com.foroescolar.exceptions.security.ErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {
    public BusinessException(String message, ErrorCode errorCode) {
        super(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
