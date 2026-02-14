package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Add an existing platform user as staff by their user public ID")
public class AddMemberRequest {

    @NotBlank
    @Schema(description = "User public ID (from auth/register)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userPublicId;

    @NotNull
    @Schema(description = "Role: MANAGER, CASHIER, STAFF", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}
