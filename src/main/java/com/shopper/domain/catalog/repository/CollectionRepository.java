package com.shopper.domain.catalog.repository;

import com.shopper.domain.catalog.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    Optional<Collection> findByPublicIdAndBusinessId(String publicId, Long businessId);

    List<Collection> findByBusinessId(Long businessId);
}
