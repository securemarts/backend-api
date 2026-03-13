package com.securemarts.domain.inventory.dto;

import com.securemarts.domain.inventory.entity.PurchaseOrder;
import com.securemarts.domain.inventory.entity.PurchaseOrderLineItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(description = "Purchase order details")
public class PurchaseOrderResponse {

    private String publicId;
    private String number;
    private String status;
    private String supplierPublicId;
    private String supplierName;
    private String destinationPublicId;
    private String destinationName;
    private String currency;
    private BigDecimal shippingCost;
    private BigDecimal adjustmentsCost;
    private BigDecimal taxCost;
    private String note;
    private LocalDate expectedOn;
    private Instant orderedAt;
    private Instant receivedAt;
    private LocalDate paymentDueOn;
    private boolean paid;
    private BigDecimal totalCost;
    private List<LineItemResponse> lineItems;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @Schema(description = "Purchase order line item")
    public static class LineItemResponse {
        private String publicId;
        private String inventoryItemPublicId;
        private String variantPublicId;
        private String variantSku;
        private String variantTitle;
        private String productTitle;
        private int quantity;
        private int receivedQuantity;
        private int rejectedQuantity;
        private BigDecimal costPrice;
        private BigDecimal retailPrice;
    }

    public static PurchaseOrderResponse from(PurchaseOrder po) {
        BigDecimal lineItemsTotal = BigDecimal.ZERO;
        List<LineItemResponse> items = List.of();
        if (po.getLineItems() != null) {
            items = po.getLineItems().stream().map(PurchaseOrderResponse::mapLineItem).toList();
            for (PurchaseOrderLineItem li : po.getLineItems()) {
                if (li.getCostPrice() != null) {
                    lineItemsTotal = lineItemsTotal.add(li.getCostPrice().multiply(BigDecimal.valueOf(li.getQuantity())));
                }
            }
        }
        BigDecimal total = lineItemsTotal
                .add(po.getShippingCost() != null ? po.getShippingCost() : BigDecimal.ZERO)
                .add(po.getAdjustmentsCost() != null ? po.getAdjustmentsCost() : BigDecimal.ZERO)
                .add(po.getTaxCost() != null ? po.getTaxCost() : BigDecimal.ZERO);

        return PurchaseOrderResponse.builder()
                .publicId(po.getPublicId())
                .number(po.getNumber())
                .status(po.getStatus().name())
                .supplierPublicId(po.getSupplier() != null ? po.getSupplier().getPublicId() : null)
                .supplierName(po.getSupplier() != null ? po.getSupplier().getName() : null)
                .destinationPublicId(po.getDestination() != null ? po.getDestination().getPublicId() : null)
                .destinationName(po.getDestination() != null ? po.getDestination().getName() : null)
                .currency(po.getCurrency())
                .shippingCost(po.getShippingCost())
                .adjustmentsCost(po.getAdjustmentsCost())
                .taxCost(po.getTaxCost())
                .note(po.getNote())
                .expectedOn(po.getExpectedOn())
                .orderedAt(po.getOrderedAt())
                .receivedAt(po.getReceivedAt())
                .paymentDueOn(po.getPaymentDueOn())
                .paid(po.isPaid())
                .totalCost(total)
                .lineItems(items)
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    private static LineItemResponse mapLineItem(PurchaseOrderLineItem li) {
        return LineItemResponse.builder()
                .publicId(li.getPublicId())
                .inventoryItemPublicId(li.getInventoryItem() != null ? li.getInventoryItem().getPublicId() : null)
                .variantPublicId(li.getProductVariant() != null ? li.getProductVariant().getPublicId() : null)
                .variantSku(li.getProductVariant() != null ? li.getProductVariant().getSku() : null)
                .variantTitle(li.getProductVariant() != null ? li.getProductVariant().getTitle() : null)
                .productTitle(li.getProductVariant() != null && li.getProductVariant().getProduct() != null
                        ? li.getProductVariant().getProduct().getTitle() : null)
                .quantity(li.getQuantity())
                .receivedQuantity(li.getReceivedQuantity())
                .rejectedQuantity(li.getRejectedQuantity())
                .costPrice(li.getCostPrice())
                .retailPrice(li.getRetailPrice())
                .build();
    }
}
