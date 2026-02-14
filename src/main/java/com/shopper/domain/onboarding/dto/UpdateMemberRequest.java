package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Update member: roles and/or status (deactivate/reactivate)")
public class UpdateMemberRequest {

    @Schema(description = "Role codes: MANAGER, CASHIER, STAFF (one or more). If provided, replaces existing roles.")
    private java.util.List<String> roles;

    @Schema(description = "Status: ACTIVE (grant access), DEACTIVATED (revoke access, member remains in list). Invited members can be activated via add/accept flow.")
    private String status;
}
