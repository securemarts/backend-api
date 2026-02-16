package com.securemarts.domain.onboarding.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "business_owners", indexes = {
        @Index(name = "idx_business_owners_business_id", columnList = "business_id"),
        @Index(name = "idx_business_owners_user_id", columnList = "user_id")
})
@Getter
@Setter
public class BusinessOwner extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean primaryOwner;
}
