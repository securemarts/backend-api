package com.securemarts.domain.catalog.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.dto.ProductRequest;
import com.securemarts.domain.catalog.dto.ProductResponse;
import com.securemarts.domain.catalog.entity.*;
import com.securemarts.domain.catalog.repository.*;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.SubscriptionLimitsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CollectionRepository collectionRepository;
    private final TagRepository tagRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionLimitsService subscriptionLimitsService;

    private static final Set<String> PRODUCT_SORT_FIELDS = Set.of(
            "id", "publicId", "title", "handle", "status", "createdAt", "updatedAt");

    /** Sanitizes pageable so only allowed Product sort fields are used; avoids 400 on invalid sort (e.g. sort=string). */
    private Pageable sanitizeProductSort(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }
        List<Sort.Order> allowed = new ArrayList<>();
        for (Sort.Order order : pageable.getSort()) {
            if (PRODUCT_SORT_FIELDS.contains(order.getProperty())) {
                allowed.add(order);
            }
        }
        if (allowed.isEmpty()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").ascending());
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(allowed));
    }

    public Long resolveStoreId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(com.securemarts.domain.onboarding.entity.Store::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }

    public Long resolveBusinessId(String storePublicId) {
        return storeRepository.findByPublicId(storePublicId)
                .map(s -> s.getBusiness().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", storePublicId));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long storeId, String productPublicId) {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        if (product.isDeleted()) throw new ResourceNotFoundException("Product", productPublicId);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> listProducts(Long storeId, String status, String q, Pageable pageable) {
        Pageable safe = sanitizeProductSort(pageable);
        Page<Product> page;
        if (q != null && !q.isBlank() && status != null && !status.isBlank()) {
            Product.ProductStatus s = Product.ProductStatus.valueOf(status.toUpperCase());
            page = productRepository.searchByStoreAndStatus(storeId, s, q.trim(), safe);
        } else if (q != null && !q.isBlank()) {
            page = productRepository.searchByStore(storeId, q.trim(), safe);
        } else if (status != null && !status.isBlank()) {
            Product.ProductStatus s = Product.ProductStatus.valueOf(status.toUpperCase());
            page = productRepository.findAllByStoreIdAndStatus(storeId, s, safe);
        } else {
            page = productRepository.findAllByStoreId(storeId, safe);
        }
        return PageResponse.of(page.map(ProductResponse::from));
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse createProduct(Long storeId, ProductRequest request) {
        var store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        var limits = subscriptionLimitsService.getLimitsForBusiness(store.getBusiness());
        if (productRepository.countByStoreIdAndDeletedAtIsNull(storeId) >= limits.getMaxProducts()) {
            throw new BusinessRuleException("Product limit reached for your plan (" + limits.getMaxProducts() + "). Upgrade to add more products.");
        }
        Long businessId = store.getBusiness().getId();
        if (request.getHandle() != null && !request.getHandle().isBlank()
                && productRepository.existsByStoreIdAndHandle(storeId, request.getHandle())) {
            throw new BusinessRuleException("Product handle already exists");
        }
        Product product = new Product();
        product.setStoreId(storeId);
        product.setTitle(request.getTitle().trim());
        product.setHandle(request.getHandle() != null ? request.getHandle().trim().toLowerCase().replace(' ', '-') : null);
        if (product.getHandle() == null || product.getHandle().isEmpty()) {
            product.setHandle(UUID.randomUUID().toString().substring(0, 8));
        }
        product.setBodyHtml(request.getBodyHtml());
        product.setStatus(request.getStatus() != null ? Product.ProductStatus.valueOf(request.getStatus()) : Product.ProductStatus.DRAFT);
        product.setSeoTitle(request.getSeoTitle());
        product.setSeoDescription(request.getSeoDescription());
        if (request.getCollectionId() != null && !request.getCollectionId().isBlank()) {
            com.securemarts.domain.catalog.entity.Collection col = collectionRepository.findByPublicIdAndBusinessId(request.getCollectionId(), businessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Collection", request.getCollectionId()));
            product.setCollectionId(col.getId());
        }
        product = productRepository.save(product);
        syncTags(product, businessId, request.getTagNames());
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (int i = 0; i < request.getVariants().size(); i++) {
                ProductRequest.ProductVariantRequest vr = request.getVariants().get(i);
                ProductVariant v = toVariant(product, vr, i);
                product.getVariants().add(v);
            }
        } else {
            ProductVariant defaultVariant = new ProductVariant();
            defaultVariant.setProduct(product);
            defaultVariant.setTitle("Default");
            defaultVariant.setPriceAmount(BigDecimal.ZERO);
            defaultVariant.setCurrency("NGN");
            defaultVariant.setPosition(0);
            product.getVariants().add(productVariantRepository.save(defaultVariant));
        }
        if (request.getMedia() != null) {
            for (int i = 0; i < request.getMedia().size(); i++) {
                ProductRequest.ProductMediaRequest mr = request.getMedia().get(i);
                ProductMedia m = new ProductMedia();
                m.setProduct(product);
                m.setUrl(mr.getUrl());
                m.setAlt(mr.getAlt());
                m.setPosition(mr.getPosition());
                m.setMediaType(mr.getMediaType() != null ? mr.getMediaType() : "image");
                product.getMedia().add(m);
            }
        }
        product = productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse updateProduct(Long storeId, String productPublicId, ProductRequest request) {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        if (product.isDeleted()) throw new ResourceNotFoundException("Product", productPublicId);
        Long businessId = resolveBusinessIdFromStoreId(storeId);
        product.setTitle(request.getTitle().trim());
        if (request.getHandle() != null) product.setHandle(request.getHandle().trim().toLowerCase().replace(' ', '-'));
        product.setBodyHtml(request.getBodyHtml());
        if (request.getStatus() != null) product.setStatus(Product.ProductStatus.valueOf(request.getStatus()));
        product.setSeoTitle(request.getSeoTitle());
        product.setSeoDescription(request.getSeoDescription());
        if (request.getCollectionId() != null && !request.getCollectionId().isBlank()) {
            com.securemarts.domain.catalog.entity.Collection col = collectionRepository.findByPublicIdAndBusinessId(request.getCollectionId(), businessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Collection", request.getCollectionId()));
            product.setCollectionId(col.getId());
        } else {
            product.setCollectionId(null);
        }
        syncTags(product, businessId, request.getTagNames());
        if (request.getVariants() != null) {
            product.getVariants().clear();
            for (int i = 0; i < request.getVariants().size(); i++) {
                ProductVariant v = toVariant(product, request.getVariants().get(i), i);
                product.getVariants().add(v);
            }
        }
        if (request.getMedia() != null) {
            product.getMedia().clear();
            for (int i = 0; i < request.getMedia().size(); i++) {
                ProductRequest.ProductMediaRequest mr = request.getMedia().get(i);
                ProductMedia m = new ProductMedia();
                m.setProduct(product);
                m.setUrl(mr.getUrl());
                m.setAlt(mr.getAlt());
                m.setPosition(mr.getPosition());
                m.setMediaType(mr.getMediaType() != null ? mr.getMediaType() : "image");
                product.getMedia().add(m);
            }
        }
        product = productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public void deleteProduct(Long storeId, String productPublicId) {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        product.setDeletedAt(java.time.Instant.now());
        productRepository.save(product);
    }

    private void syncTags(Product product, Long businessId, Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            product.getTags().clear();
            return;
        }
        Set<Tag> newTags = new HashSet<>();
        for (String name : tagNames) {
            Tag tag = tagRepository.findByBusinessIdAndName(businessId, name.trim())
                    .orElseGet(() -> {
                        Tag t = new Tag();
                        t.setBusinessId(businessId);
                        t.setName(name.trim());
                        return tagRepository.save(t);
                    });
            newTags.add(tag);
        }
        product.setTags(newTags);
    }

    private ProductVariant toVariant(Product product, ProductRequest.ProductVariantRequest vr, int position) {
        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSku(vr.getSku());
        v.setTitle(vr.getTitle());
        v.setPriceAmount(vr.getPriceAmount() != null ? vr.getPriceAmount() : BigDecimal.ZERO);
        v.setCompareAtAmount(vr.getCompareAtAmount());
        v.setCurrency(vr.getCurrency() != null ? vr.getCurrency() : "NGN");
        v.setAttributesJson(vr.getAttributesJson());
        v.setPosition(vr.getPosition() >= 0 ? vr.getPosition() : position);
        return productVariantRepository.save(v);
    }

    private void ensureStoreExists(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
    }

    private Long resolveBusinessIdFromStoreId(Long storeId) {
        return storeRepository.findById(storeId)
                .map(s -> s.getBusiness().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
    }
}
