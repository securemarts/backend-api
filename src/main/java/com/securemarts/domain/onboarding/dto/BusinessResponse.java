package com.securemarts.domain.onboarding.dto;

import com.securemarts.domain.onboarding.entity.Business;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Business response")
public class BusinessResponse {

    private String publicId;
    private String legalName;
    private String tradeName;
    private String cacNumber;
    private String taxId;
    private String verificationStatus;
    private Instant createdAt;
    private List<StoreSummary> stores;

    public static BusinessResponse from(Business b) {
        return BusinessResponse.builder()
                .publicId(b.getPublicId())
                .legalName(b.getLegalName())
                .tradeName(b.getTradeName())
                .cacNumber(b.getCacNumber())
                .taxId(b.getTaxId())
                .verificationStatus(b.getVerificationStatus() != null ? b.getVerificationStatus().name() : "PENDING")
                .createdAt(b.getCreatedAt())
                .stores(b.getStores() != null ? b.getStores().stream()
                        .map(StoreSummary::from)
                        .collect(Collectors.toList()) : List.of())
                .build();
    }

    @Data
    @Builder
    public static class StoreSummary {
        private String publicId;
        private String name;
        private String domainSlug;
        private boolean active;

        public static StoreSummary from(com.securemarts.domain.onboarding.entity.Store s) {
            return StoreSummary.builder()
                    .publicId(s.getPublicId())
                    .name(s.getName())
                    .domainSlug(s.getDomainSlug())
                    .active(s.isActive())
                    .build();
        }
    }
}
