package com.shopper.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create rider (admin); password in plain text for initial setup")
public class CreateRiderRequest {

    private String phone;

    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    /** Service zone the rider operates in */
    private String zonePublicId;
}
