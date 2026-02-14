package com.shopper.domain.catalog.service;

import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.catalog.dto.CollectionResponse;
import com.shopper.domain.catalog.dto.CreateCollectionRequest;
import com.shopper.domain.catalog.entity.Collection;
import com.shopper.domain.catalog.repository.CollectionRepository;
import com.shopper.domain.onboarding.repository.BusinessRepository;
import com.shopper.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final StoreRepository storeRepository;
    private final BusinessRepository businessRepository;

    public Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.shopper.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }

    public Long resolveBusinessId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(s -> s.getBusiness().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> listByBusiness(Long businessId) {
        return collectionRepository.findByBusinessId(businessId).stream()
                .map(CollectionResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionResponse get(Long businessId, String collectionPublicId) {
        Collection c = collectionRepository.findByPublicIdAndBusinessId(collectionPublicId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionPublicId));
        return CollectionResponse.from(c);
    }

    @Transactional
    public CollectionResponse create(Long businessId, CreateCollectionRequest request) {
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business", String.valueOf(businessId));
        }
        Collection c = new Collection();
        c.setBusinessId(businessId);
        c.setTitle(request.getTitle().trim());
        String handle = request.getHandle();
        if (handle == null || handle.isBlank()) {
            handle = request.getTitle().trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            if (handle.isEmpty()) handle = "collection-" + System.currentTimeMillis();
        } else {
            handle = handle.trim().toLowerCase().replace(' ', '-');
        }
        c.setHandle(handle);
        c.setDescription(request.getDescription());
        c = collectionRepository.save(c);
        return CollectionResponse.from(c);
    }
}
