package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Upload compliance document (CAC, TIN, ID)")
public class UploadComplianceDocumentRequest {

    @NotBlank
    @Size(max = 50)
    @Schema(description = "Document type", example = "CAC_CERTIFICATE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String documentType;

    @NotBlank
    @Size(max = 500)
    @Schema(description = "URL of uploaded file (after upload to storage)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    @Size(max = 255)
    @Schema(description = "Original file name")
    private String fileName;

    @Size(max = 50)
    @Schema(description = "MIME type")
    private String mimeType;
}
