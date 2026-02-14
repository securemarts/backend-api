package com.shopper.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Update logistics hub")
public class UpdateLogisticsHubRequest {

    @Size(max = 50)
    private String state;

    @Size(max = 100)
    private String city;

    @Size(max = 255)
    private String name;

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Size(max = 500)
    private String address;

    private Boolean active;
}
