package com.securemarts.domain.order.dto;

import com.securemarts.domain.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Order response")
public class OrderResponse {

    private String publicId;
    private String orderNumber;
    @Schema(description = "Order status", allowableValues = {"PENDING", "CONFIRMED", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"})
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private Long storeId;
    private Long customerId;
    private List<OrderLineDto> items;
    private Instant createdAt;

    @Data
    @Builder
    public static class OrderLineDto {
        private String publicId;
        private String variantPublicId;
        private String variantSku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .publicId(order.getPublicId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .storeId(order.getStoreId())
                .customerId(order.getCustomerId())
                .items(order.getItems() != null ? order.getItems().stream()
                        .map(oi -> OrderLineDto.builder()
                                .publicId(oi.getPublicId())
                                .variantPublicId(oi.getProductVariant().getPublicId())
                                .variantSku(oi.getProductVariant().getSku())
                                .quantity(oi.getQuantity())
                                .unitPrice(oi.getUnitPrice())
                                .totalPrice(oi.getTotalPrice())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
