package com.securemarts.domain.rating.repository;

import com.securemarts.domain.rating.entity.StoreRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRatingRepository extends JpaRepository<StoreRating, Long> {

    @Query("SELECT r FROM StoreRating r JOIN FETCH r.store JOIN FETCH r.user WHERE r.user.publicId = :userPublicId AND r.store.publicId = :storePublicId")
    Optional<StoreRating> findByUserPublicIdAndStorePublicId(@Param("userPublicId") String userPublicId, @Param("storePublicId") String storePublicId);

    boolean existsByUserPublicIdAndStorePublicId(String userPublicId, String storePublicId);

    @Query(value = "SELECT r FROM StoreRating r JOIN FETCH r.user WHERE r.store.id = :storeId ORDER BY r.updatedAt DESC",
            countQuery = "SELECT COUNT(r) FROM StoreRating r WHERE r.store.id = :storeId")
    Page<StoreRating> findAllByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.score), 0) FROM StoreRating r WHERE r.store.id = :storeId")
    Double getAverageScoreByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT COUNT(r) FROM StoreRating r WHERE r.store.id = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);
}
