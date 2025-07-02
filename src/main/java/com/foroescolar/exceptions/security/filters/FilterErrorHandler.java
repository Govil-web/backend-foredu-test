package com.foroescolar.exceptions.security.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foroescolar.exceptions.dtoserror.ErrorResponse;
import com.foroescolar.exceptions.dtoserror.ErrorResponseBuilder;
import com.foroescolar.exceptions.security.filters.token.TokenExpiredException;
import com.foroescolar.exceptions.security.filters.token.TokenInvalidatedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FilterErrorHandler {

    private final ObjectMapper objectMapper;
    private final ErrorResponseBuilder errorResponseBuilder;
    private final MeterRegistry meterRegistry;

    // Contadores para tipos de errores
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();

    // Contador para errores totales
    private final Counter totalErrorCounter;

    public FilterErrorHandler(ObjectMapper objectMapper,
                              ErrorResponseBuilder errorResponseBuilder,
                              MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.errorResponseBuilder = errorResponseBuilder;
        this.meterRegistry = meterRegistry;

        this.totalErrorCounter = Counter.builder("security.errors.total")
                .description("Total de errores de seguridad")
                .register(meterRegistry);
    }

    public void handleException(Exception exception, HttpServletResponse response) throws IOException {
        handleException(exception, response, null);
    }

    public void handleException(Exception exception, HttpServletResponse response, String additionalInfo)
            throws IOException {
        HttpStatus status;
        String message;
        String code;

        // Incrementar contador total
        totalErrorCounter.increment();

        // Incrementar contador específico
        getOrCreateErrorCounter(exception.getClass().getSimpleName()).increment();

        // Determinar nivel de logging apropiado según tipo de error
        if (isExpectedClientError(exception)) {
            // Errores esperados del cliente: DEBUG
            if (log.isDebugEnabled()) {
                logDebugError(exception, additionalInfo);
            }
        } else {
            // Errores inesperados o del servidor: ERROR
            logServerError(exception, additionalInfo);
        }

        if (exception instanceof TokenExpiredException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Su sesión ha expirado";
            code = "TOKEN_EXPIRED";
        } else if (exception instanceof TokenInvalidatedException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Su sesión ha sido invalidada";
            code = "TOKEN_INVALIDATED";
        } else if (exception instanceof FilterAuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "Error de autenticación";
            code = "AUTHENTICATION_ERROR";
        } else if (exception instanceof FilterAuthorizationException) {
            status = HttpStatus.FORBIDDEN;
            message = "No tiene los permisos necesarios para esta operación";
            code = "AUTHORIZATION_ERROR";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "Error interno del servidor";
            code = "INTERNAL_ERROR";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .details(createErrorDetails(exception, additionalInfo))
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private boolean isExpectedClientError(Exception exception) {
        return exception instanceof TokenExpiredException ||
                exception instanceof TokenInvalidatedException ||
                exception instanceof FilterAuthenticationException;
    }

    private void logDebugError(Exception exception, String additionalInfo) {
        String errorMsg = additionalInfo != null ?
                exception.getMessage() + " - " + additionalInfo :
                exception.getMessage();

        log.debug("Error de seguridad (esperado): {}", errorMsg);
    }

    private void logServerError(Exception exception, String additionalInfo) {
        String errorMsg = additionalInfo != null ?
                exception.getMessage() + " - " + additionalInfo :
                exception.getMessage();

        log.error("Error de seguridad: {}", errorMsg, exception);
    }

    private Counter getOrCreateErrorCounter(String errorType) {
        return errorCounters.computeIfAbsent(
                errorType,
                type -> Counter.builder("security.errors.type")
                        .tag("type", type)
                        .description("Errores por tipo: " + type)
                        .register(meterRegistry)
        );
    }

    public void handleTokenExpired(HttpServletRequest request, HttpServletResponse response, TokenExpiredException ex)
            throws IOException {

        // Obtener contador específico
        getOrCreateErrorCounter("TokenExpired").increment();

        if (log.isDebugEnabled()) {
            log.debug("Token expirado: {}", ex.getMessage());
        }

        ErrorResponse errorResponse = errorResponseBuilder.buildAuthorizationError(
                "Su sesión ha expirado, por favor inicie sesión nuevamente",
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }

    private Map<String, String> createErrorDetails(Exception exception, String additionalInfo) {
        Map<String, String> details = new HashMap<>();
        details.put("error_type", exception.getClass().getSimpleName());
        details.put("error_message", exception.getMessage());

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            details.put("additional_info", additionalInfo);
        }



        return details;
    }
}