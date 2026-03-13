package com.securemarts.domain.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create or update location")
public class LocationRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Location name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Main Warehouse")
    private String name;

    @Schema(description = "Address", example = "12 Broad Street, Lagos Island, Lagos")
    private String address;
}
