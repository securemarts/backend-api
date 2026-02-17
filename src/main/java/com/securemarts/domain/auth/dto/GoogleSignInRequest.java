package com.securemarts.domain.auth.dto;

import com.securemarts.domain.auth.entity.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Google Sign-In: ID token from Google + desired role (for new users)")
public class GoogleSignInRequest {

    @NotBlank
    @Schema(description = "Google ID token from the client (after Google Sign-In)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idToken;

    @NotNull
    @Schema(description = "User role (CUSTOMER, MERCHANT_OWNER, etc.). Used when creating a new user; existing users keep their existing role.", requiredMode = Schema.RequiredMode.REQUIRED)
    private UserType role;
}
