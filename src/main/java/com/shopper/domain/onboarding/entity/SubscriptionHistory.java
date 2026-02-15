package com.shopper.domain.onboarding.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "subscription_history", indexes = {
        @Index(name = "idx_subscription_history_business_created", columnList = "business_id, created_at")
})
@Getter
@Setter
public class SubscriptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 20)
    private String plan;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "paystack_subscription_code", length = 100)
    private String paystackSubscriptionCode;

    @Column(name = "period_start")
    private Instant periodStart;

    @Column(name = "period_end")
    private Instant periodEnd;

    @Column(nullable = false, length = 20)
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public enum EventType {
        TRIAL_STARTED,
        ACTIVATED,
        RENEWED,
        CANCELLED,
        PAST_DUE,
        UPDATED
    }

    public enum Source {
        WEBHOOK,
        START_TRIAL,
        ADMIN,
        APP
    }
}
