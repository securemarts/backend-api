package com.shopper.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body for email verification. Supply the email address and the 6-digit OTP sent to that email.")
public class VerifyEmailRequest {

    @NotBlank
    @Email
    @Schema(description = "Email address to verify", requiredMode = Schema.RequiredMode.REQUIRED, example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 6, max = 6)
    @Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
    @Schema(description = "6-digit OTP code received by email", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String code;
}
