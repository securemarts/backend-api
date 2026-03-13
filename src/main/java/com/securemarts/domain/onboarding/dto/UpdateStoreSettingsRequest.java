package com.securemarts.domain.onboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Update store settings")
public class UpdateStoreSettingsRequest {

    @Pattern(regexp = "^(ONLINE|RETAIL|BOTH|NONE)$", message = "salesChannel must be one of: ONLINE, RETAIL, BOTH, NONE")
    @Schema(description = "Selling channel: ONLINE (e-commerce only), RETAIL (POS only), BOTH, or NONE", example = "BOTH")
    private String salesChannel;
}
