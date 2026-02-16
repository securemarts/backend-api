package com.securemarts.domain.logistics.repository;

import com.securemarts.domain.logistics.entity.LogisticsHub;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogisticsHubRepository extends JpaRepository<LogisticsHub, Long> {

    Optional<LogisticsHub> findByPublicId(String publicId);

    Page<LogisticsHub> findByStateAndCityAndActiveTrue(String state, String city, Pageable pageable);

    Page<LogisticsHub> findByStateAndActiveTrue(String state, Pageable pageable);

    Page<LogisticsHub> findByActiveTrue(Pageable pageable);
}
