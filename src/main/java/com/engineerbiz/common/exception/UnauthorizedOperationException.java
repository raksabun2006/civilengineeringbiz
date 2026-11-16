package com.engineerbiz.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user attempts an operation lacking required granular permission.
 */
public class UnauthorizedOperationException extends BaseException {

    public UnauthorizedOperationException(String message) {
        super(message, "FORBIDDEN_OPERATION", HttpStatus.FORBIDDEN);
    }

    public UnauthorizedOperationException(String action, String requiredPermission) {
        super(String.format("You are not authorized to perform '%s'. Missing permission: '%s'", action, requiredPermission),
                "FORBIDDEN_OPERATION",
                HttpStatus.FORBIDDEN);
    }
}
