package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Invite a member by email (they will receive invite; when they register or accept, they join as staff)")
public class InviteMemberRequest {

    @NotBlank
    @Email
    @Schema(description = "Email to invite", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotNull
    @Schema(description = "Role: MANAGER, CASHIER, STAFF", requiredMode = Schema.RequiredMode.REQUIRED)
    private String role;
}
