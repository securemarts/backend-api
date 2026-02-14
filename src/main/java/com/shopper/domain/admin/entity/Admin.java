package com.shopper.domain.admin.entity;

import com.shopper.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "admins", indexes = {
        @Index(name = "idx_admins_email", columnList = "email"),
        @Index(name = "idx_admins_active", columnList = "active")
})
@Getter
@Setter
public class Admin extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "admin_roles",
            joinColumns = @JoinColumn(name = "admin_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<PlatformRole> platformRoles = new HashSet<>();

    @Column(nullable = false)
    private boolean active = true;

    /** Role codes for JWT and API responses (derived from platformRoles). */
    public Set<String> getRoleCodes() {
        return platformRoles == null ? Set.of() : platformRoles.stream()
                .map(PlatformRole::getCode)
                .collect(Collectors.toSet());
    }

    public boolean hasRole(String role) {
        return getRoleCodes().contains(role);
    }

    /** Kept for invite flow and validation; prefer resolving to PlatformRole when possible. */
    public enum AdminRole {
        SUPERUSER,
        PLATFORM_ADMIN,
        SUPPORT
    }
}
