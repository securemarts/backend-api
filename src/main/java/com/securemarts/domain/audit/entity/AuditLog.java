package com.securemarts.domain.audit.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_logs_created_at", columnList = "createdAt"),
        @Index(name = "idx_audit_logs_module_created", columnList = "module, createdAt"),
        @Index(name = "idx_audit_logs_actor_created", columnList = "actorPublicId, createdAt")
})
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, length = 36, updatable = false)
    private String publicId;

    @Column(name = "actor_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActorType actorType;

    @Column(name = "actor_public_id", length = 36)
    private String actorPublicId;

    @Column(name = "actor_label", length = 255)
    private String actorLabel;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 50)
    private String module;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (publicId == null) {
            publicId = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public enum ActorType {
        ADMIN,
        MERCHANT,
        RIDER,
        SYSTEM
    }
}
