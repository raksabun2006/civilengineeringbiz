package com.engineerbiz.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Standard enterprise API response wrapper for EngineerBiz REST endpoints.
 *
 * @param <T> Payload data type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        String code,
        List<String> errors,
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Operation completed successfully", data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message, String code, List<String> errors) {
        return new ApiResponse<>(false, message, null, code, errors, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, message, null, code, null, Instant.now());
    }
}
