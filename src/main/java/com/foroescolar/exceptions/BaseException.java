package com.foroescolar.exceptions;

import com.foroescolar.exceptions.security.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class BaseException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    protected final ErrorCode errorCode;
    protected final transient HttpStatus status;
    protected final transient Map<String, String> details;

    protected BaseException(String message, ErrorCode errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.details = new HashMap<>();
    }

    protected BaseException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.details = new HashMap<>();
    }






    public Map<String, String> getDetails() {
        return new HashMap<>(details);
    }
}