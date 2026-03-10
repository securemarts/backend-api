package com.securemarts.domain.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "User (owner or member) associated with a business")
public class AdminBusinessUserSummary {

    private String userPublicId;
    private String email;
    private String firstName;
    private String lastName;
    @Schema(description = "OWNER or MEMBER")
    private String role;
    @Schema(description = "True when this user is the primary owner of the business")
    private Boolean primaryOwner;
    @Schema(description = "For members: INVITED, ACTIVE, DEACTIVATED")
    private String memberStatus;
}
