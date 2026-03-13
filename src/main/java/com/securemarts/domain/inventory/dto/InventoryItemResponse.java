package com.securemarts.domain.inventory.dto;

import com.securemarts.domain.inventory.entity.InventoryItem;
import com.securemarts.domain.inventory.entity.InventoryLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Inventory level response (variant + location + quantities)")
public class InventoryItemResponse {

    private String publicId;
    @Schema(description = "Inventory item (store+variant) public ID")
    private String inventoryItemId;
    private String productVariantId;
    private String variantSku;
    private String locationId;
    private String locationName;
    private int quantityAvailable;
    private int quantityReserved;
    @Schema(description = "True when quantity available is 0 or less")
    private boolean lowStock;

    public static InventoryItemResponse from(InventoryLevel level) {
        if (level == null) return null;
        InventoryItem item = level.getInventoryItem();
        return InventoryItemResponse.builder()
                .publicId(level.getPublicId())
                .inventoryItemId(item != null ? item.getPublicId() : null)
                .productVariantId(item != null && item.getProductVariant() != null ? item.getProductVariant().getPublicId() : null)
                .variantSku(item != null && item.getProductVariant() != null ? item.getProductVariant().getSku() : null)
                .locationId(level.getLocation() != null ? level.getLocation().getPublicId() : null)
                .locationName(level.getLocation() != null ? level.getLocation().getName() : null)
                .quantityAvailable(level.getQuantityAvailable())
                .quantityReserved(level.getQuantityReserved())
                .lowStock(level.getQuantityAvailable() <= 0)
                .build();
    }

    /** Single level summary when you only have the item (e.g. first level or aggregated). */
    public static InventoryItemResponse from(InventoryItem item) {
        if (item == null) return null;
        if (item.getLevels() != null && !item.getLevels().isEmpty()) {
            return from(item.getLevels().get(0));
        }
        return InventoryItemResponse.builder()
                .inventoryItemId(item.getPublicId())
                .productVariantId(item.getProductVariant() != null ? item.getProductVariant().getPublicId() : null)
                .variantSku(item.getProductVariant() != null ? item.getProductVariant().getSku() : null)
                .locationId(null)
                .locationName(null)
                .quantityAvailable(0)
                .quantityReserved(0)
                .lowStock(true)
                .build();
    }
}
