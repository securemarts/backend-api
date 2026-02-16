package com.securemarts.domain.onboarding.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "business_verifications", indexes = {
        @Index(name = "idx_business_verifications_business_id", columnList = "business_id")
})
@Getter
@Setter
public class BusinessVerification extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false, unique = true)
    private Business business;

    @Column(name = "verified_by")
    private Long verifiedBy; // admin user id

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(length = 1000)
    private String notes;

    @Column(length = 500)
    private String rejectionReason;
}
