package com.shopper.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Add bank account for payouts")
public class AddBankAccountRequest {

    @NotBlank
    @Size(max = 10)
    @Schema(description = "Bank code (e.g. 058 for GTBank)", example = "058", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankCode;

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Bank name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bankName;

    @NotBlank
    @Size(max = 10)
    @Schema(description = "Account number", example = "0123456789", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountNumber;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "Account name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountName;

    @Schema(description = "Use as default for payouts")
    private boolean payoutDefault;
}
