package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Create logistics hub")
public class CreateLogisticsHubRequest {

    @NotBlank
    @Size(max = 50)
    private String state;

    @NotBlank
    @Size(max = 100)
    private String city;

    @NotBlank
    @Size(max = 255)
    private String name;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Size(max = 500)
    private String address;

    private boolean active = true;
}
