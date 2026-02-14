package com.shopper.domain.inventory.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.catalog.repository.ProductVariantRepository;
import com.shopper.domain.inventory.dto.InventoryAdjustmentRequest;
import com.shopper.domain.inventory.dto.InventoryItemResponse;
import com.shopper.domain.inventory.dto.LocationRequest;
import com.shopper.domain.inventory.entity.InventoryItem;
import com.shopper.domain.inventory.entity.InventoryMovement;
import com.shopper.domain.inventory.entity.Location;
import com.shopper.domain.inventory.repository.InventoryItemRepository;
import com.shopper.domain.inventory.repository.InventoryMovementRepository;
import com.shopper.domain.inventory.repository.LocationRepository;
import com.shopper.domain.onboarding.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final LocationRepository locationRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final ProductVariantRepository productVariantRepository;
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<Location> listLocations(Long storeId) {
        ensureStoreExists(storeId);
        return locationRepository.findByStoreId(storeId);
    }

    @Transactional
    public Location createLocation(Long storeId, LocationRequest request) {
        ensureStoreExists(storeId);
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
        return inventoryItemRepository.findByStoreId(storeId).stream()
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

    @Transactional
    public InventoryItem getOrCreateInventoryItem(Long storeId, String variantPublicId, String locationPublicId) {
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            throw new BusinessRuleException("Variant's product is not part of this store's catalog");
        }
        Location loc = getLocation(storeId, locationPublicId);
        return inventoryItemRepository.findByProductVariantIdAndLocationId(variant.getId(), loc.getId())
                .orElseGet(() -> {
                    InventoryItem item = new InventoryItem();
                    item.setStoreId(storeId);
                    item.setProductVariant(variant);
                    item.setLocation(loc);
                    item.setQuantityAvailable(0);
                    item.setQuantityReserved(0);
                    return inventoryItemRepository.save(item);
                });
    }

    @Transactional
    public InventoryItemResponse adjustStock(Long storeId, String inventoryItemPublicId, InventoryAdjustmentRequest request) {
        InventoryItem item = getInventoryItem(storeId, inventoryItemPublicId);
        int delta = request.getQuantityDelta();
        if (delta == 0) return InventoryItemResponse.from(item);
        if (delta < 0 && item.getQuantityAvailable() + delta < 0) {
            throw new BusinessRuleException("Insufficient quantity. Available: " + item.getQuantityAvailable());
        }
        item.setQuantityAvailable(item.getQuantityAvailable() + delta);
        inventoryItemRepository.save(item);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(item);
        movement.setQuantityDelta(delta);
        movement.setMovementType(request.getMovementType() != null ? request.getMovementType() : "ADJUSTMENT");
        movement.setReferenceType(request.getReferenceType());
        movement.setReferenceId(request.getReferenceId());
        inventoryMovementRepository.save(movement);
        return InventoryItemResponse.from(item);
    }

    @Transactional
    public InventoryItemResponse reserve(Long storeId, String inventoryItemPublicId, int quantity) {
        InventoryItem item = getInventoryItem(storeId, inventoryItemPublicId);
        if (item.getQuantityAvailable() < quantity) {
            throw new BusinessRuleException("Insufficient quantity to reserve. Available: " + item.getQuantityAvailable());
        }
        item.setQuantityAvailable(item.getQuantityAvailable() - quantity);
        item.setQuantityReserved(item.getQuantityReserved() + quantity);
        inventoryItemRepository.save(item);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(item);
        movement.setQuantityDelta(-quantity);
        movement.setMovementType(InventoryMovement.MovementType.RESERVE.name());
        inventoryMovementRepository.save(movement);
        return InventoryItemResponse.from(item);
    }

    @Transactional
    public InventoryItemResponse release(Long storeId, String inventoryItemPublicId, int quantity) {
        InventoryItem item = getInventoryItem(storeId, inventoryItemPublicId);
        int toRelease = Math.min(quantity, item.getQuantityReserved());
        if (toRelease <= 0) return InventoryItemResponse.from(item);
        item.setQuantityReserved(item.getQuantityReserved() - toRelease);
        item.setQuantityAvailable(item.getQuantityAvailable() + toRelease);
        inventoryItemRepository.save(item);
        InventoryMovement movement = new InventoryMovement();
        movement.setInventoryItem(item);
        movement.setQuantityDelta(toRelease);
        movement.setMovementType(InventoryMovement.MovementType.RELEASE.name());
        inventoryMovementRepository.save(movement);
        return InventoryItemResponse.from(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> listLowStock(Long storeId) {
        ensureStoreExists(storeId);
        return inventoryItemRepository.findLowStockByStoreId(storeId).stream()
                .map(InventoryItemResponse::from)
                .toList();
    }

    /** Total quantity available for a variant across all locations in the store. If no inventory items exist, returns unlimited so checkout is not blocked. */
    @Transactional(readOnly = true)
    public int getAvailableQuantityForVariant(Long storeId, String variantPublicId) {
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            return 0;
        }
        var items = inventoryItemRepository.findByStoreIdAndProductVariant_IdOrderByQuantityAvailableDesc(storeId, variant.getId());
        if (items.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return items.stream().mapToInt(InventoryItem::getQuantityAvailable).sum();
    }

    /** Deduct quantity for a variant (allocates across locations). Used at order creation. No-op if variant has no inventory items. */
    @Transactional
    public void deductVariantQuantity(Long storeId, String variantPublicId, int quantity, String referenceType, String referenceId) {
        if (quantity <= 0) return;
        var variant = productVariantRepository.findByPublicId(variantPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantPublicId));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            throw new BusinessRuleException("Variant does not belong to this store");
        }
        var items = inventoryItemRepository.findByStoreIdAndProductVariant_IdOrderByQuantityAvailableDesc(storeId, variant.getId());
        if (items.isEmpty()) return;
        int remaining = quantity;
        for (InventoryItem item : items) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, item.getQuantityAvailable());
            if (take <= 0) continue;
            item.setQuantityAvailable(item.getQuantityAvailable() - take);
            inventoryItemRepository.save(item);
            InventoryMovement movement = new InventoryMovement();
            movement.setInventoryItem(item);
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
