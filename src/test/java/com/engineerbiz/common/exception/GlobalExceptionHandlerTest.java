package com.engineerbiz.common.exception;

import com.engineerbiz.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should map ResourceNotFoundException to 404 response")
    void testHandleResourceNotFoundException() {
        UUID id = UUID.randomUUID();
        ResourceNotFoundException ex = new ResourceNotFoundException("Client", id);

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().message()).contains(id.toString());
    }

    @Test
    @DisplayName("Should map DuplicateResourceException to 409 response")
    void testHandleDuplicateResourceException() {
        DuplicateResourceException ex = new DuplicateResourceException("User", "email", "engineer@biz.com");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo("DUPLICATE_RESOURCE");
    }

    @Test
    @DisplayName("Should map BusinessRuleException to 422 response")
    void testHandleBusinessRuleException() {
        BusinessRuleException ex = new BusinessRuleException("Payment exceeds invoice outstanding balance");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo("BUSINESS_RULE_VIOLATION");
    }

    @Test
    @DisplayName("Should map UnauthorizedOperationException to 403 response")
    void testHandleUnauthorizedOperationException() {
        UnauthorizedOperationException ex = new UnauthorizedOperationException("Approve Estimate", "ESTIMATE_APPROVE");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleBaseException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo("FORBIDDEN_OPERATION");
        assertThat(response.getBody().message()).contains("ESTIMATE_APPROVE");
    }

    @Test
    @DisplayName("Should map MissingServletRequestParameterException to 400 response")
    void testHandleMissingServletRequestParameterException() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("status", "QuotationStatus");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleMissingServletRequestParameter(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo("MISSING_PARAMETER");
        assertThat(response.getBody().message()).contains("status");
    }

    @Test
    @DisplayName("Should map IllegalArgumentException to 400 response")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid quotation status value");

        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().message()).isEqualTo("Invalid quotation status value");
    }
}
