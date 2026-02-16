package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Approve or reject rider KYC")
public class RiderVerificationRequest {

    @NotBlank
    @Schema(description = "APPROVED or REJECTED", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"APPROVED", "REJECTED"})
    private String status;

    @Schema(description = "Required when status is REJECTED")
    private String rejectionReason;
}
