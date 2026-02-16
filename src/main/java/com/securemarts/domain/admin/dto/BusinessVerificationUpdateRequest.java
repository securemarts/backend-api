package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Approve or reject business verification")
public class BusinessVerificationUpdateRequest {

    @NotBlank
    @Schema(description = "Verification status", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"APPROVED", "REJECTED"}, example = "APPROVED")
    private String status;

    @Schema(description = "Rejection reason (required when status is REJECTED)")
    private String rejectionReason;
}
