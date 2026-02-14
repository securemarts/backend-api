package com.shopper.domain.onboarding.repository;

import com.shopper.domain.onboarding.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByPublicId(String publicId);

    Optional<Store> findByDomainSlug(String domainSlug);

    boolean existsByDomainSlug(String domainSlug);

    List<Store> findByBusinessId(Long businessId);

    List<Store> findByBusinessIdIn(List<Long> businessIds);

    @Query("SELECT s FROM Store s WHERE s.business IN (SELECT bo.business FROM BusinessOwner bo WHERE bo.userId = :userId)")
    List<Store> findByOwnerUserId(Long userId);

    List<Store> findByActiveTrue();

    @Query("SELECT s FROM Store s JOIN s.profile p WHERE s.active = true AND p.state = :state AND p.city = :city")
    List<Store> findActiveByProfileStateAndCity(@Param("state") String state, @Param("city") String city);

    @Query("SELECT s FROM Store s JOIN s.business b WHERE s.active = true AND (LOWER(s.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.tradeName) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Store> findActiveByNameOrTradeName(@Param("q") String q);

    @Query("SELECT s FROM Store s JOIN s.profile p WHERE s.active = true AND p.latitude IS NOT NULL AND p.longitude IS NOT NULL")
    List<Store> findActiveWithGeo();
}
