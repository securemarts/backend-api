package com.securemarts.domain.rider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Update delivery location (tracking)")
public class UpdateLocationRequest {

    @Schema(description = "Latitude", example = "6.5244")
    private BigDecimal latitude;

    @Schema(description = "Longitude", example = "3.3792")
    private BigDecimal longitude;

    @Schema(description = "Optional note", example = "At customer gate")
    private String note;
}
