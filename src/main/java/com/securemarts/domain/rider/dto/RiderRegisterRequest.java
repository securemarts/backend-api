package com.securemarts.domain.rider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Rider self-registration (onboarding)")
public class RiderRegisterRequest {

    @NotBlank
    @Email
    @Schema(description = "Email", example = "rider@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(description = "Password (min 8 chars)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "First name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Last name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Size(max = 20)
    @Schema(description = "Phone number (optional)")
    private String phone;
}
