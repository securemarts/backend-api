package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Update member: roles and/or status (deactivate/reactivate)")
public class UpdateMemberRequest {

    @Schema(description = "Role codes: MANAGER, CASHIER, STAFF (one or more). If provided, replaces existing roles.", allowableValues = {"MANAGER", "CASHIER", "STAFF"})
    private java.util.List<String> roles;

    @Schema(description = "Member status: ACTIVE (grant access), DEACTIVATED (revoke access).", allowableValues = {"INVITED", "ACTIVE", "DEACTIVATED"})
    private String status;
}
