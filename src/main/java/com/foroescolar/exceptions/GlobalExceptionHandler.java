package com.foroescolar.exceptions;

import com.foroescolar.exceptions.dtoserror.ErrorResponse;
import com.foroescolar.exceptions.dtoserror.ErrorResponseBuilder;
import com.foroescolar.exceptions.security.ErrorCode;
import com.foroescolar.exceptions.security.SecurityException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final Map<String, ErrorCode> CONSTRAINT_VIOLATIONS = Map.of(
            "estudiantes.dni", ErrorCode.DUPLICATE_DNI,
            "users.email", ErrorCode.DUPLICATE_EMAIL
    );

    private final ErrorResponseBuilder errorResponseBuilder;

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        return buildResponse(ex, request);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex, WebRequest request) {
        logSecurityError(ex);
        return buildSecurityResponse(ex, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = getValidationErrors(ex);
        logValidationError(errors);
        return buildValidationResponse(errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        return processConstraintViolation(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        logUnexpectedError(ex);
        return buildUnexpectedErrorResponse();
    }

    private ResponseEntity<ErrorResponse> processConstraintViolation(DataIntegrityViolationException ex) {
        if (!(ex.getCause() instanceof ConstraintViolationException)) {
            return buildDatabaseErrorResponse();
        }

        String message = ex.getCause().getCause().getMessage();
        return CONSTRAINT_VIOLATIONS.entrySet().stream()
                .filter(entry -> message.contains(entry.getKey()))
                .findFirst()
                .map(entry -> buildConstraintResponse(entry.getValue()))
                .orElse(buildDatabaseErrorResponse());
    }

    private Map<String, String> getValidationErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        this::getErrorMessage,
                        (first, second) -> first
                ));
    }

    private String getErrorMessage(FieldError error) {
        return Optional.ofNullable(error.getDefaultMessage())
                .orElse("Error de validación");
    }

    private void logSecurityError(SecurityException ex) {
        log.error("Error de seguridad: {}", ex.getMessage(), ex);
    }

    private void logValidationError(Map<String, String> errors) {
        log.warn("Errores de validación: {}", errors);
    }

    private void logUnexpectedError(Exception ex) {
        log.error("Error no esperado: ", ex);
    }

    private ResponseEntity<ErrorResponse> buildSecurityResponse(SecurityException ex, WebRequest request) {
        ErrorResponse response = errorResponseBuilder.buildSecurityError(ex, request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    private ResponseEntity<ErrorResponse> buildConstraintResponse(ErrorCode errorCode) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT)
                .code(errorCode.getCode())
                .message(errorCode.getDefaultMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    private ResponseEntity<ErrorResponse> buildValidationResponse(Map<String, String> errors) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .code("VALIDATION_ERROR")
                .message("Errores de validación en los datos")
                .details(errors)
                .build();

        return ResponseEntity
                .status(errorResponse.getStatus())
                .body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> buildDatabaseErrorResponse() {
        return buildConstraintResponse(ErrorCode.DATABASE_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildUnexpectedErrorResponse() {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .code("INTERNAL_ERROR")
                .message("Error interno del servidor")
                .build();

        return ResponseEntity
                .status(errorResponse.getStatus())
                .body(errorResponse);
    }
    private ResponseEntity<ErrorResponse> buildResponse(BaseException ex, WebRequest request) {
        log.error("Error base: {}", ex.getMessage(), ex);

        ErrorResponse response = ErrorResponse.builder()
                .status(ex.getStatus())
                .code(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .details(ex.getDetails())
                .build();
        return ResponseEntity
                .status(ex.getStatus())
                .body(response);
    }
}
