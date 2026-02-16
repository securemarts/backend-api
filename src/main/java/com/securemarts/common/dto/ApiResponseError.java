package com.securemarts.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error payload inside ApiResponse")
public class ApiResponseError {

    @Schema(description = "Error code", example = "ZONE_NOT_FOUND")
    private String code;

    @Schema(description = "Human-readable message")
    private String message;
}
