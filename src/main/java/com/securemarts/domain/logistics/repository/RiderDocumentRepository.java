package com.securemarts.domain.logistics.repository;

import com.securemarts.domain.logistics.entity.RiderDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RiderDocumentRepository extends JpaRepository<RiderDocument, Long> {

    Optional<RiderDocument> findByPublicId(String publicId);

    List<RiderDocument> findByRiderIdOrderByCreatedAtDesc(Long riderId);
}
