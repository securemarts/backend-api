package com.securemarts.domain.inventory.dto;

import com.securemarts.domain.inventory.entity.StockTransfer;
import com.securemarts.domain.inventory.entity.StockTransferLineItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@Schema(description = "Stock transfer details")
public class StockTransferResponse {

    private String publicId;
    private String number;
    private String status;
    private String originPublicId;
    private String originName;
    private String destinationPublicId;
    private String destinationName;
    private LocalDate expectedArrivalDate;
    private Instant shippedAt;
    private Instant receivedAt;
    private String note;
    private String referenceName;
    private List<LineItemResponse> lineItems;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @Schema(description = "Stock transfer line item")
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
    }

    public static StockTransferResponse from(StockTransfer st) {
        List<LineItemResponse> items = List.of();
        if (st.getLineItems() != null) {
            items = st.getLineItems().stream().map(StockTransferResponse::mapLineItem).toList();
        }
        return StockTransferResponse.builder()
                .publicId(st.getPublicId())
                .number(st.getNumber())
                .status(st.getStatus().name())
                .originPublicId(st.getOrigin() != null ? st.getOrigin().getPublicId() : null)
                .originName(st.getOrigin() != null ? st.getOrigin().getName() : null)
                .destinationPublicId(st.getDestination() != null ? st.getDestination().getPublicId() : null)
                .destinationName(st.getDestination() != null ? st.getDestination().getName() : null)
                .expectedArrivalDate(st.getExpectedArrivalDate())
                .shippedAt(st.getShippedAt())
                .receivedAt(st.getReceivedAt())
                .note(st.getNote())
                .referenceName(st.getReferenceName())
                .lineItems(items)
                .createdAt(st.getCreatedAt())
                .updatedAt(st.getUpdatedAt())
                .build();
    }

    private static LineItemResponse mapLineItem(StockTransferLineItem li) {
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
                .build();
    }
}
