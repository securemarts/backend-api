package com.securemarts.domain.auth.dto;

import com.securemarts.domain.auth.entity.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank
    @Email
    @Schema(description = "Email address", example = "merchant@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(description = "Password (min 8 chars)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "First name", example = "Chidi", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Last name", example = "Okafor", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Size(max = 20)
    @Schema(description = "Phone number (optional)", example = "+2348012345678")
    private String phone;

    @NotNull
    @Schema(description = "User type", requiredMode = Schema.RequiredMode.REQUIRED, implementation = UserType.class)
    private UserType userType;
}
