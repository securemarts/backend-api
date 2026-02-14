package com.shopper.domain.admin.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "admin_invites", indexes = {
        @Index(name = "idx_admin_invites_invite_token", columnList = "invite_token"),
        @Index(name = "idx_admin_invites_expires_at", columnList = "expires_at")
}, uniqueConstraints = @UniqueConstraint(name = "idx_admin_invites_email_token", columnNames = {"email", "invite_token"}))
@Getter
@Setter
public class AdminInvite extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Admin.AdminRole role;

    @Column(name = "invite_token", nullable = false, unique = true, length = 64)
    private String inviteToken;

    @Column(name = "invited_by_admin_id", nullable = false)
    private Long invitedByAdminId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;
}
