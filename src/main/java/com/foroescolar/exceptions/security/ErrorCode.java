package com.foroescolar.exceptions.security;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // Errores de autenticación (AUTH-XXX)
    AUTHENTICATION_FAILED("AUTH-001", "Error de autenticación"),
    INVALID_CREDENTIALS("AUTH-002", "Credenciales inválidas"),

    TOKEN_EXPIRED("SEC-001", "Token expirado"),
    TOKEN_INVALID("SEC-002", "Token inválido"),
    TOKEN_BLACKLISTED("SEC-003", "Token en lista negra"),
    TOKEN_MALFORMED("TOKEN-003", "Token malformado"),
    AUTHORIZATION_FAILED("SEC-005", "Fallo en la autorización"),

    // Errores de Validación (VAL-XXX)
    VALIDATION_ERROR("VAL-001", "Error de validación"),
    TOKEN_OPERATION_FAILED("VAL-002", "Error al operar con el token"),
    DATABASE_ERROR("VAL-003", "Error de base de datos"),

    // Errores de Recursos (RES-XXX)
    RESOURCE_NOT_FOUND("RES-001", "Recurso no encontrado"),
    CONSTRAINT_VIOLATION("RES-002", "Violación de restricción"),


    // Errores del Sistema (SYS-XXX)
    INTERNAL_ERROR("SYS-001", "Error interno del servidor"),
    UNIQUE_CONSTRAINT_VIOLATION("VAL-001", "Violación de restricción única"),
    DUPLICATE_DNI("VAL-002", "DNI duplicado"),
    JWT_AUTHENTICATION_FAILED("VAL-003", "Error de autenticación JWT"),
    ENTITY_NOT_FOUND("VAL-004", "Entidad no encontrada"),
    DUPLICATE_EMAIL("VAL-003", "Email duplicado");
    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}