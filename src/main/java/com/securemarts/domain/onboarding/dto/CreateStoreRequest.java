package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create store under a business")
public class CreateStoreRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Store name", example = "Acme Main Store", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]*[a-z0-9]$", message = "Slug must be lowercase alphanumeric with hyphens")
    @Schema(description = "URL-friendly slug", example = "acme-main", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainSlug;

    @Size(min = 3, max = 3)
    @Schema(description = "Default currency", example = "NGN")
    private String defaultCurrency = "NGN";
}
