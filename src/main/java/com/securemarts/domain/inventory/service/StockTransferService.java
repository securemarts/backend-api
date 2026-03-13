package com.securemarts.domain.inventory.service;

import com.securemarts.common.exception.BusinessRuleException;
import com.securemarts.common.exception.ResourceNotFoundException;
import com.securemarts.domain.catalog.entity.ProductVariant;
import com.securemarts.domain.catalog.repository.ProductVariantRepository;
import com.securemarts.domain.inventory.dto.*;
import com.securemarts.domain.inventory.entity.*;
import com.securemarts.domain.inventory.repository.StockTransferRepository;
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
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final InventoryService inventoryService;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public StockTransferResponse create(Long storeId, CreateStockTransferRequest request) {
        Location origin = inventoryService.getLocation(storeId, request.getOriginPublicId());
        Location destination = inventoryService.getLocation(storeId, request.getDestinationPublicId());

        if (origin.getId().equals(destination.getId())) {
            throw new BusinessRuleException("Origin and destination locations must be different");
        }

        StockTransfer st = new StockTransfer();
        st.setStoreId(storeId);
        st.setOrigin(origin);
        st.setDestination(destination);
        st.setNumber(nextTransferNumber(storeId));
        st.setExpectedArrivalDate(request.getExpectedArrivalDate());
        st.setNote(request.getNote());
        st.setReferenceName(request.getReferenceName());

        st = stockTransferRepository.save(st);

        for (CreateStockTransferRequest.LineItem li : request.getLineItems()) {
            ProductVariant variant = productVariantRepository.findByPublicId(li.getVariantPublicId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", li.getVariantPublicId()));
            InventoryItem item = inventoryService.getOrCreateInventoryItem(storeId, li.getVariantPublicId());

            StockTransferLineItem lineItem = new StockTransferLineItem();
            lineItem.setStockTransfer(st);
            lineItem.setInventoryItem(item);
            lineItem.setProductVariant(variant);
            lineItem.setQuantity(li.getQuantity());
            st.getLineItems().add(lineItem);
        }

        st = stockTransferRepository.save(st);
        return StockTransferResponse.from(st);
    }

    @Transactional(readOnly = true)
    public PageResponse<StockTransferResponse> list(Long storeId, String status, Pageable pageable) {
        org.springframework.data.domain.Page<StockTransfer> page;
        if (status != null && !status.isBlank()) {
            StockTransfer.StockTransferStatus s = StockTransfer.StockTransferStatus.valueOf(status.toUpperCase());
            page = stockTransferRepository.findByStoreIdAndStatus(storeId, s, pageable);
        } else {
            page = stockTransferRepository.findByStoreId(storeId, pageable);
        }
        return PageResponse.of(page.map(StockTransferResponse::from));
    }

    @Transactional(readOnly = true)
    public StockTransferResponse get(Long storeId, String transferPublicId) {
        StockTransfer st = findByStoreOrThrow(storeId, transferPublicId);
        return StockTransferResponse.from(st);
    }

    @Transactional
    public StockTransferResponse update(Long storeId, String transferPublicId, UpdateStockTransferRequest request) {
        StockTransfer st = findByStoreOrThrow(storeId, transferPublicId);
        ensureDraft(st);

        if (request.getOriginPublicId() != null) {
            st.setOrigin(inventoryService.getLocation(storeId, request.getOriginPublicId()));
        }
        if (request.getDestinationPublicId() != null) {
            st.setDestination(inventoryService.getLocation(storeId, request.getDestinationPublicId()));
        }
        if (st.getOrigin() != null && st.getDestination() != null && st.getOrigin().getId().equals(st.getDestination().getId())) {
            throw new BusinessRuleException("Origin and destination locations must be different");
        }
        if (request.getExpectedArrivalDate() != null) st.setExpectedArrivalDate(request.getExpectedArrivalDate());
        if (request.getNote() != null) st.setNote(request.getNote());
        if (request.getReferenceName() != null) st.setReferenceName(request.getReferenceName());

        st = stockTransferRepository.save(st);
        return StockTransferResponse.from(st);
    }

    /**
     * DRAFT -> PENDING: Reserve stock at origin, set incoming at destination.
     */
    @Transactional
    public StockTransferResponse markPending(Long storeId, String transferPublicId) {
        StockTransfer st = findByStoreOrThrow(storeId, transferPublicId);
        ensureDraft(st);
        validateLocations(st);
        if (st.getLineItems().isEmpty()) {
            throw new BusinessRuleException("Transfer must have at least one line item");
        }

        for (StockTransferLineItem li : st.getLineItems()) {
            InventoryLevel originLevel = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), st.getOrigin());
            inventoryService.reserveAtLevel(originLevel, li.getQuantity(), "STOCK_TRANSFER", st.getPublicId());

            InventoryLevel destLevel = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), st.getDestination());
            inventoryService.incrementIncoming(destLevel, li.getQuantity());
        }

        st.setStatus(StockTransfer.StockTransferStatus.PENDING);
        st = stockTransferRepository.save(st);
        return StockTransferResponse.from(st);
    }

    /**
     * PENDING -> IN_TRANSIT: Deduct reserved stock at origin, create TRANSFER_OUT movements.
     */
    @Transactional
    public StockTransferResponse ship(Long storeId, String transferPublicId) {
        StockTransfer st = findByStoreOrThrow(storeId, transferPublicId);
        if (st.getStatus() != StockTransfer.StockTransferStatus.PENDING) {
            throw new BusinessRuleException("Can only ship a PENDING transfer");
        }

        for (StockTransferLineItem li : st.getLineItems()) {
            InventoryLevel originLevel = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), st.getOrigin());
            inventoryService.deductReservedStock(originLevel, li.getQuantity(),
                    InventoryMovement.MovementType.TRANSFER_OUT.name(),
                    "STOCK_TRANSFER", st.getPublicId());
        }

        st.setStatus(StockTransfer.StockTransferStatus.IN_TRANSIT);
        st.setShippedAt(Instant.now());
        st = stockTransferRepository.save(st);
        return StockTransferResponse.from(st);
    }

    /**
     * IN_TRANSIT|PARTIAL -> PARTIAL|RECEIVED: Add stock at destination, decrement incoming.
     */
    @Transactional
    public StockTransferResponse receive(Long storeId, String transferPublicId, ReceiveStockTransferItemsRequest request) {
        StockTransfer st = findByStoreOrThrow(storeId, transferPublicId);
        if (st.getStatus() != StockTransfer.StockTransferStatus.IN_TRANSIT
                && st.getStatus() != StockTransfer.StockTransferStatus.PARTIAL) {
            throw new BusinessRuleException("Can only receive items for IN_TRANSIT or PARTIAL transfers");
        }

        Map<String, StockTransferLineItem> lineItemMap = st.getLineItems().stream()
                .collect(Collectors.toMap(li -> li.getPublicId(), Function.identity()));

        for (ReceiveStockTransferItemsRequest.ReceivedItem ri : request.getItems()) {
            StockTransferLineItem li = lineItemMap.get(ri.getLineItemPublicId());
            if (li == null) {
                throw new ResourceNotFoundException("StockTransferLineItem", ri.getLineItemPublicId());
            }
            int remaining = li.getQuantity() - li.getReceivedQuantity() - li.getRejectedQuantity();
            int totalIncoming = ri.getQuantity() + ri.getRejectedQuantity();
            if (totalIncoming > remaining) {
                throw new BusinessRuleException("Cannot receive more than outstanding quantity (" + remaining + ") for line item " + ri.getLineItemPublicId());
            }

            li.setReceivedQuantity(li.getReceivedQuantity() + ri.getQuantity());
            li.setRejectedQuantity(li.getRejectedQuantity() + ri.getRejectedQuantity());

            InventoryLevel destLevel = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), st.getDestination());
            if (ri.getQuantity() > 0) {
                inventoryService.addAvailableStock(destLevel, ri.getQuantity(),
                        InventoryMovement.MovementType.TRANSFER_IN.name(),
                        "STOCK_TRANSFER", st.getPublicId());
            }
            inventoryService.decrementIncoming(destLevel, totalIncoming);
        }

        boolean allReceived = st.getLineItems().stream()
                .allMatch(li -> li.getReceivedQuantity() + li.getRejectedQuantity() >= li.getQuantity());

        st.setStatus(allReceived ? StockTransfer.StockTransferStatus.RECEIVED : StockTransfer.StockTransferStatus.PARTIAL);
        if (allReceived) {
            st.setReceivedAt(Instant.now());
        }

        st = stockTransferRepository.save(st);
        return StockTransferResponse.from(st);
    }

    @Transactional
    public StockTransferResponse cancel(Long storeId, String transferPublicId) {
        StockTransfer st = findByStoreOrThrow(storeId, transferPublicId);
        if (st.getStatus() == StockTransfer.StockTransferStatus.RECEIVED
                || st.getStatus() == StockTransfer.StockTransferStatus.CANCELLED) {
            throw new BusinessRuleException("Cannot cancel a " + st.getStatus().name() + " transfer");
        }

        if (st.getStatus() == StockTransfer.StockTransferStatus.PENDING) {
            for (StockTransferLineItem li : st.getLineItems()) {
                InventoryLevel originLevel = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), st.getOrigin());
                inventoryService.releaseAtLevel(originLevel, li.getQuantity(), "STOCK_TRANSFER", st.getPublicId());

                InventoryLevel destLevel = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), st.getDestination());
                inventoryService.decrementIncoming(destLevel, li.getQuantity());
            }
        }

        if (st.getStatus() == StockTransfer.StockTransferStatus.IN_TRANSIT
                || st.getStatus() == StockTransfer.StockTransferStatus.PARTIAL) {
            for (StockTransferLineItem li : st.getLineItems()) {
                int unreceived = li.getQuantity() - li.getReceivedQuantity() - li.getRejectedQuantity();
                if (unreceived > 0 && st.getDestination() != null) {
                    InventoryLevel destLevel = inventoryService.getOrCreateInventoryLevel(li.getInventoryItem(), st.getDestination());
                    inventoryService.decrementIncoming(destLevel, unreceived);
                }
            }
        }

        st.setStatus(StockTransfer.StockTransferStatus.CANCELLED);
        st = stockTransferRepository.save(st);
        return StockTransferResponse.from(st);
    }

    @Transactional
    public void delete(Long storeId, String transferPublicId) {
        StockTransfer st = findByStoreOrThrow(storeId, transferPublicId);
        ensureDraft(st);
        stockTransferRepository.delete(st);
    }

    private StockTransfer findByStoreOrThrow(Long storeId, String transferPublicId) {
        return stockTransferRepository.findByPublicIdAndStoreId(transferPublicId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("StockTransfer", transferPublicId));
    }

    private void ensureDraft(StockTransfer st) {
        if (st.getStatus() != StockTransfer.StockTransferStatus.DRAFT) {
            throw new BusinessRuleException("This operation is only allowed on DRAFT transfers");
        }
    }

    private void validateLocations(StockTransfer st) {
        if (st.getOrigin() == null) throw new BusinessRuleException("Transfer must have an origin location");
        if (st.getDestination() == null) throw new BusinessRuleException("Transfer must have a destination location");
    }

    private String nextTransferNumber(Long storeId) {
        int seq = stockTransferRepository.findMaxSequenceByStoreId(storeId) + 1;
        return "T-" + seq;
    }
}
