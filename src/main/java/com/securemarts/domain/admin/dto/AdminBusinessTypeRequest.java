package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Admin request to create or update a business type")
public class AdminBusinessTypeRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Unique code (e.g. RESTAURANT)", example = "RESTAURANT")
    private String code;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Display name", example = "Restaurant / Food")
    private String name;

    @Schema(description = "Optional description")
    private String description;

    @Schema(description = "Optional icon key for frontend use", example = "restaurant")
    private String iconKey;
}

