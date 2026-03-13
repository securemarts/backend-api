package com.securemarts.domain.inventory.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.entity.ProductVariant;
import com.securemarts.domain.catalog.repository.ProductVariantRepository;
import com.securemarts.domain.inventory.dto.*;
import com.securemarts.domain.inventory.entity.*;
import com.securemarts.domain.inventory.repository.PurchaseOrderRepository;
import com.securemarts.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierService supplierService;
    private final InventoryService inventoryService;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public PurchaseOrderResponse create(Long storeId, CreatePurchaseOrderRequest request) {
        Location destination = inventoryService.getLocation(storeId, request.getDestinationPublicId());

        PurchaseOrder po = new PurchaseOrder();
        po.setStoreId(storeId);
        po.setDestination(destination);
        po.setNumber(nextPoNumber(storeId));
        po.setCurrency(request.getCurrency() != null ? request.getCurrency() : "NGN");
        po.setShippingCost(request.getShippingCost() != null ? request.getShippingCost() : po.getShippingCost());
        po.setAdjustmentsCost(request.getAdjustmentsCost() != null ? request.getAdjustmentsCost() : po.getAdjustmentsCost());
        po.setTaxCost(request.getTaxCost() != null ? request.getTaxCost() : po.getTaxCost());
        po.setNote(request.getNote());
        po.setExpectedOn(request.getExpectedOn());
        po.setPaymentDueOn(request.getPaymentDueOn());

        if (request.getSupplierPublicId() != null) {
            Supplier supplier = supplierService.findByStoreOrThrow(storeId, request.getSupplierPublicId());
            po.setSupplier(supplier);
        }

        po = purchaseOrderRepository.save(po);

        for (CreatePurchaseOrderRequest.LineItem li : request.getLineItems()) {
            ProductVariant variant = productVariantRepository.findByPublicId(li.getVariantPublicId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", li.getVariantPublicId()));
            InventoryItem item = inventoryService.getOrCreateInventoryItem(storeId, li.getVariantPublicId());

            PurchaseOrderLineItem lineItem = new PurchaseOrderLineItem();
            lineItem.setPurchaseOrder(po);
            lineItem.setInventoryItem(item);
            lineItem.setProductVariant(variant);
            lineItem.setQuantity(li.getQuantity());
            lineItem.setCostPrice(li.getCostPrice());
            lineItem.setRetailPrice(li.getRetailPrice());
            po.getLineItems().add(lineItem);
        }

        po = purchaseOrderRepository.save(po);
        return PurchaseOrderResponse.from(po);
    }

    @Transactional(readOnly = true)
    public PageResponse<PurchaseOrderResponse> list(Long storeId, String status, Pageable pageable) {
        org.springframework.data.domain.Page<PurchaseOrder> page;
        if (status != null && !status.isBlank()) {
            PurchaseOrder.PurchaseOrderStatus s = PurchaseOrder.PurchaseOrderStatus.valueOf(status.toUpperCase());
            page = purchaseOrderRepository.findByStoreIdAndStatus(storeId, s, pageable);
        } else {
            page = purchaseOrderRepository.findByStoreId(storeId, pageable);
        }
        return PageResponse.of(page.map(PurchaseOrderResponse::from));
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse get(Long storeId, String poPublicId) {
        PurchaseOrder po = findByStoreOrThrow(storeId, poPublicId);
        return PurchaseOrderResponse.from(po);
    }

    @Transactional
    public PurchaseOrderResponse update(Long storeId, String poPublicId, UpdatePurchaseOrderRequest request) {
        PurchaseOrder po = findByStoreOrThrow(storeId, poPublicId);
        ensureDraft(po);

        if (request.getSupplierPublicId() != null) {
            po.setSupplier(supplierService.findByStoreOrThrow(storeId, request.getSupplierPublicId()));
        }
        if (request.getDestinationPublicId() != null) {
            po.setDestination(inventoryService.getLocation(storeId, request.getDestinationPublicId()));
        }
        if (request.getCurrency() != null) po.setCurrency(request.getCurrency());
        if (request.getShippingCost() != null) po.setShippingCost(request.getShippingCost());
        if (request.getAdjustmentsCost() != null) po.setAdjustmentsCost(request.getAdjustmentsCost());
        if (request.getTaxCost() != null) po.setTaxCost(request.getTaxCost());
        if (request.getNote() != null) po.setNote(request.getNote());
        if (request.getExpectedOn() != null) po.setExpectedOn(request.getExpectedOn());
        if (request.getPaymentDueOn() != null) po.setPaymentDueOn(request.getPaymentDueOn());
        if (request.getPaid() != null) po.setPaid(request.getPaid());

        po = purchaseOrderRepository.save(po);
        return PurchaseOrderResponse.from(po);
    }

    /**
     * DRAFT -> ORDERED: sets orderedAt, increments quantityIncoming at destination for each line.
     */
    @Transactional
    public PurchaseOrderResponse markOrdered(Long storeId, String poPublicId) {
        PurchaseOrder po = findByStoreOrThrow(storeId, poPublicId);
        ensureDraft(po);
        if (po.getDestination() == null) {
            throw new BusinessRuleException("Purchase order must have a destination location");
        }
        if (po.getLineItems().isEmpty()) {
            throw new BusinessRuleException("Purchase order must have at least one line item");
        }

        po.setStatus(PurchaseOrder.PurchaseOrderStatus.ORDERED);
        po.setOrderedAt(Instant.now());

        for (PurchaseOrderLineItem li : po.getLineItems()) {
            InventoryLevel level = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), po.getDestination());
            inventoryService.incrementIncoming(level, li.getQuantity());
        }

        po = purchaseOrderRepository.save(po);
        return PurchaseOrderResponse.from(po);
    }

    /**
     * ORDERED|PARTIAL -> PARTIAL|RECEIVED: receive items, add to available, decrement incoming.
     */
    @Transactional
    public PurchaseOrderResponse receive(Long storeId, String poPublicId, ReceivePurchaseOrderItemsRequest request) {
        PurchaseOrder po = findByStoreOrThrow(storeId, poPublicId);
        if (po.getStatus() != PurchaseOrder.PurchaseOrderStatus.ORDERED
                && po.getStatus() != PurchaseOrder.PurchaseOrderStatus.PARTIAL) {
            throw new BusinessRuleException("Can only receive items for ORDERED or PARTIAL purchase orders");
        }

        Map<String, PurchaseOrderLineItem> lineItemMap = po.getLineItems().stream()
                .collect(Collectors.toMap(li -> li.getPublicId(), Function.identity()));

        for (ReceivePurchaseOrderItemsRequest.ReceivedItem ri : request.getItems()) {
            PurchaseOrderLineItem li = lineItemMap.get(ri.getLineItemPublicId());
            if (li == null) {
                throw new ResourceNotFoundException("PurchaseOrderLineItem", ri.getLineItemPublicId());
            }
            int remaining = li.getQuantity() - li.getReceivedQuantity() - li.getRejectedQuantity();
            int totalIncoming = ri.getQuantity() + ri.getRejectedQuantity();
            if (totalIncoming > remaining) {
                throw new BusinessRuleException("Cannot receive more than outstanding quantity (" + remaining + ") for line item " + ri.getLineItemPublicId());
            }

            li.setReceivedQuantity(li.getReceivedQuantity() + ri.getQuantity());
            li.setRejectedQuantity(li.getRejectedQuantity() + ri.getRejectedQuantity());

            InventoryLevel level = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), po.getDestination());
            if (ri.getQuantity() > 0) {
                inventoryService.addAvailableStock(level, ri.getQuantity(),
                        InventoryMovement.MovementType.PURCHASE_ORDER_RECEIVE.name(),
                        "PURCHASE_ORDER", po.getPublicId());
            }
            inventoryService.decrementIncoming(level, totalIncoming);
        }

        boolean allReceived = po.getLineItems().stream()
                .allMatch(li -> li.getReceivedQuantity() + li.getRejectedQuantity() >= li.getQuantity());

        po.setStatus(allReceived ? PurchaseOrder.PurchaseOrderStatus.RECEIVED : PurchaseOrder.PurchaseOrderStatus.PARTIAL);
        if (allReceived) {
            po.setReceivedAt(Instant.now());
        }

        po = purchaseOrderRepository.save(po);
        return PurchaseOrderResponse.from(po);
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long storeId, String poPublicId) {
        PurchaseOrder po = findByStoreOrThrow(storeId, poPublicId);
        if (po.getStatus() == PurchaseOrder.PurchaseOrderStatus.RECEIVED
                || po.getStatus() == PurchaseOrder.PurchaseOrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot cancel a " + po.getStatus().name() + " purchase order");
        }

        if (po.getStatus() == PurchaseOrder.PurchaseOrderStatus.ORDERED
                || po.getStatus() == PurchaseOrder.PurchaseOrderStatus.PARTIAL) {
            for (PurchaseOrderLineItem li : po.getLineItems()) {
                int unreceived = li.getQuantity() - li.getReceivedQuantity() - li.getRejectedQuantity();
                if (unreceived > 0 && po.getDestination() != null) {
                    InventoryLevel level = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), po.getDestination());
                    inventoryService.decrementIncoming(level, unreceived);
                }
            }
        }

        po.setStatus(PurchaseOrder.PurchaseOrderStatus.CANCELLED);
        po = purchaseOrderRepository.save(po);
        return PurchaseOrderResponse.from(po);
    }

    @Transactional
    public void delete(Long storeId, String poPublicId) {
        PurchaseOrder po = findByStoreOrThrow(storeId, poPublicId);
        ensureDraft(po);
        purchaseOrderRepository.delete(po);
    }

    private PurchaseOrder findByStoreOrThrow(Long storeId, String poPublicId) {
        return purchaseOrderRepository.findByPublicIdAndStoreId(poPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", poPublicId));
    }

    private void ensureDraft(PurchaseOrder po) {
        if (po.getStatus() != PurchaseOrder.PurchaseOrderStatus.DRAFT) {
            throw new BusinessRuleException("This operation is only allowed on DRAFT purchase orders");
        }
    }

    private String nextPoNumber(Long storeId) {
        int seq = purchaseOrderRepository.findMaxSequenceByStoreId(storeId) + 1;
        return "PO-" + seq;
    }
}
