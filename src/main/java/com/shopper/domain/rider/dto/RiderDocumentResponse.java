package com.shopper.domain.rider.dto;

import com.shopper.domain.logistics.entity.RiderDocument;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@Schema(description = "Rider KYC document")
public class RiderDocumentResponse {

    private String publicId;
    private String documentType;
    private String fileUrl;
    private String fileName;
    private Instant createdAt;

    public static RiderDocumentResponse from(RiderDocument d) {
        return RiderDocumentResponse.builder()
                .publicId(d.getPublicId())
                .documentType(d.getDocumentType())
                .fileUrl(d.getFileUrl())
                .fileName(d.getFileName())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
