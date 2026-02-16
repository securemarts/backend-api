package com.securemarts.domain.auth.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_phone", columnList = "phone")
})
@Getter
@Setter
public class User extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserType userType;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean phoneVerified;

    @Column(nullable = false)
    private boolean locked;

    @Column
    private Instant lockedUntil;

    @Column
    private Integer failedLoginAttempts;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public boolean isLocked() {
        if (!locked) return false;
        if (lockedUntil != null && Instant.now().isAfter(lockedUntil)) {
            setLocked(false);
            setLockedUntil(null);
            setFailedLoginAttempts(0);
            return false;
        }
        return true;
    }
}
