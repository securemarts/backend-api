package com.securemarts.domain.admin.dto;

import com.securemarts.domain.admin.entity.Admin;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Admin response")
public class AdminResponse {

    private String publicId;
    private String email;
    private String fullName;
    @Schema(description = "Admin roles (RBAC)")
    private List<String> roles;
    private boolean active;
    private Instant createdAt;

    public static AdminResponse from(Admin a) {
        return AdminResponse.builder()
                .publicId(a.getPublicId())
                .email(a.getEmail())
                .fullName(a.getFullName())
                .roles(a.getRoleCodes() != null ? List.copyOf(a.getRoleCodes()) : List.of())
                .active(a.isActive())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
