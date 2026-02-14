package com.shopper.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Assign rider to delivery order")
public class AssignRiderRequest {

    @NotBlank
    private String riderPublicId;
}
