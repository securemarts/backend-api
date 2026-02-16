package com.securemarts.domain.storefront.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.dto.ProductResponse;
import com.securemarts.domain.catalog.service.CatalogService;
import com.securemarts.domain.storefront.dto.StorefrontStoreDto;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StorefrontService {

    private final StoreRepository storeRepository;
    private final CatalogService catalogService;

    private static final String ACTIVE_STATUS = "ACTIVE";

    @Transactional(readOnly = true)
    public StorefrontStoreDto getStoreBySlug(String storeSlug) {
        Store store = storeRepository.findByDomainSlug(storeSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeSlug));
        if (!store.isActive()) {
            throw new ResourceNotFoundException("Store", storeSlug);
        }
        return StorefrontStoreDto.builder()
                .publicId(store.getPublicId())
                .name(store.getName())
                .domainSlug(store.getDomainSlug())
                .defaultCurrency(store.getDefaultCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(String storeSlug, String q, Pageable pageable) {
        Store store = resolveActiveStore(storeSlug);
        return catalogService.listProducts(store.getId(), ACTIVE_STATUS, q, pageable);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(String storeSlug, String productPublicId) {
        Store store = resolveActiveStore(storeSlug);
        ProductResponse product = catalogService.getProduct(store.getId(), productPublicId);
        if (!ACTIVE_STATUS.equals(product.getStatus())) {
            throw new ResourceNotFoundException("Product", productPublicId);
        }
        return product;
    }

    private Store resolveActiveStore(String storeSlug) {
        Store store = storeRepository.findByDomainSlug(storeSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeSlug));
        if (!store.isActive()) {
            throw new ResourceNotFoundException("Store", storeSlug);
        }
        return store;
    }
}
