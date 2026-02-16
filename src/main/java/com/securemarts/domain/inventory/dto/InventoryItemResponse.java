package com.securemarts.domain.inventory.dto;

import com.securemarts.domain.inventory.entity.InventoryItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Inventory item response")
public class InventoryItemResponse {

    private String publicId;
    private String productVariantId;
    private String variantSku;
    private String locationId;
    private String locationName;
    private int quantityAvailable;
    private int quantityReserved;
    private Integer lowStockThreshold;
    private boolean lowStock;

    public static InventoryItemResponse from(InventoryItem item) {
        return InventoryItemResponse.builder()
                .publicId(item.getPublicId())
                .productVariantId(item.getProductVariant() != null ? item.getProductVariant().getPublicId() : null)
                .variantSku(item.getProductVariant() != null ? item.getProductVariant().getSku() : null)
                .locationId(item.getLocation() != null ? item.getLocation().getPublicId() : null)
                .locationName(item.getLocation() != null ? item.getLocation().getName() : null)
                .quantityAvailable(item.getQuantityAvailable())
                .quantityReserved(item.getQuantityReserved())
                .lowStockThreshold(item.getLowStockThreshold())
                .lowStock(item.isLowStock())
                .build();
    }
}
