package com.shopper.domain.onboarding.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "businesses", indexes = {
        @Index(name = "idx_businesses_cac_number", columnList = "cac_number"),
        @Index(name = "idx_businesses_tax_id", columnList = "tax_id")
})
@Getter
@Setter
public class Business extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String legalName;

    @Column(length = 255)
    private String tradeName;

    @Column(name = "cac_number", length = 50)
    private String cacNumber;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 30)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Store> stores = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessOwner> owners = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComplianceDocument> complianceDocuments = new ArrayList<>();

    @OneToOne(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private BusinessVerification verification;

    public enum VerificationStatus {
        PENDING,
        UNDER_REVIEW,
        APPROVED,
        REJECTED
    }
}
