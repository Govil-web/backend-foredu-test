package com.foroescolar.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class ApplicationException extends RuntimeException{

    private final String campo;
    private final HttpStatus httpStatus;

    public ApplicationException(String campo, String mensaje, HttpStatus httpStatus) {
        super(mensaje);
        this.campo = campo;
        this.httpStatus = httpStatus;
    }


    public int getStatus() {
        return httpStatus.value();
    }
}
