package com.shopper.domain.pos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create POS register")
public class CreatePOSRegisterRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    private Long locationId;
    private String deviceId;
    private boolean active = true;
}
