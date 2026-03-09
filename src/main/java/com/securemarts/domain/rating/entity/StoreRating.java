package com.securemarts.domain.rating.entity;

import com.securemarts.common.entity.BaseEntity;
import com.securemarts.domain.auth.entity.User;
import com.securemarts.domain.onboarding.entity.Store;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "store_ratings", indexes = {
        @Index(name = "idx_store_ratings_store_id", columnList = "store_id"),
        @Index(name = "idx_store_ratings_user_id", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_store_ratings_user_store", columnNames = {"user_id", "store_id"})
})
@Getter
@Setter
public class StoreRating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, columnDefinition = "smallint")
    private Short score;

    @Column(columnDefinition = "TEXT")
    private String comment;
}
