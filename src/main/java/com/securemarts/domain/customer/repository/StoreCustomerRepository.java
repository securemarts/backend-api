package com.securemarts.domain.customer.repository;

import com.securemarts.domain.customer.entity.StoreCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreCustomerRepository extends JpaRepository<StoreCustomer, Long> {

    Optional<StoreCustomer> findByPublicId(String publicId);

    Optional<StoreCustomer> findByStoreIdAndPublicId(Long storeId, String publicId);

    Page<StoreCustomer> findByStoreId(Long storeId, Pageable pageable);

    @Query("SELECT c FROM StoreCustomer c WHERE c.storeId = :storeId AND " +
            "(LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR c.phone LIKE CONCAT('%', :q, '%'))")
    Page<StoreCustomer> findByStoreIdAndSearch(@Param("storeId") Long storeId, @Param("q") String q, Pageable pageable);

    boolean existsByStoreIdAndPhone(Long storeId, String phone);
}
