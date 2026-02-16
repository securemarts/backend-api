package com.securemarts.domain.rider.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Complete delivery (optional POD payload)")
public class CompleteDeliveryRequest {

    @Schema(description = "Proof of delivery type (if uploading file separately, can be null)", allowableValues = {"SIGNATURE", "PHOTO"})
    private String podType;

    @Schema(description = "POD payload (e.g. base64 signature); use upload endpoint for file")
    private String podPayload;
}
