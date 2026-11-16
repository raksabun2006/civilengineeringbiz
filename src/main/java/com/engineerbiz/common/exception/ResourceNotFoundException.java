package com.engineerbiz.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an entity requested by ID or business key does not exist.
 */
public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(String.format("%s with identifier '%s' was not found", resourceName, identifier),
                "RESOURCE_NOT_FOUND",
                HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
