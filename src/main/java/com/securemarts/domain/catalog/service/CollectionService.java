package com.securemarts.domain.catalog.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.dto.*;
import com.securemarts.domain.catalog.entity.Collection;
import com.securemarts.domain.catalog.entity.CollectionProduct;
import com.securemarts.domain.catalog.entity.CollectionRule;
import com.securemarts.domain.catalog.entity.Product;
import com.securemarts.domain.catalog.repository.CollectionProductRepository;
import com.securemarts.domain.catalog.repository.CollectionRepository;
import com.securemarts.domain.catalog.repository.CollectionRuleRepository;
import com.securemarts.domain.catalog.repository.ProductRepository;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionRuleRepository collectionRuleRepository;
    private final CollectionProductRepository collectionProductRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CollectionRuleEvaluator collectionRuleEvaluator;

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
    public List<CollectionResponse> listByStore(Long storeId) {
        List<Collection> list = collectionRepository.findByStoreId(storeId);
        return list.stream()
                .map(c -> CollectionResponse.from(c, collectionProductRepository.countByCollectionId(c.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CollectionResponse get(Long storeId, String collectionPublicId) {
        Collection c = collectionRepository.findByPublicIdAndStoreId(collectionPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionPublicId));
        Long productCount = collectionProductRepository.countByCollectionId(c.getId());
        return CollectionResponse.from(c, productCount);
    }

    @Transactional
    public CollectionResponse create(Long storeId, CreateCollectionRequest request, String imageUrl) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
        Collection c = new Collection();
        c.setStoreId(storeId);
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
        c.setCollectionType("smart".equalsIgnoreCase(request.getCollectionType()) ? Collection.CollectionType.SMART : Collection.CollectionType.MANUAL);
        String condOp = "any".equalsIgnoreCase(request.getConditionsOperator()) ? "any" : ("smart".equalsIgnoreCase(request.getCollectionType()) ? "all" : null);
        c.setConditionsOperator(condOp);
        c.setImageUrl(imageUrl);
        c = collectionRepository.save(c);

        if (c.getCollectionType() == Collection.CollectionType.SMART && request.getRules() != null && !request.getRules().isEmpty()) {
            syncRules(c, request.getRules());
            c = collectionRepository.save(c);
            recomputeSmartCollection(c);
        } else if (c.getCollectionType() == Collection.CollectionType.MANUAL && request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            addProductsToCollection(c, storeId, request.getProductIds(), 0);
        }
        return CollectionResponse.from(c, collectionProductRepository.countByCollectionId(c.getId()));
    }

    @Transactional
    public CollectionResponse update(Long storeId, String collectionPublicId, UpdateCollectionRequest request, String imageUrl) {
        Collection c = collectionRepository.findByPublicIdAndStoreId(collectionPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionPublicId));
        if (request.getTitle() != null) c.setTitle(request.getTitle().trim());
        if (request.getHandle() != null) c.setHandle(request.getHandle().trim().toLowerCase().replace(' ', '-'));
        if (request.getDescription() != null) c.setDescription(request.getDescription());
        if (request.getCollectionType() != null) {
            c.setCollectionType("smart".equalsIgnoreCase(request.getCollectionType()) ? Collection.CollectionType.SMART : Collection.CollectionType.MANUAL);
        }
        if (request.getConditionsOperator() != null) c.setConditionsOperator(request.getConditionsOperator());
        if (imageUrl != null) c.setImageUrl(imageUrl);

        if (c.getCollectionType() == Collection.CollectionType.SMART && request.getRules() != null) {
            syncRules(c, request.getRules());
            recomputeSmartCollection(c);
        } else if (c.getCollectionType() == Collection.CollectionType.MANUAL && request.getProductIds() != null) {
            collectionProductRepository.deleteByCollectionId(c.getId());
            if (!request.getProductIds().isEmpty()) {
                addProductsToCollection(c, storeId, request.getProductIds(), 0);
            }
        }
        c = collectionRepository.save(c);
        return CollectionResponse.from(c, collectionProductRepository.countByCollectionId(c.getId()));
    }

    @Transactional(readOnly = true)
    public Page<CollectionProductItemResponse> listCollectionProducts(Long storeId, String collectionPublicId, Pageable pageable) {
        Collection c = collectionRepository.findByPublicIdAndStoreId(collectionPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionPublicId));
        return collectionProductRepository.findByCollectionIdOrderByPositionAscProductIdAsc(c.getId(), pageable)
                .map(CollectionProductItemResponse::from);
    }

    @Transactional
    public void addProducts(Long storeId, String collectionPublicId, AddCollectionProductsRequest request) {
        Collection c = collectionRepository.findByPublicIdAndStoreId(collectionPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionPublicId));
        if (c.getCollectionType() == Collection.CollectionType.SMART) {
            throw new BusinessRuleException("Cannot add products to a smart collection; update rules instead.");
        }
        int startPosition = request.getPosition() != null ? request.getPosition() : (int) collectionProductRepository.countByCollectionId(c.getId());
        addProductsToCollection(c, storeId, request.getProductIds(), startPosition);
    }

    @Transactional
    public CollectionResponse updateImage(Long storeId, String collectionPublicId, String imageUrl) {
        Collection c = collectionRepository.findByPublicIdAndStoreId(collectionPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionPublicId));
        c.setImageUrl(imageUrl);
        c = collectionRepository.save(c);
        return CollectionResponse.from(c, collectionProductRepository.countByCollectionId(c.getId()));
    }

    @Transactional
    public void removeProduct(Long storeId, String collectionPublicId, String productPublicId) {
        Collection c = collectionRepository.findByPublicIdAndStoreId(collectionPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection", collectionPublicId));
        if (c.getCollectionType() == Collection.CollectionType.SMART) {
            throw new BusinessRuleException("Cannot remove products from a smart collection; update rules instead.");
        }
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        collectionProductRepository.findByCollectionIdAndProductId(c.getId(), product.getId())
                .ifPresent(collectionProductRepository::delete);
    }

    /**
     * Recomputes membership for a smart collection from its rules and writes to collection_products.
     */
    @Transactional
    public void recomputeSmartCollection(Collection collection) {
        if (collection.getCollectionType() != Collection.CollectionType.SMART) return;
        collectionProductRepository.deleteByCollectionId(collection.getId());
        List<Long> productIds = collectionRuleEvaluator.evaluate(collection, collection.getStoreId());
        if (productIds.isEmpty()) return;
        List<CollectionProduct> toSave = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            CollectionProduct cp = new CollectionProduct();
            cp.setCollectionId(collection.getId());
            cp.setProductId(productId);
            cp.setCollection(collection);
            cp.setProduct(productRepository.getReferenceById(productId));
            cp.setPosition(i);
            cp.setCreatedAt(Instant.now());
            toSave.add(cp);
        }
        collectionProductRepository.saveAll(toSave);
    }

    private void syncRules(Collection collection, List<CollectionRuleRequest> ruleRequests) {
        collectionRuleRepository.deleteByCollectionId(collection.getId());
        collection.getRules().clear();
        if (ruleRequests != null) {
            for (int i = 0; i < ruleRequests.size(); i++) {
                CollectionRuleRequest req = ruleRequests.get(i);
                if (req == null || req.getField() == null || req.getField().isBlank()) continue;
                CollectionRule rule = new CollectionRule();
                rule.setCollection(collection);
                rule.setField(req.getField().trim());
                rule.setOperator(req.getOperator() != null ? req.getOperator().trim() : "equals");
                rule.setValue(req.getValue());
                rule.setPosition(i);
                collection.getRules().add(collectionRuleRepository.save(rule));
            }
        }
    }

    private void addProductsToCollection(Collection collection, Long storeId, List<String> productPublicIds, int startPosition) {
        if (productPublicIds == null) return;
        int[] index = { startPosition };
        for (String publicId : productPublicIds) {
            if (publicId == null || publicId.isBlank()) continue;
            final int pos = index[0];
            productRepository.findByPublicIdAndStoreId(publicId.trim(), storeId).ifPresent(product -> {
                if (collectionProductRepository.existsByCollectionIdAndProductId(collection.getId(), product.getId())) return;
                CollectionProduct cp = new CollectionProduct();
                cp.setCollectionId(collection.getId());
                cp.setProductId(product.getId());
                cp.setCollection(collection);
                cp.setProduct(product);
                cp.setPosition(pos);
                cp.setCreatedAt(Instant.now());
                collectionProductRepository.save(cp);
            });
            index[0]++;
        }
    }
}
