package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Update rider")
public class UpdateRiderRequest {

    private String phone;
    private String email;

    @Size(min = 8)
    private String password;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Schema(description = "Rider status", allowableValues = {"AVAILABLE", "BUSY", "OFF_DUTY"})
    private String status;
    private String zonePublicId;
}
