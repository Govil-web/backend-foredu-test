package com.foroescolar.utils;


import com.foroescolar.dtos.ApiResponseDto;
import com.foroescolar.dtos.asistencia.AsistenciaDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.List;

public final class ApiResponseUtils {

    private ApiResponseUtils() {
        // Constructor privado para utilidad
    }

    // Para respuestas con un solo objeto
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> success(T data, String message) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, message, data));
    }

    // Para respuestas con Iterable
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> successIterable(Iterable<T> data, String message) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, message, data));
    }

    // Para respuestas con List de Iterables
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> successListIterable(List<Iterable<T>> data, String message) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, message, data));
    }

    // Para respuestas de creación exitosa
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, message, data));
    }

    // Para respuestas de error genérico
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> error(String message) {
        return ResponseEntity.badRequest()
                .body(new ApiResponseDto<>(false, message, (T) null));
    }

    // Para respuestas de error de permisos
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDto<>(false, message, (T) null));
    }

    // Para respuestas de error interno
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseDto<>(false, message, (T) null));
    }
    public static <T extends Serializable> ResponseEntity<ApiResponseDto<T>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseDto<>(false, message, (T) null));
    }


}