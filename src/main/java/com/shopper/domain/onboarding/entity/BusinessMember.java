package com.shopper.domain.onboarding.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "business_members", indexes = {
        @Index(name = "idx_business_members_business_id", columnList = "business_id"),
        @Index(name = "idx_business_members_user_id", columnList = "user_id"),
        @Index(name = "idx_business_members_email", columnList = "email"),
        @Index(name = "idx_business_members_invite_token", columnList = "invite_token")
}, uniqueConstraints = @UniqueConstraint(name = "uq_business_member_email", columnNames = {"business_id", "email"}))
@Getter
@Setter
public class BusinessMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "business_member_roles",
            joinColumns = @JoinColumn(name = "business_member_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<MerchantRole> merchantRoles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MemberStatus status = MemberStatus.INVITED;

    @Column(name = "invite_token", length = 64)
    private String inviteToken;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    /** Role codes for this member (derived from merchantRoles). */
    public Set<String> getRoleCodes() {
        return merchantRoles == null ? Set.of() : merchantRoles.stream()
                .map(MerchantRole::getCode)
                .collect(Collectors.toSet());
    }

    public enum MemberStatus {
        INVITED,
        ACTIVE,
        DEACTIVATED
    }
}
