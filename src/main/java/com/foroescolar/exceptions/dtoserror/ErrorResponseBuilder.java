package com.foroescolar.exceptions.dtoserror;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foroescolar.exceptions.security.filters.token.TokenException;
import com.foroescolar.exceptions.security.SecurityException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Component;

import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ErrorResponseBuilder {

    private final ObjectMapper objectMapper;

    public ErrorResponseBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String buildAuthenticationErrorResponse(HttpServletRequest request, Exception ex) {
        try {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .code("AUTHENTICATION_ERROR")
                    .message("Su sesión ha expirado o no tiene autorización")
                    .details(createErrorDetails(request, ex))
                    .build();

            return objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Error al crear la respuesta de error", e);
            return "{\"error\":\"Error interno del servidor\"}";
        }
    }

    private Map<String, String> createErrorDetails(HttpServletRequest request, Exception ex) {
        Map<String, String> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("error_type", ex.getClass().getSimpleName());
        details.put("timestamp", LocalDateTime.now().toString());
        return details;
    }


    public ErrorResponse buildTokenError(TokenException ex) {
        return ErrorResponse.builder()
                .status(ex.getStatus())
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ErrorResponse buildSecurityError(SecurityException ex) {
        return ErrorResponse.builder()
                .status(ex.getStatus())
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ErrorResponse buildUnexpectedError(Exception ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .code("INT-001")
                .message("Error interno del servidor")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ErrorResponse buildAuthorizationError(String message, String path) {
        return ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .code("AUTH-002")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ErrorResponse buildSecurityError(SecurityException ex, WebRequest request) {
        return ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(getPathFromRequest(request))
                .details(ex.getDetails())
                .build();
    }

    private String getPathFromRequest(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
