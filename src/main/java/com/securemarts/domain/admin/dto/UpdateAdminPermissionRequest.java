package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update admin permission (description only; code is immutable)")
public class UpdateAdminPermissionRequest {

    @Size(max = 255)
    @Schema(description = "Description")
    private String description;
}
