package com.foroescolar.enums;

public enum RoleEnum {
    ROLE_ESTUDIANTE,
    ROLE_PROFESOR,
    ROLE_TUTOR,
    ROLE_ADMINISTRADOR;

    public String getAuthority() {
        return this.name();
    }


}
