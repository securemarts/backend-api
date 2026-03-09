package com.securemarts.domain.invoice.repository;

import com.securemarts.domain.invoice.entity.Invoice;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByPublicId(String publicId);

    Optional<Invoice> findByStoreIdAndPublicId(Long storeId, String publicId);

    Page<Invoice> findByStoreId(Long storeId, Pageable pageable);

    Page<Invoice> findByStoreIdAndStatus(Long storeId, Invoice.InvoiceStatus status, Pageable pageable);

    Page<Invoice> findByStoreIdAndStoreCustomerId(Long storeId, Long storeCustomerId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Invoice i WHERE i.storeId = :storeId ORDER BY i.id DESC")
    List<Invoice> findTopByStoreIdOrderByIdDesc(@Param("storeId") Long storeId, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE i.storeId = :storeId " +
            "AND (:status IS NULL OR i.status = :status) " +
            "AND (:storeCustomerId IS NULL OR i.storeCustomerId = :storeCustomerId) " +
            "AND (:from IS NULL OR i.createdAt >= :from) " +
            "AND (:to IS NULL OR i.createdAt <= :to)")
    Page<Invoice> findByStoreIdAndFilters(@Param("storeId") Long storeId,
                                          @Param("status") Invoice.InvoiceStatus status,
                                          @Param("storeCustomerId") Long storeCustomerId,
                                          @Param("from") Instant from,
                                          @Param("to") Instant to,
                                          Pageable pageable);
}
