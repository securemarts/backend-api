package com.securemarts.domain.storefront.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.dto.CollectionResponse;
import com.securemarts.domain.catalog.dto.MenuResponse;
import com.securemarts.domain.catalog.dto.ProductResponse;
import com.securemarts.domain.catalog.entity.Collection;
import com.securemarts.domain.catalog.repository.CollectionProductRepository;
import com.securemarts.domain.catalog.repository.CollectionRepository;
import com.securemarts.domain.catalog.service.CatalogService;
import com.securemarts.domain.catalog.service.MenuService;
import com.securemarts.domain.rating.service.StoreRatingService;
import com.securemarts.domain.storefront.dto.StorefrontStoreDto;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StorefrontService {

    private final StoreRepository storeRepository;
    private final CatalogService catalogService;
    private final StoreRatingService storeRatingService;
    private final MenuService menuService;
    private final CollectionRepository collectionRepository;
    private final CollectionProductRepository collectionProductRepository;

    private static final String ACTIVE_STATUS = "ACTIVE";

    @Transactional(readOnly = true)
    public StorefrontStoreDto getStoreBySlug(String storeSlug) {
        Store store = storeRepository.findByDomainSlug(storeSlug)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeSlug));
        if (!store.isActive()) {
            throw new ResourceNotFoundException("Store", storeSlug);
        }
        double avg = storeRatingService.getAverageRating(store.getId());
        long count = storeRatingService.getRatingCount(store.getId());
        return StorefrontStoreDto.builder()
                .publicId(store.getPublicId())
                .name(store.getName())
                .domainSlug(store.getDomainSlug())
                .defaultCurrency(store.getDefaultCurrency())
                .averageRating(count > 0 ? avg : null)
                .ratingCount(count > 0 ? (int) count : 0)
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

    @Transactional(readOnly = true)
    public MenuResponse getMenuByHandle(String storeSlug, String handle) {
        Store store = resolveActiveStore(storeSlug);
        return menuService.getByHandle(store.getId(), handle);
    }

    @Transactional(readOnly = true)
    public List<CollectionResponse> listCollections(String storeSlug) {
        Store store = resolveActiveStore(storeSlug);
        return collectionRepository.findByStoreId(store.getId()).stream()
                .map(c -> CollectionResponse.from(c, collectionProductRepository.countByCollectionId(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionResponse getCollectionByHandle(String storeSlug, String collectionHandle) {
        Store store = resolveActiveStore(storeSlug);
        Collection collection = collectionRepository.findByHandleAndStoreId(collectionHandle, store.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionHandle));
        return CollectionResponse.from(collection, collectionProductRepository.countByCollectionId(collection.getId()));
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
