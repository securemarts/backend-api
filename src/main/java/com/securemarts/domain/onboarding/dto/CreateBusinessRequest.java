package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Create business (step after user verification)")
public class CreateBusinessRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Legal business name", example = "Acme Ventures Ltd", requiredMode = Schema.RequiredMode.REQUIRED)
    private String legalName;

    @Size(max = 255)
    @Schema(description = "Trading name", example = "Acme Store")
    private String tradeName;

    @Size(max = 50)
    @Schema(description = "CAC registration number", example = "RC123456")
    private String cacNumber;

    @Size(max = 36)
    @Schema(description = "Business type publicId from /onboarding/business-types", example = "e7f3ff7c-8f11-4a6a-8f47-3e5b8d9e12ab", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessTypePublicId;
}
