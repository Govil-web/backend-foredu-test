package com.foroescolar.controllers;

import com.foroescolar.dtos.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ApiResponse {

    private ApiResponse() {
    }


    public static <T> ResponseEntity<ApiResponseDto<T>> success(String message, T data) {
        return ResponseEntity.ok(new ApiResponseDto<>(true, message, data));
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(true, message, data));
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto<>(false, message, null));
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponseDto<>(false, message, null));
    }

    public static <T> ResponseEntity<ApiResponseDto<T>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponseDto<>(false, message, null));
    }
}