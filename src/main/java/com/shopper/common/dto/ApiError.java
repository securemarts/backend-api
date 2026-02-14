package com.shopper.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Schema(description = "Standard API error response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ApiError {

    @Schema(description = "Timestamp of the error", example = "2025-02-13T10:00:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type/code", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable message")
    private String message;

    @Schema(description = "Request path", example = "/api/v1/products")
    private String path;

    @Schema(description = "Field-level validation errors")
    private List<FieldError> fieldErrors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
