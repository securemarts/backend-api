package com.securemarts.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "JWT token response")
public class TokenResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "Refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType;

    @Schema(description = "Expires in seconds")
    private long expiresIn;

    @Schema(description = "User public ID")
    private String userId;

    @Schema(description = "User email")
    private String email;

    @Schema(description = "User roles")
    private List<String> roles;

    @Schema(description = "Permission codes (admin RBAC scopes); used for UI to show allowed actions")
    private List<String> scopes;

    @Schema(description = "Active store ID (if merchant)")
    private Long storeId;
}
