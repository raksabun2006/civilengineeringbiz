package com.engineerbiz.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an entity uniqueness constraint is violated (e.g. duplicate email, code, or number).
 */
public class DuplicateResourceException extends BaseException {

    public DuplicateResourceException(String resourceName, String fieldName, Object value) {
        super(String.format("%s with %s '%s' already exists", resourceName, fieldName, value),
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT);
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE", HttpStatus.CONFLICT);
    }
}
