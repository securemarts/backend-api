package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByStoreIdOrderByCreatedAtAsc(Long storeId);

    Optional<Menu> findByPublicIdAndStoreId(String publicId, Long storeId);

    Optional<Menu> findByStoreIdAndHandle(Long storeId, String handle);

    boolean existsByStoreIdAndHandle(Long storeId, String handle);

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.items WHERE m.storeId = :storeId AND m.handle = :handle")
    Optional<Menu> findByStoreIdAndHandleWithItems(@Param("storeId") Long storeId, @Param("handle") String handle);

    @Query("SELECT m FROM Menu m LEFT JOIN FETCH m.items WHERE m.publicId = :publicId AND m.storeId = :storeId")
    Optional<Menu> findByPublicIdAndStoreIdWithItems(@Param("publicId") String publicId, @Param("storeId") Long storeId);
}
