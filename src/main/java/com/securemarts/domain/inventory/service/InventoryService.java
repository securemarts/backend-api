package com.securemarts.domain.inventory.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.repository.ProductVariantRepository;
import com.securemarts.domain.inventory.dto.InventoryAdjustmentRequest;
import com.securemarts.domain.inventory.dto.InventoryItemResponse;
import com.securemarts.domain.inventory.dto.LocationRequest;
import com.securemarts.domain.inventory.entity.InventoryItem;
import com.securemarts.domain.inventory.entity.InventoryLevel;
import com.securemarts.domain.inventory.entity.InventoryMovement;
import com.securemarts.domain.inventory.entity.Location;
import com.securemarts.domain.inventory.repository.InventoryItemRepository;
import com.securemarts.domain.inventory.repository.InventoryLevelRepository;
import com.securemarts.domain.inventory.repository.InventoryMovementRepository;
import com.securemarts.domain.inventory.repository.LocationRepository;
import com.securemarts.domain.onboarding.entity.Business;
import com.securemarts.domain.onboarding.entity.Store;
import com.securemarts.domain.onboarding.repository.StoreRepository;
import com.securemarts.domain.onboarding.service.SubscriptionLimitsService;
import com.securemarts.domain.catalog.dto.VariantInventoryRequest;
import com.securemarts.domain.catalog.service.RecomputeSmartCollectionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final LocationRepository locationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryLevelRepository inventoryLevelRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;
    private final SubscriptionLimitsService subscriptionLimitsService;
    private final RecomputeSmartCollectionsService recomputeSmartCollectionsService;

    @Transactional(readOnly = true)
    public List<Location> listLocations(Long storeId) {
        ensureStoreExists(storeId);
        return locationRepository.findByStoreId(storeId);
    }

    @Transactional
    public Location createLocation(Long storeId, LocationRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", String.valueOf(storeId)));
        Business business = store.getBusiness();
        var limits = subscriptionLimitsService.getLimitsForBusiness(business);
        long locationCount = locationRepository.countByStoreId(storeId);
        if (locationCount >= limits.getMaxLocationsPerStore()) {
            throw new BusinessRuleException("Location limit reached for your plan (" + limits.getMaxLocationsPerStore() + " per store). Upgrade to add more locations.");
        }
        Location loc = new Location();
        loc.setStoreId(storeId);
        loc.setName(request.getName().trim());
        loc.setAddress(request.getAddress());
        return locationRepository.save(loc);
    }

    @Transactional(readOnly = true)
    public Location getLocation(Long storeId, String locationPublicId) {
        return locationRepository.findByPublicIdAndStoreId(locationPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationPublicId));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> listInventoryByStore(Long storeId) {
        ensureStoreExists(storeId);
        return inventoryLevelRepository.findByInventoryItem_StoreId(storeId).stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryItem getInventoryItem(Long storeId, String inventoryItemPublicId) {
        InventoryItem item = inventoryItemRepository.findByPublicId(inventoryItemPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", inventoryItemPublicId));
        if (!item.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("InventoryItem", inventoryItemPublicId);
        }
        return item;
    }

    @Transactional(readOnly = true)
    public InventoryLevel getInventoryLevel(Long storeId, String levelPublicId) {
        InventoryLevel level = inventoryLevelRepository.findByPublicId(levelPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryLevel", levelPublicId));
        if (!level.getInventoryItem().getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("InventoryLevel", levelPublicId);
        }
        return level;
    }

    /** Returns the single inventory item per (store, variant). Creates it if it does not exist. */
    @Transactional
    public InventoryItem getOrCreateInventoryItem(Long storeId, String variantPublicId) {
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            throw new BusinessRuleException("Variant's product is not part of this store's catalog");
        }
        return inventoryItemRepository.findByStoreIdAndProductVariantId(storeId, variant.getId())
                .orElseGet(() -> {
                    InventoryItem item = new InventoryItem();
                    item.setStoreId(storeId);
                    item.setProductVariant(variant);
                    item.setTracked(variant.isTrackInventory());
                    item.setRequiresShipping(variant.isRequiresShipping());
                    item.setCostAmount(variant.getCostAmount());
                    return inventoryItemRepository.save(item);
                });
    }

    /** Returns the inventory level for (item, location). Creates it with zero quantities if it does not exist. */
    @Transactional
    public InventoryLevel getOrCreateInventoryLevel(InventoryItem item, Location location) {
        return inventoryLevelRepository.findByInventoryItemIdAndLocationId(item.getId(), location.getId())
                .orElseGet(() -> {
                    InventoryLevel level = new InventoryLevel();
                    level.setInventoryItem(item);
                    level.setLocation(location);
                    level.setQuantityAvailable(0);
                    level.setQuantityReserved(0);
                    level.setQuantityIncoming(0);
                    return inventoryLevelRepository.save(level);
                });
    }

    /** Returns the level for (store, variant, location). Creates item and level if needed. */
    @Transactional
    public InventoryLevel getOrCreateLevel(Long storeId, String variantPublicId, String locationPublicId) {
        InventoryItem item = getOrCreateInventoryItem(storeId, variantPublicId);
        Location loc = getLocation(storeId, locationPublicId);
        return getOrCreateInventoryLevel(item, loc);
    }

    /** Sets inventory quantities per location for a variant from product create/update. Creates item and levels as needed. */
    @Transactional
    public void ensureVariantInventoryLevels(Long storeId, String variantPublicId, List<VariantInventoryRequest> inventory) {
        if (inventory == null || inventory.isEmpty()) return;
        InventoryItem item = getOrCreateInventoryItem(storeId, variantPublicId);
        for (VariantInventoryRequest req : inventory) {
            if (req.getLocationId() == null || req.getLocationId().isBlank()) continue;
            Location loc = getLocation(storeId, req.getLocationId());
            InventoryLevel level = getOrCreateInventoryLevel(item, loc);
            level.setQuantityAvailable(req.getQuantity() != null ? req.getQuantity() : 0);
            level.setQuantityReserved(0);
            level.setQuantityIncoming(0);
            inventoryLevelRepository.save(level);
        }
        recomputeSmartCollectionsService.recomputeForStore(storeId);
    }

    @Transactional
    public InventoryItemResponse adjustStock(Long storeId, String inventoryLevelPublicId, InventoryAdjustmentRequest request) {
        InventoryLevel level = getInventoryLevel(storeId, inventoryLevelPublicId);
        int delta = request.getQuantityDelta();
        if (delta == 0) return InventoryItemResponse.from(level);
        if (delta < 0 && level.getQuantityAvailable() + delta < 0) {
            throw new BusinessRuleException("Insufficient quantity. Available: " + level.getQuantityAvailable());
        }
        level.setQuantityAvailable(level.getQuantityAvailable() + delta);
        inventoryLevelRepository.save(level);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(level.getInventoryItem());
        movement.setInventoryLevel(level);
        movement.setQuantityDelta(delta);
        movement.setMovementType(request.getMovementType() != null ? request.getMovementType() : "ADJUSTMENT");
        movement.setReferenceType(request.getReferenceType());
        movement.setReferenceId(request.getReferenceId());
        inventoryMovementRepository.save(movement);
        recomputeSmartCollectionsService.recomputeForStore(storeId);
        return InventoryItemResponse.from(level);
    }

    @Transactional
    public InventoryItemResponse reserve(Long storeId, String inventoryLevelPublicId, int quantity) {
        return reserve(storeId, inventoryLevelPublicId, quantity, null, null);
    }

    @Transactional
    public InventoryItemResponse reserve(Long storeId, String inventoryLevelPublicId, int quantity, String referenceType, String referenceId) {
        InventoryLevel level = getInventoryLevel(storeId, inventoryLevelPublicId);
        if (level.getQuantityAvailable() < quantity) {
            throw new BusinessRuleException("Insufficient quantity to reserve. Available: " + level.getQuantityAvailable());
        }
        level.setQuantityAvailable(level.getQuantityAvailable() - quantity);
        level.setQuantityReserved(level.getQuantityReserved() + quantity);
        inventoryLevelRepository.save(level);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(level.getInventoryItem());
        movement.setInventoryLevel(level);
        movement.setQuantityDelta(-quantity);
        movement.setMovementType(InventoryMovement.MovementType.RESERVE.name());
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        inventoryMovementRepository.save(movement);
        return InventoryItemResponse.from(level);
    }

    /** Reserve quantity at a specific (variant, location). Used by checkout when allocating to a location. */
    @Transactional
    public void reserveAtLevel(Long storeId, String variantPublicId, String locationPublicId, int quantity, String referenceType, String referenceId) {
        if (quantity <= 0) return;
        InventoryLevel level = getOrCreateLevel(storeId, variantPublicId, locationPublicId);
        if (level.getQuantityAvailable() < quantity) {
            throw new BusinessRuleException("Insufficient quantity to reserve at this location. Available: " + level.getQuantityAvailable());
        }
        level.setQuantityAvailable(level.getQuantityAvailable() - quantity);
        level.setQuantityReserved(level.getQuantityReserved() + quantity);
        inventoryLevelRepository.save(level);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(level.getInventoryItem());
        movement.setInventoryLevel(level);
        movement.setQuantityDelta(-quantity);
        movement.setMovementType(InventoryMovement.MovementType.RESERVE.name());
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        inventoryMovementRepository.save(movement);
    }

    @Transactional
    public InventoryItemResponse release(Long storeId, String inventoryLevelPublicId, int quantity) {
        InventoryLevel level = getInventoryLevel(storeId, inventoryLevelPublicId);
        int toRelease = Math.min(quantity, level.getQuantityReserved());
        if (toRelease <= 0) return InventoryItemResponse.from(level);
        level.setQuantityReserved(level.getQuantityReserved() - toRelease);
        level.setQuantityAvailable(level.getQuantityAvailable() + toRelease);
        inventoryLevelRepository.save(level);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(level.getInventoryItem());
        movement.setInventoryLevel(level);
        movement.setQuantityDelta(toRelease);
        movement.setMovementType(InventoryMovement.MovementType.RELEASE.name());
        inventoryMovementRepository.save(movement);
        return InventoryItemResponse.from(level);
    }

    /**
     * Reserve quantity for a variant across locations (highest available first). Uses pessimistic lock to avoid races.
     * referenceType e.g. "ORDER", referenceId e.g. order publicId.
     */
    @Transactional
    public void reserveVariantQuantity(Long storeId, String variantPublicId, int quantity, String referenceType, String referenceId) {
        if (quantity <= 0) return;
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            throw new BusinessRuleException("Variant does not belong to this store");
        }
        var levels = inventoryLevelRepository.findByStoreIdAndVariantIdForUpdate(storeId, variant.getId());
        if (levels.isEmpty()) {
            throw new BusinessRuleException("No inventory for variant " + variantPublicId);
        }
        int remaining = quantity;
        for (InventoryLevel level : levels) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, level.getQuantityAvailable());
            if (take <= 0) continue;
            level.setQuantityAvailable(level.getQuantityAvailable() - take);
            level.setQuantityReserved(level.getQuantityReserved() + take);
            inventoryLevelRepository.save(level);
            InventoryMovement movement = new InventoryMovement();
            movement.setInventoryItem(level.getInventoryItem());
            movement.setInventoryLevel(level);
            movement.setQuantityDelta(-take);
            movement.setMovementType(InventoryMovement.MovementType.RESERVE.name());
            movement.setReferenceType(referenceType);
            movement.setReferenceId(referenceId);
            inventoryMovementRepository.save(movement);
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessRuleException("Insufficient stock for variant " + variantPublicId + ". Requested: " + quantity);
        }
    }

    /**
     * Release all quantities reserved for the given reference (e.g. ORDER + orderPublicId).
     */
    @Transactional
    public void releaseByReference(Long storeId, String referenceType, String referenceId) {
        var movements = inventoryMovementRepository.findByInventoryItem_StoreIdAndReferenceTypeAndReferenceIdAndMovementType(
                storeId, referenceType, referenceId, InventoryMovement.MovementType.RESERVE.name());
        for (InventoryMovement m : movements) {
            int qty = Math.abs(m.getQuantityDelta());
            InventoryLevel level = m.getInventoryLevel();
            if (level == null) continue;
            int toRelease = Math.min(qty, level.getQuantityReserved());
            if (toRelease <= 0) continue;
            level.setQuantityReserved(level.getQuantityReserved() - toRelease);
            level.setQuantityAvailable(level.getQuantityAvailable() + toRelease);
            inventoryLevelRepository.save(level);
            InventoryMovement releaseMovement = new InventoryMovement();
            releaseMovement.setInventoryItem(level.getInventoryItem());
            releaseMovement.setInventoryLevel(level);
            releaseMovement.setQuantityDelta(toRelease);
            releaseMovement.setMovementType(InventoryMovement.MovementType.RELEASE.name());
            releaseMovement.setReferenceType(referenceType);
            releaseMovement.setReferenceId(referenceId);
            inventoryMovementRepository.save(releaseMovement);
        }
    }

    /**
     * Convert reserved quantities to sale (payment success). Deducts from reserved and records SALE movements.
     */
    @Transactional
    public void convertReservationToSale(Long storeId, String referenceType, String referenceId) {
        var movements = inventoryMovementRepository.findByInventoryItem_StoreIdAndReferenceTypeAndReferenceIdAndMovementType(
                storeId, referenceType, referenceId, InventoryMovement.MovementType.RESERVE.name());
        for (InventoryMovement m : movements) {
            int qty = Math.abs(m.getQuantityDelta());
            InventoryLevel level = m.getInventoryLevel();
            if (level == null) continue;
            int toConvert = Math.min(qty, level.getQuantityReserved());
            if (toConvert <= 0) continue;
            level.setQuantityReserved(level.getQuantityReserved() - toConvert);
            inventoryLevelRepository.save(level);
            InventoryMovement saleMovement = new InventoryMovement();
            saleMovement.setInventoryItem(level.getInventoryItem());
            saleMovement.setInventoryLevel(level);
            saleMovement.setQuantityDelta(-toConvert);
            saleMovement.setMovementType(InventoryMovement.MovementType.SALE.name());
            saleMovement.setReferenceType(referenceType);
            saleMovement.setReferenceId(referenceId);
            inventoryMovementRepository.save(saleMovement);
        }
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> listLowStock(Long storeId) {
        ensureStoreExists(storeId);
        return inventoryLevelRepository.findByInventoryItem_StoreId(storeId).stream()
                .filter(l -> l.getQuantityAvailable() <= 0)
                .map(InventoryItemResponse::from)
                .toList();
    }

    /**
     * Returns inventory levels for a variant with quantity available > 0, sorted by quantity desc (for allocation).
     */
    @Transactional
    public List<InventoryLevel> getAllocationCandidatesForVariant(Long storeId, String variantPublicId) {
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            return List.of();
        }
        return inventoryLevelRepository.findByStoreIdAndVariantIdForUpdate(storeId, variant.getId());
    }

    @Transactional(readOnly = true)
    public int getAvailableQuantityForVariant(Long storeId, String variantPublicId) {
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            return 0;
        }
        var levels = inventoryLevelRepository.findByInventoryItem_StoreId(storeId).stream()
                .filter(l -> l.getInventoryItem().getProductVariant().getId().equals(variant.getId()))
                .toList();
        if (levels.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return levels.stream().mapToInt(InventoryLevel::getQuantityAvailable).sum();
    }

    /** Deduct quantity from a specific (variant, location) level. Used when location is known (e.g. POS preferred location). */
    @Transactional
    public void deductAtLevel(Long storeId, String variantPublicId, String locationPublicId, int quantity, String referenceType, String referenceId) {
        if (quantity <= 0) return;
        InventoryLevel level = getOrCreateLevel(storeId, variantPublicId, locationPublicId);
        if (level.getQuantityAvailable() < quantity) {
            throw new BusinessRuleException("Insufficient stock at this location for variant. Available: " + level.getQuantityAvailable());
        }
        level.setQuantityAvailable(level.getQuantityAvailable() - quantity);
        inventoryLevelRepository.save(level);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(level.getInventoryItem());
        movement.setInventoryLevel(level);
        movement.setQuantityDelta(-quantity);
        movement.setMovementType(InventoryMovement.MovementType.SALE.name());
        movement.setReferenceType(referenceType);
        movement.setReferenceId(referenceId);
        inventoryMovementRepository.save(movement);
    }

    /** Deduct quantity for a variant from levels (allocates across locations). Used at order creation. */
    @Transactional
    public void deductVariantQuantity(Long storeId, String variantPublicId, int quantity, String referenceType, String referenceId) {
        if (quantity <= 0) return;
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            throw new BusinessRuleException("Variant does not belong to this store");
        }
        var levels = inventoryLevelRepository.findByStoreIdAndVariantIdForUpdate(storeId, variant.getId());
        if (levels.isEmpty()) return;
        int remaining = quantity;
        for (InventoryLevel level : levels) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, level.getQuantityAvailable());
            if (take <= 0) continue;
            level.setQuantityAvailable(level.getQuantityAvailable() - take);
            inventoryLevelRepository.save(level);
            InventoryMovement movement = new InventoryMovement();
            movement.setInventoryItem(level.getInventoryItem());
            movement.setInventoryLevel(level);
            movement.setQuantityDelta(-take);
            movement.setMovementType(InventoryMovement.MovementType.SALE.name());
            movement.setReferenceType(referenceType);
            movement.setReferenceId(referenceId);
            inventoryMovementRepository.save(movement);
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessRuleException("Insufficient stock for variant " + variantPublicId + ". Requested: " + quantity);
        }
    }

    private void ensureStoreExists(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("Store", String.valueOf(storeId));
        }
    }
}
