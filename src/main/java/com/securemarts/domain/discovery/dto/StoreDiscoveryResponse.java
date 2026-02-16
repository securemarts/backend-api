package com.securemarts.domain.discovery.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "Store summary for discovery (geo-search, nearby). Address/geo come from locations when includeLocations=true.")
public class StoreDiscoveryResponse {

    private String publicId;
    private String name;
    private String domainSlug;
    @Schema(description = "Distance in km from query point (if lat/lng provided)")
    private Double distanceKm;
    @Schema(description = "Business trade name")
    private String brandName;
    @Schema(description = "Store logo URL (from store profile)")
    private String logoUrl;
    @Schema(description = "Store description (from store profile)")
    private String description;
    @Schema(description = "Contact email (from store profile)")
    private String contactEmail;
    @Schema(description = "Contact phone (from store profile)")
    private String contactPhone;
    @Schema(description = "Locations (when includeLocations=true)")
    private List<LocationSummaryResponse> locations;
}
