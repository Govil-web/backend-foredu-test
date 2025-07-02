package com.foroescolar.config.security.handlers;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.foroescolar.exceptions.dtoserror.ErrorResponse;
import com.foroescolar.exceptions.dtoserror.ErrorResponseBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ErrorResponseBuilder errorResponseBuilder;

    public SecurityExceptionHandler(ObjectMapper objectMapper, ErrorResponseBuilder errorResponseBuilder) {
        this.objectMapper = objectMapper;
        this.errorResponseBuilder = errorResponseBuilder;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Error de autenticación: {}", authException.getMessage());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .code("AUTHENTICATION_ERROR")
                .message("Su sesión ha expirado o no tiene autorización para acceder a este recurso")
                .details(createErrorDetails(request, authException))
                .build();



        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.error("Acceso denegado: {}", accessDeniedException.getMessage());

        ErrorResponse errorResponse = errorResponseBuilder.buildAuthorizationError(
                "Acceso denegado",
                request.getRequestURI()
        );

        writeResponse(response, errorResponse);
    }

    private void writeResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(errorResponse.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
    private Map<String, String> createErrorDetails(HttpServletRequest request,
                                                   AuthenticationException authException) {
        Map<String, String> details = new HashMap<>();
        details.put("path", request.getRequestURI());
        details.put("error_message", authException.getMessage());
        details.put("timestamp", LocalDateTime.now().toString());
        return details;
    }
}
