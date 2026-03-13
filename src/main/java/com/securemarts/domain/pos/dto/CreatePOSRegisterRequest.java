package com.securemarts.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create POS register")
public class CreatePOSRegisterRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Register name", example = "Front Counter", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Location ID (internal, optional)")
    private Long locationId;

    @Schema(description = "Device identifier (e.g. iPad serial)", example = "IPAD-001")
    private String deviceId;

    @Schema(description = "Whether register is active", example = "true")
    private boolean active = true;
}
