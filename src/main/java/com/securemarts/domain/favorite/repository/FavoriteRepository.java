package com.securemarts.domain.favorite.repository;

import com.securemarts.domain.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f FROM Favorite f JOIN FETCH f.store JOIN FETCH f.product WHERE f.user.publicId = :userPublicId")
    Page<Favorite> findAllByUserPublicId(String userPublicId, Pageable pageable);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.store JOIN FETCH f.product WHERE f.user.publicId = :userPublicId AND f.store.publicId = :storePublicId")
    Page<Favorite> findAllByUserPublicIdAndStorePublicId(String userPublicId, String storePublicId, Pageable pageable);

    Optional<Favorite> findByUserPublicIdAndStorePublicIdAndProductPublicId(String userPublicId, String storePublicId, String productPublicId);

    boolean existsByUserPublicIdAndStorePublicIdAndProductPublicId(String userPublicId, String storePublicId, String productPublicId);

    Optional<Favorite> findByPublicIdAndUserPublicId(String favoritePublicId, String userPublicId);

    void deleteByUserPublicIdAndPublicId(String userPublicId, String favoritePublicId);
}
