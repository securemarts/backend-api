package com.shopper.domain.rider.dto;

import com.shopper.domain.logistics.entity.Rider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Rider profile and KYC status")
public class RiderProfileResponse {

    private String publicId;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    @Schema(description = "Rider status", allowableValues = {"AVAILABLE", "BUSY", "OFF_DUTY"})
    private String status;
    @Schema(description = "KYC verification status", allowableValues = {"PENDING", "UNDER_REVIEW", "APPROVED", "REJECTED"})
    private String verificationStatus;
    private String rejectionReason;
    private String zonePublicId;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private boolean available;

    public static RiderProfileResponse from(Rider r) {
        return RiderProfileResponse.builder()
                .publicId(r.getPublicId())
                .email(r.getEmail())
                .phone(r.getPhone())
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .status(r.getStatus().name())
                .verificationStatus(r.getVerificationStatus().name())
                .rejectionReason(r.getRejectionReason())
                .zonePublicId(r.getZone() != null ? r.getZone().getPublicId() : null)
                .currentLat(r.getCurrentLat())
                .currentLng(r.getCurrentLng())
                .available(r.isAvailable())
                .build();
    }
}
