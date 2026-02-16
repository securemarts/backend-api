package com.securemarts.domain.inventory.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "inventory_movements", indexes = {
        @Index(name = "idx_inventory_movements_item_id", columnList = "inventory_item_id")
})
@Getter
@Setter
public class InventoryMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id", nullable = false)
    private InventoryItem inventoryItem;

    @Column(name = "quantity_delta", nullable = false)
    private int quantityDelta;

    @Column(name = "movement_type", nullable = false, length = 30)
    private String movementType;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id", length = 36)
    private String referenceId;

    public enum MovementType {
        ADJUSTMENT,
        SALE,
        RESTOCK,
        RESERVE,
        RELEASE,
        RETURN,
        TRANSFER_IN,
        TRANSFER_OUT
    }
}
