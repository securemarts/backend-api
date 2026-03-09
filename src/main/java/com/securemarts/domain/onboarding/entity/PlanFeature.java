package com.securemarts.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "plan_features", indexes = {
        @Index(name = "idx_plan_features_plan_id", columnList = "plan_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_plan_features_plan_key", columnNames = {"plan_id", "feature_key"})
})
@Getter
@Setter
public class PlanFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "feature_key", nullable = false, length = 80)
    private String featureKey;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "limit_value")
    private Integer limitValue;
}
