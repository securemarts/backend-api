package com.shopper.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Approve or reject business verification")
public class BusinessVerificationUpdateRequest {

    @NotBlank
    @Schema(description = "Status: APPROVED or REJECTED", requiredMode = Schema.RequiredMode.REQUIRED, example = "APPROVED")
    private String status;

    @Schema(description = "Rejection reason (required when status is REJECTED)")
    private String rejectionReason;
}
