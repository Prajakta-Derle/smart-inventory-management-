package com.inventory.exception;

public class DuplicateResourceException extends RuntimeException {

    private final String fieldName;

    public DuplicateResourceException(String fieldName, Object value) {
        super(fieldName + " already exists: " + value);
        this.fieldName = fieldName;
    }

    public String getFieldName() { return fieldName; }
}
