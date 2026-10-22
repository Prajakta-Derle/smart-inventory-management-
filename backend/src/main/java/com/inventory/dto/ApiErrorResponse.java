package com.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final boolean success = false;
    private final int statusCode;
    private final String message;
    private final String timestamp;
    private final Map<String, String> fieldErrors;

    public ApiErrorResponse(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public ApiErrorResponse(int statusCode, String message, Map<String, String> fieldErrors) {
        this.statusCode  = statusCode;
        this.message     = message;
        this.timestamp   = Instant.now().toString();
        this.fieldErrors = fieldErrors;
    }
}
