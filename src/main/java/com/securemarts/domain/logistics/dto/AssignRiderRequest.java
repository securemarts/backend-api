package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Assign rider to delivery order")
public class AssignRiderRequest {

    @NotBlank
    @Schema(description = "Rider public ID to assign", example = "r1a2b3c4-d5e6-7890-abcd-ef1234567890", requiredMode = Schema.RequiredMode.REQUIRED)
    private String riderPublicId;
}
