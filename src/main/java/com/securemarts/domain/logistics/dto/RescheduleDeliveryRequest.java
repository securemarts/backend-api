package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Data
@Schema(description = "Reschedule failed delivery for reattempt")
public class RescheduleDeliveryRequest {

    @Schema(description = "New scheduled date/time", example = "2026-03-15T14:00:00Z")
    private Instant scheduledAt;
}
