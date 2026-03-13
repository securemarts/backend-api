package com.securemarts.domain.catalog.repository;

import com.securemarts.domain.catalog.entity.CollectionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRuleRepository extends JpaRepository<CollectionRule, Long> {

    List<CollectionRule> findByCollectionIdOrderByPositionAsc(Long collectionId);

    void deleteByCollectionId(Long collectionId);
}
