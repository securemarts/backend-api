package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Store response")
public class StoreResponse {

    private String publicId;
    private String name;
    private String domainSlug;
    private String defaultCurrency;
    private boolean active;
    private boolean paymentsEnabled;
    private String businessPublicId;
    private Instant createdAt;

    public static StoreResponse from(Store s) {
        return StoreResponse.builder()
                .publicId(s.getPublicId())
                .name(s.getName())
                .domainSlug(s.getDomainSlug())
                .defaultCurrency(s.getDefaultCurrency())
                .active(s.isActive())
                .paymentsEnabled(s.isPaymentsEnabled())
                .businessPublicId(s.getBusiness() != null ? s.getBusiness().getPublicId() : null)
                .createdAt(s.getCreatedAt())
                .build();
    }
}
