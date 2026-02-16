package com.securemarts.domain.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Assign store to a service zone (null or blank to unassign)")
public class SetStoreZoneRequest {
    private String zonePublicId;
}
