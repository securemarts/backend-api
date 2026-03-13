package com.securemarts.domain.onboarding.service;

import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.onboarding.dto.StoreSettingsResponse;
import com.securemarts.domain.onboarding.dto.UpdateStoreSettingsRequest;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreSettingsService {

    private final StoreRepository storeRepository;

    public Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(Store::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }

    @Transactional(readOnly = true)
    public StoreSettingsResponse get(String storePublicId) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        return StoreSettingsResponse.from(store);
    }

    @Transactional
    public StoreSettingsResponse update(String storePublicId, UpdateStoreSettingsRequest request) {
        Store store = storeRepository.findByPublicId(storePublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
        if (request.getSalesChannel() != null && !request.getSalesChannel().isBlank()) {
            store.setSalesChannel(Store.SalesChannel.valueOf(request.getSalesChannel().trim().toUpperCase()));
        }
        store = storeRepository.save(store);
        return StoreSettingsResponse.from(store);
    }
}
