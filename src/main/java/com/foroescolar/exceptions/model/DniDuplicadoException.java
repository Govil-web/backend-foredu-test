package com.foroescolar.exceptions.model;

import com.foroescolar.exceptions.security.ErrorCode;

public class DniDuplicadoException extends BusinessException {
    public DniDuplicadoException(String dni) {
        super(
                String.format("El DNI %s ya se encuentra registrado en el sistema", dni),
                ErrorCode.DUPLICATE_DNI
        );
    }
}
