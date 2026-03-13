package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Store settings (e.g. selling channels)")
public class StoreSettingsResponse {

    @Schema(description = "Selling channel: ONLINE, RETAIL, BOTH, or NONE")
    private String salesChannel;

    public static StoreSettingsResponse from(Store store) {
        StoreSettingsResponse r = new StoreSettingsResponse();
        r.setSalesChannel(store.getSalesChannel() != null ? store.getSalesChannel().name() : null);
        return r;
    }
}
