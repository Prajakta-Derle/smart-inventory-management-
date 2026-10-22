package com.inventory.exception;

/**
 * Thrown when a request is syntactically valid but violates a business rule
 * that must be checked in the service layer (e.g. referencing an inactive or
 * non-existent related resource, or supplying duplicate items). Mapped to
 * HTTP 400 by {@link GlobalExceptionHandler}.
 */
public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
