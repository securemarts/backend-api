package com.securemarts.domain.catalog.service;

import com.securemarts.common.dto.PageResponse;
import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.dto.ProductRequest;
import com.securemarts.domain.catalog.dto.ProductResponse;
import com.securemarts.domain.catalog.entity.*;
import com.securemarts.domain.catalog.repository.*;
import com.securemarts.domain.inventory.service.InventoryService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMediaRepository productMediaRepository;
    private final CollectionRepository collectionRepository;
    private final FileStorageService fileStorageService;
    private final TagRepository tagRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionLimitsService subscriptionLimitsService;
    private final InventoryService inventoryService;
    private final RecomputeSmartCollectionsService recomputeSmartCollectionsService;

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
        product.setVendor(request.getVendor());
        product.setProductType(request.getProductType());
        product.setSeoTitle(request.getSeoTitle());
        product.setSeoDescription(request.getSeoDescription());
        if (request.getStatus() != null && "ACTIVE".equals(request.getStatus())) {
            product.setPublishedAt(java.time.Instant.now());
        }
        product = productRepository.save(product);
        syncCollections(product, storeId, request.getCollectionIds());
        syncTags(product, businessId, request.getTagNames());
        syncOptions(product, request.getOptions());
        product = productRepository.save(product);
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
        if (request.getVariants() != null) {
            for (int i = 0; i < request.getVariants().size(); i++) {
                ProductRequest.ProductVariantRequest vr = request.getVariants().get(i);
                if (vr.getInventory() != null && !vr.getInventory().isEmpty()) {
                    ProductVariant v = product.getVariants().get(i);
                    inventoryService.ensureVariantInventoryLevels(storeId, v.getPublicId(), vr.getInventory());
                }
            }
        }
        recomputeSmartCollectionsService.recomputeForStore(storeId);
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
        product.setVendor(request.getVendor());
        product.setProductType(request.getProductType());
        if (request.getStatus() != null && "ACTIVE".equals(request.getStatus()) && product.getPublishedAt() == null) {
            product.setPublishedAt(java.time.Instant.now());
        }
        product.setSeoTitle(request.getSeoTitle());
        product.setSeoDescription(request.getSeoDescription());
        syncCollections(product, storeId, request.getCollectionIds());
        syncTags(product, businessId, request.getTagNames());
        syncOptions(product, request.getOptions());
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
        if (request.getVariants() != null) {
            for (int i = 0; i < request.getVariants().size(); i++) {
                ProductRequest.ProductVariantRequest vr = request.getVariants().get(i);
                if (vr.getInventory() != null && !vr.getInventory().isEmpty()) {
                    ProductVariant v = product.getVariants().get(i);
                    inventoryService.ensureVariantInventoryLevels(storeId, v.getPublicId(), vr.getInventory());
                }
            }
        }
        recomputeSmartCollectionsService.recomputeForStore(storeId);
        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public void deleteProduct(Long storeId, String productPublicId) {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        product.setDeletedAt(java.time.Instant.now());
        productRepository.save(product);
        recomputeSmartCollectionsService.recomputeForStore(storeId);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse attachMediaToVariant(Long storeId, String productPublicId, String variantPublicId, List<MultipartFile> mediaFiles) throws IOException {
        if (mediaFiles == null || mediaFiles.stream().allMatch(f -> f == null || f.isEmpty())) {
            return getProduct(storeId, productPublicId);
        }
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        if (product.isDeleted()) throw new ResourceNotFoundException("Product", productPublicId);
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getPublicId().equals(variantPublicId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product variant", variantPublicId));

        String storePublicId = storeRepository.findById(storeId).orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId))).getPublicId();
        int startPosition = product.getMedia().stream().mapToInt(ProductMedia::getPosition).max().orElse(-1) + 1;
        int position = startPosition;
        for (MultipartFile file : mediaFiles) {
            if (file == null || file.isEmpty()) continue;
            String url = fileStorageService.store(storePublicId, file);
            if (url == null) continue;
            ProductMedia m = new ProductMedia();
            m.setProduct(product);
            m.setUrl(url);
            m.setAlt("");
            m.setPosition(position++);
            m.setMediaType("image");
            product.getMedia().add(m);
            variant.getMedia().add(m);
        }
        product = productRepository.save(product);
        productVariantRepository.save(variant);
        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse detachMediaFromVariant(Long storeId, String productPublicId, String variantPublicId, String mediaPublicId) {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        if (product.isDeleted()) throw new ResourceNotFoundException("Product", productPublicId);
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getPublicId().equals(variantPublicId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product variant", variantPublicId));
        boolean removed = variant.getMedia().removeIf(m -> m.getPublicId().equals(mediaPublicId));
        if (!removed) {
            throw new ResourceNotFoundException("Product media", mediaPublicId);
        }
        productVariantRepository.save(variant);
        return ProductResponse.from(product);
    }
    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse addVariant(Long storeId, String productPublicId, ProductRequest.ProductVariantRequest request) throws IOException {
        return addVariantWithMedia(storeId, productPublicId, request, null);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse addVariantWithMedia(Long storeId, String productPublicId, ProductRequest.ProductVariantRequest request, List<MultipartFile> mediaFiles) throws IOException {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        if (product.isDeleted()) throw new ResourceNotFoundException("Product", productPublicId);
        int nextPosition = product.getVariants().stream().mapToInt(ProductVariant::getPosition).max().orElse(-1) + 1;
        ProductVariant v = toVariant(product, request, nextPosition);
        product.getVariants().add(v);
        product = productRepository.save(product);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            String storePublicId = storeRepository.findById(storeId).orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId))).getPublicId();
            int startPosition = product.getMedia().stream().mapToInt(ProductMedia::getPosition).max().orElse(-1) + 1;
            int position = startPosition;
            for (MultipartFile file : mediaFiles) {
                if (file == null || file.isEmpty()) continue;
                String url = fileStorageService.store(storePublicId, file);
                if (url == null) continue;
                ProductMedia m = new ProductMedia();
                m.setProduct(product);
                m.setUrl(url);
                m.setAlt("");
                m.setPosition(position++);
                m.setMediaType("image");
                product.getMedia().add(m);
                v.getMedia().add(m);
            }
            product = productRepository.save(product);
            productVariantRepository.save(v);
        }
        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse updateVariant(Long storeId, String productPublicId, String variantPublicId, ProductRequest.ProductVariantRequest request) {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        if (product.isDeleted()) throw new ResourceNotFoundException("Product", productPublicId);
        ProductVariant v = product.getVariants().stream()
                .filter(variant -> variant.getPublicId().equals(variantPublicId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product variant", variantPublicId));
        v.setSku(request.getSku());
        v.setTitle(request.getTitle());
        v.setBarcode(request.getBarcode());
        v.setPriceAmount(request.getPriceAmount() != null ? request.getPriceAmount() : BigDecimal.ZERO);
        v.setCompareAtAmount(request.getCompareAtAmount());
        v.setCurrency(request.getCurrency() != null ? request.getCurrency() : "NGN");
        v.setWeight(request.getWeight());
        v.setWeightUnit(request.getWeightUnit());
        v.setTrackInventory(request.isTrackInventory());
        v.setRequiresShipping(request.isRequiresShipping());
        if (request.getPosition() >= 0) v.setPosition(request.getPosition());
        syncVariantOptionValues(v, product, request.getOptions());
        productVariantRepository.save(v);
        if (request.getInventory() != null && !request.getInventory().isEmpty()) {
            inventoryService.ensureVariantInventoryLevels(storeId, v.getPublicId(), request.getInventory());
        }
        product = productRepository.save(product);
        recomputeSmartCollectionsService.recomputeForStore(storeId);
        return ProductResponse.from(product);
    }

    @Transactional
    @CacheEvict(value = "products", key = "#storeId")
    public ProductResponse deleteVariant(Long storeId, String productPublicId, String variantPublicId) {
        Product product = productRepository.findByPublicIdAndStoreId(productPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productPublicId));
        if (product.isDeleted()) throw new ResourceNotFoundException("Product", productPublicId);
        if (product.getVariants().size() <= 1) {
            throw new BusinessRuleException("A product must have at least one variant. Add another variant before removing this one.");
        }
        boolean removed = product.getVariants().removeIf(v -> v.getPublicId().equals(variantPublicId));
        if (!removed) throw new ResourceNotFoundException("Product variant", variantPublicId);
        product = productRepository.save(product);
        return ProductResponse.from(product);
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

    private void syncCollections(Product product, Long storeId, List<String> collectionIds) {
        product.getCollectionProducts().clear();
        if (collectionIds != null && !collectionIds.isEmpty()) {
            int position = 0;
            for (String publicId : collectionIds) {
                if (publicId == null || publicId.isBlank()) continue;
                final int pos = position;
                collectionRepository.findByPublicIdAndStoreId(publicId.trim(), storeId)
                        .ifPresent(collection -> {
                            CollectionProduct cp = new CollectionProduct();
                            cp.setCollectionId(collection.getId());
                            cp.setProductId(product.getId());
                            cp.setCollection(collection);
                            cp.setProduct(product);
                            cp.setPosition(pos);
                            product.getCollectionProducts().add(cp);
                            collection.getCollectionProducts().add(cp);
                        });
                position++;
            }
        }
    }

    private void syncOptions(Product product, List<com.securemarts.domain.catalog.dto.ProductOptionRequest> optionRequests) {
        product.getOptions().clear();
        if (optionRequests != null) {
            for (int i = 0; i < optionRequests.size(); i++) {
                com.securemarts.domain.catalog.dto.ProductOptionRequest req = optionRequests.get(i);
                if (req == null || req.getName() == null || req.getName().isBlank()) continue;
                ProductOption opt = new ProductOption();
                opt.setProduct(product);
                opt.setName(req.getName().trim());
                opt.setPosition(i);
                product.getOptions().add(opt);
                if (req.getValues() != null) {
                    for (int j = 0; j < req.getValues().size(); j++) {
                        String val = req.getValues().get(j);
                        if (val == null || val.isBlank()) continue;
                        ProductOptionValue ov = new ProductOptionValue();
                        ov.setOption(opt);
                        ov.setValue(val.trim());
                        ov.setPosition(j);
                        opt.getValues().add(ov);
                    }
                }
            }
        }
    }

    private ProductOptionValue findOptionValue(Product product, String optionName, String value) {
        if (product.getOptions() == null) return null;
        for (ProductOption opt : product.getOptions()) {
            if (opt.getName() != null && opt.getName().equals(optionName) && opt.getValues() != null) {
                for (ProductOptionValue ov : opt.getValues()) {
                    if (value != null && value.equals(ov.getValue())) return ov;
                }
            }
        }
        return null;
    }

    private void syncVariantOptionValues(ProductVariant variant, Product product, Map<String, String> optionsMap) {
        variant.getOptionValues().clear();
        if (optionsMap != null && !optionsMap.isEmpty() && product.getOptions() != null) {
            for (Map.Entry<String, String> e : optionsMap.entrySet()) {
                ProductOptionValue ov = findOptionValue(product, e.getKey(), e.getValue());
                if (ov != null) {
                    VariantOptionValue vov = new VariantOptionValue();
                    vov.setVariant(variant);
                    vov.setOptionValue(ov);
                    variant.getOptionValues().add(vov);
                }
            }
        }
    }

    private ProductVariant toVariant(Product product, ProductRequest.ProductVariantRequest vr, int position) {
        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSku(vr.getSku());
        v.setTitle(vr.getTitle());
        v.setBarcode(vr.getBarcode());
        v.setPriceAmount(vr.getPriceAmount() != null ? vr.getPriceAmount() : BigDecimal.ZERO);
        v.setCompareAtAmount(vr.getCompareAtAmount());
        v.setCurrency(vr.getCurrency() != null ? vr.getCurrency() : "NGN");
        v.setWeight(vr.getWeight());
        v.setWeightUnit(vr.getWeightUnit());
        v.setTrackInventory(vr.isTrackInventory());
        v.setRequiresShipping(vr.isRequiresShipping());
        v.setPosition(vr.getPosition() >= 0 ? vr.getPosition() : position);
        v = productVariantRepository.save(v);
        syncVariantOptionValues(v, product, vr.getOptions());
        return productVariantRepository.save(v);
    }

    private Long resolveBusinessIdFromStoreId(Long storeId) {
        return storeRepository.findById(storeId)
                .map(s -> s.getBusiness().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
    }
}
