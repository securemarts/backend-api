package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update merchant role (name, description)")
public class UpdateMerchantRoleRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;
}
