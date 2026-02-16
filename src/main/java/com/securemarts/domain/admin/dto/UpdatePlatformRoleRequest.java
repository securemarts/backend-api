package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update platform role (name, description; code is immutable)")
public class UpdatePlatformRoleRequest {

    @Size(max = 100)
    @Schema(description = "Display name")
    private String name;

    @Size(max = 255)
    @Schema(description = "Description")
    private String description;
}
