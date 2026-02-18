package com.securemarts.domain.favorite.repository;

import com.securemarts.domain.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query(value = "SELECT f FROM Favorite f JOIN FETCH f.store JOIN FETCH f.product WHERE f.user.publicId = :userPublicId",
            countQuery = "SELECT COUNT(f) FROM Favorite f WHERE f.user.publicId = :userPublicId")
    Page<Favorite> findAllByUserPublicId(String userPublicId, Pageable pageable);

    @Query(value = "SELECT f FROM Favorite f JOIN FETCH f.store JOIN FETCH f.product WHERE f.user.publicId = :userPublicId AND f.store.publicId = :storePublicId",
            countQuery = "SELECT COUNT(f) FROM Favorite f WHERE f.user.publicId = :userPublicId AND f.store.publicId = :storePublicId")
    Page<Favorite> findAllByUserPublicIdAndStorePublicId(String userPublicId, String storePublicId, Pageable pageable);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.store JOIN FETCH f.product WHERE f.user.publicId = :userPublicId AND f.store.publicId = :storePublicId AND f.product.publicId = :productPublicId")
    Optional<Favorite> findByUserPublicIdAndStorePublicIdAndProductPublicId(@Param("userPublicId") String userPublicId, @Param("storePublicId") String storePublicId, @Param("productPublicId") String productPublicId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.user.publicId = :userPublicId AND f.store.publicId = :storePublicId AND f.product.publicId = :productPublicId")
    boolean existsByUserPublicIdAndStorePublicIdAndProductPublicId(@Param("userPublicId") String userPublicId, @Param("storePublicId") String storePublicId, @Param("productPublicId") String productPublicId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.store JOIN FETCH f.product WHERE f.publicId = :favoritePublicId AND f.user.publicId = :userPublicId")
    Optional<Favorite> findByPublicIdAndUserPublicId(@Param("favoritePublicId") String favoritePublicId, @Param("userPublicId") String userPublicId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.publicId = :userPublicId AND f.publicId = :favoritePublicId")
    void deleteByUserPublicIdAndPublicId(@Param("userPublicId") String userPublicId, @Param("favoritePublicId") String favoritePublicId);
}
