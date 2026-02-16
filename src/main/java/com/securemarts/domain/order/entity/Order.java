package com.securemarts.domain.order.entity;

import com.securemarts.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_store_id", columnList = "store_id"),
        @Index(name = "idx_orders_store_number", columnList = "store_id, order_number", unique = true)
})
@Getter
@Setter
public class Order extends BaseEntity {

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    /** Optional delivery address at checkout; when set and payment succeeds, delivery order is created. */
    @Column(name = "delivery_address", length = 500)
    private String deliveryAddress;

    @Column(name = "delivery_lat", precision = 10, scale = 7)
    private BigDecimal deliveryLat;

    @Column(name = "delivery_lng", precision = 10, scale = 7)
    private BigDecimal deliveryLng;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PAID,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
}
