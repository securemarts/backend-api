package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.CollectionProduct;
import com.securemarts.domain.catalog.entity.CollectionProductId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionProductRepository extends JpaRepository<CollectionProduct, CollectionProductId> {

    List<CollectionProduct> findByCollectionIdOrderByPositionAscProductIdAsc(Long collectionId);

    @EntityGraph(attributePaths = "product")
    Page<CollectionProduct> findByCollectionIdOrderByPositionAscProductIdAsc(Long collectionId, Pageable pageable);

    boolean existsByCollectionIdAndProductId(Long collectionId, Long productId);

    Optional<CollectionProduct> findByCollectionIdAndProductId(Long collectionId, Long productId);

    @Modifying
    @Query("DELETE FROM CollectionProduct cp WHERE cp.collectionId = :collectionId")
    void deleteByCollectionId(@Param("collectionId") Long collectionId);

    long countByCollectionId(Long collectionId);
}
