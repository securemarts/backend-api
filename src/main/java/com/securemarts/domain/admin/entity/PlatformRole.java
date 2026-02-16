package com.securemarts.domain.admin.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "platform_roles", indexes = {
        @Index(name = "idx_platform_roles_code", columnList = "code")
})
@Getter
@Setter
public class PlatformRole extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(length = 100)
    private String name;

    @Column(length = 255)
    private String description;
}
