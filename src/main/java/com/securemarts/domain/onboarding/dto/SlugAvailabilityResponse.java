package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Slug availability check result")
public class SlugAvailabilityResponse {

    @Schema(description = "The slug that was checked", example = "acme-main")
    private String slug;

    @Schema(description = "Whether the slug is available for use", example = "true")
    private boolean available;
}
