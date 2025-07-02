package com.foroescolar.exceptions.dtoserror;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final HttpStatus status;
    private final String code;
    private final String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final String timestamp;

    private final Map<String, String> details;

    private ErrorResponse(ErrorResponseBuilder builder) {
        this.status = builder.status;
        this.code = builder.code;
        this.message = builder.message;
        this.timestamp = builder.timestamp;
        this.details = builder.details;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    @Getter
    public static class ErrorResponseBuilder {
        private HttpStatus status;
        private String code;
        private String message;
        private String timestamp;
        private final Map<String, String> details = new HashMap<>();  // Inicialización directa

        private ErrorResponseBuilder() {
            this.timestamp = String.valueOf(LocalDateTime.now());
        }

        public ErrorResponseBuilder status(HttpStatus status) {
            this.status = status;
            return this;
        }

        public ErrorResponseBuilder code(String code) {
            this.code = code;
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = String.valueOf(timestamp);
            return this;
        }

        public ErrorResponseBuilder details(Map<String, String> details) {
            if (details != null) {
                this.details.putAll(details);
            }
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            this.details.put("path", path);
            return this;
        }

        public ErrorResponse build() {
            validateRequiredFields();
            return new ErrorResponse(this);
        }

        private void validateRequiredFields() {
            if (status == null) {
                throw new IllegalStateException("El estado HTTP es obligatorio");
            }
            if (code == null || code.trim().isEmpty()) {
                throw new IllegalStateException("El código de error es obligatorio");
            }
            if (message == null || message.trim().isEmpty()) {
                throw new IllegalStateException("El mensaje es obligatorio");
            }
        }
    }
}