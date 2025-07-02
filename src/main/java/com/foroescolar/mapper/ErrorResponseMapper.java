package com.foroescolar.mapper;

import com.foroescolar.dtos.ApiResponseDto;
import com.foroescolar.exceptions.dtoserror.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Clase responsable de mapear respuestas de error a ResponseEntity.
 * Implementa el patrón Mapper para convertir entre diferentes representaciones de errores.
 */
@Component
public class ErrorResponseMapper {

    /**
     * Convierte un ErrorResponse a ResponseEntity manteniendo el estado HTTP.
     */
    public ResponseEntity<ErrorResponse> toResponseEntity(ErrorResponse errorResponse) {
        return ResponseEntity
                .status(errorResponse.getStatus())
                .body(errorResponse);
    }

    /**
     * Convierte un error genérico a ApiResponseDto con estado de éxito falso.
     */
    public <T> ResponseEntity<ApiResponseDto<T>> toApiResponse(
            ErrorResponse errorResponse,
            T errorDetails) {

        ApiResponseDto<T> apiResponse = new ApiResponseDto<>(
                false,
                errorResponse.getMessage(),
                errorDetails
        );

        return ResponseEntity
                .status(errorResponse.getStatus())
                .body(apiResponse);
    }

    /**
     * Crea una respuesta de error con detalles personalizados.
     */
    public <T> ResponseEntity<ApiResponseDto<T>> createErrorResponse(
            ErrorResponse errorResponse,
            String customMessage,
            T errorDetails) {

        ApiResponseDto<T> apiResponse = new ApiResponseDto<>(
                false,
                customMessage != null ? customMessage : errorResponse.getMessage(),
                errorDetails
        );

        return ResponseEntity
                .status(errorResponse.getStatus())
                .body(apiResponse);
    }
}


