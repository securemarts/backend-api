package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.BusinessMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Business member (staff) response")
public class BusinessMemberResponse {

    private String publicId;
    private String email;
    @Schema(description = "Role codes (e.g. MANAGER, CASHIER, STAFF); multiple if multi-role")
    private java.util.List<String> roles;
    @Schema(description = "Member status", allowableValues = {"INVITED", "ACTIVE", "DEACTIVATED"})
    private String status;
    private String userPublicId;
    private Instant invitedAt;
    private Instant joinedAt;
    private Instant createdAt;

    public static BusinessMemberResponse from(BusinessMember m) {
        return BusinessMemberResponse.builder()
                .publicId(m.getPublicId())
                .email(m.getEmail())
                .roles(m.getRoleCodes() != null ? new java.util.ArrayList<>(m.getRoleCodes()) : java.util.List.of())
                .status(m.getStatus() != null ? m.getStatus().name() : null)
                .userPublicId(null)
                .invitedAt(m.getInvitedAt())
                .joinedAt(m.getJoinedAt())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
