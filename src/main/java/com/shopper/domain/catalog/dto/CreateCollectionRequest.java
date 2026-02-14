package com.shopper.domain.catalog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create collection")
public class CreateCollectionRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Collection title", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Size(max = 100)
    @Schema(description = "URL handle (auto-generated from title if blank)")
    private String handle;

    @Schema(description = "Description")
    private String description;
}
