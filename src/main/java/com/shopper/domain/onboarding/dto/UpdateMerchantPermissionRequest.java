package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update merchant permission (description)")
public class UpdateMerchantPermissionRequest {

    @Size(max = 255)
    private String description;
}
