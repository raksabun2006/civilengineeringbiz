package com.engineerbiz.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation violates engineering or financial business rules
 * (e.g. overpayment, invalid project state transition, unapproved estimate quotation).
 */
public class BusinessRuleException extends BaseException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_CONTENT);
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}
