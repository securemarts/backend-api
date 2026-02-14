package com.shopper.domain.rider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Rider login (phone or email + password)")
public class RiderLoginRequest {

    @Schema(description = "Phone number (optional if email provided)")
    private String phone;

    @Schema(description = "Email (optional if phone provided)")
    private String email;

    @NotBlank
    @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
