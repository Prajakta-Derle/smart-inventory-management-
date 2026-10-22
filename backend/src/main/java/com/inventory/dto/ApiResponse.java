package com.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int statusCode;
    private final String message;
    private final T data;
    private final String timestamp;

    private ApiResponse(int statusCode, String message, T data) {
        this.success    = true;
        this.statusCode = statusCode;
        this.message    = message;
        this.data       = data;
        this.timestamp  = null;
    }

    private ApiResponse(int statusCode, String message) {
        this.success    = false;
        this.statusCode = statusCode;
        this.message    = message;
        this.data       = null;
        this.timestamp  = Instant.now().toString();
    }

    public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>(statusCode, message);
    }
}
