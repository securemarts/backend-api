package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findByPublicIdAndStoreId(String publicId, Long storeId);

    List<Collection> findByStoreId(Long storeId);

    List<Collection> findByStoreIdAndCollectionType(Long storeId, Collection.CollectionType collectionType);

    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.rules WHERE c.id = :id")
    Optional<Collection> findByIdWithRules(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Collection c LEFT JOIN FETCH c.rules WHERE c.storeId = :storeId AND c.collectionType = :type")
    List<Collection> findByStoreIdAndCollectionTypeWithRules(@Param("storeId") Long storeId, @Param("type") Collection.CollectionType type);
}
