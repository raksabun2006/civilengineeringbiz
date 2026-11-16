package com.engineerbiz.common.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("Should build successful response with payload")
    void testSuccessResponseWithData() {
        String data = "Civil Engineering Project";
        ApiResponse<String> response = ApiResponse.success("Created successfully", data);

        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("Created successfully");
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.code()).isNull();
        assertThat(response.errors()).isNull();
        assertThat(response.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should build error response with error code and messages")
    void testErrorResponseWithErrors() {
        List<String> errors = List.of("Field 'name' cannot be blank", "Field 'phone' is invalid");
        ApiResponse<Void> response = ApiResponse.error("Validation failed", "VALIDATION_ERROR", errors);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Validation failed");
        assertThat(response.code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.errors()).containsExactlyElementsOf(errors);
        assertThat(response.data()).isNull();
        assertThat(response.timestamp()).isNotNull();
    }
}
