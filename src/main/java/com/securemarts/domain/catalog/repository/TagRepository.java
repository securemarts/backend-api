package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByPublicIdAndBusinessId(String publicId, Long businessId);

    List<Tag> findByBusinessId(Long businessId);

    Optional<Tag> findByBusinessIdAndName(Long businessId, String name);
}
