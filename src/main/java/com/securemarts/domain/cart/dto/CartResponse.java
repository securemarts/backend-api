package com.securemarts.domain.cart.dto;

import com.securemarts.domain.cart.entity.Cart;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Schema(description = "Cart response with line items and totals")
public class CartResponse {

    private String publicId;
    private Long storeId;
    private String cartToken;
    private Instant expiresAt;
    private List<CartLineDto> items;
    private int itemCount;
    private BigDecimal subtotal;
    private String currency;

    @Data
    @Builder
    public static class CartLineDto {
        private String publicId;
        private String variantPublicId;
        private String variantSku;
        private String variantTitle;
        @Schema(description = "Product title (for display as item name)")
        private String productTitle;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal lineTotal;
    }

    public static CartResponse from(Cart cart, List<CartLineDto> lines, BigDecimal subtotal, String currency) {
        int count = lines.stream().mapToInt(CartLineDto::getQuantity).sum();
        return CartResponse.builder()
                .publicId(cart.getPublicId())
                .storeId(cart.getStoreId())
                .cartToken(cart.getCartToken())
                .expiresAt(cart.getExpiresAt())
                .items(lines)
                .itemCount(count)
                .subtotal(subtotal)
                .currency(currency != null ? currency : "NGN")
                .build();
    }
}
