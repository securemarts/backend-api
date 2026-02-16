package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByPublicIdAndStoreId(String publicId, Long storeId);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND (p.deletedAt IS NULL)")
    Page<Product> findAllByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.status = :status AND (p.deletedAt IS NULL)")
    Page<Product> findAllByStoreIdAndStatus(Long storeId, Product.ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND (p.deletedAt IS NULL) AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) OR :q IS NULL)")
    Page<Product> searchByStore(Long storeId, String q, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.storeId = :storeId AND p.status = :status AND (p.deletedAt IS NULL) AND " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Product> searchByStoreAndStatus(Long storeId, Product.ProductStatus status, String q, Pageable pageable);

    boolean existsByStoreIdAndHandle(Long storeId, String handle);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.storeId = :storeId AND p.deletedAt IS NULL")
    long countByStoreIdAndDeletedAtIsNull(Long storeId);
}
