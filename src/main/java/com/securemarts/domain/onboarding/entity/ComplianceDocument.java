package com.securemarts.domain.onboarding.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "compliance_documents", indexes = {
        @Index(name = "idx_compliance_docs_business_id", columnList = "business_id")
})
@Getter
@Setter
public class ComplianceDocument extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, length = 50)
    private String documentType; // CAC_CERTIFICATE, TIN, ID_CARD, etc.

    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(length = 255)
    private String fileName;

    @Column(length = 50)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DocumentStatus status = DocumentStatus.PENDING;

    public enum DocumentStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
