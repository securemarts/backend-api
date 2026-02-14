package com.shopper.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Email or phone verification request")
public class VerifyRequest {

    @NotBlank
    @Schema(description = "Verification token/code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
}
