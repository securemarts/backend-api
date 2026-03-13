package com.securemarts.domain.catalog.service;

import com.securemarts.domain.catalog.entity.Collection;
import com.securemarts.domain.catalog.repository.CollectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Recomputes smart collection membership asynchronously when catalog or inventory changes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecomputeSmartCollectionsService {

    private final CollectionRepository collectionRepository;
    private final CollectionService collectionService;

    @Async
    @Transactional
    public void recomputeForStore(Long storeId) {
        List<Collection> smart = collectionRepository.findByStoreIdAndCollectionTypeWithRules(storeId, Collection.CollectionType.SMART);
        for (Collection c : smart) {
            try {
                collectionService.recomputeSmartCollection(c);
            } catch (Exception e) {
                log.warn("Recompute failed for collection {}: {}", c.getPublicId(), e.getMessage());
            }
        }
    }

    @Async
    @Transactional
    public void recomputeForCollection(Long collectionId) {
        collectionRepository.findByIdWithRules(collectionId)
                .filter(c -> c.getCollectionType() == Collection.CollectionType.SMART)
                .ifPresent(collectionService::recomputeSmartCollection);
    }
}
