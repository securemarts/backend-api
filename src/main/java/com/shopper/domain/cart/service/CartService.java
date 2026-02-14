package com.shopper.domain.cart.service;

import com.shopper.common.exception.BusinessRuleException;
import com.shopper.common.exception.ResourceNotFoundException;
import com.shopper.domain.cart.dto.CartItemRequest;
import com.shopper.domain.cart.dto.CartResponse;
import com.shopper.domain.cart.entity.Cart;
import com.shopper.domain.cart.entity.CartItem;
import com.shopper.domain.cart.repository.CartRepository;
import com.shopper.domain.catalog.entity.ProductVariant;
import com.shopper.domain.catalog.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;

    private static final int CART_EXPIRY_DAYS = 30;

    @Transactional
    public Cart getOrCreateCart(Long storeId, Long customerId, String cartToken) {
        if (cartToken != null && !cartToken.isBlank()) {
            Optional<Cart> byToken = cartRepository.findByCartToken(cartToken);
            if (byToken.isPresent() && byToken.get().getStoreId().equals(storeId)) {
                return byToken.get();
            }
        }
        if (customerId != null) {
            Optional<Cart> byCustomer = cartRepository.findByStoreIdAndCustomerId(storeId, customerId);
            if (byCustomer.isPresent()) return byCustomer.get();
        }
        Cart cart = new Cart();
        cart.setStoreId(storeId);
        cart.setCustomerId(customerId);
        cart.setCartToken(UUID.randomUUID().toString().replace("-", ""));
        cart.setExpiresAt(Instant.now().plusSeconds(CART_EXPIRY_DAYS * 86400L));
        return cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Cart getCart(Long storeId, String cartPublicId) {
        Cart cart = cartRepository.findByPublicId(cartPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartPublicId));
        if (!cart.getStoreId().equals(storeId)) {
            throw new ResourceNotFoundException("Cart", cartPublicId);
        }
        return cart;
    }

    @Transactional
    public CartResponse addItem(Long storeId, Long customerId, String cartToken, CartItemRequest request) {
        Cart cart = getOrCreateCart(storeId, customerId, cartToken);
        ProductVariant variant = productVariantRepository.findByPublicId(request.getVariantPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", request.getVariantPublicId()));
        if (!variant.getProduct().getStoreId().equals(storeId)) {
            throw new BusinessRuleException("Variant does not belong to this store's catalog");
        }
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductVariant().getId().equals(variant.getId()))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductVariant(variant);
            item.setQuantity(request.getQuantity());
            cart.getItems().add(item);
        }
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Long storeId, String cartPublicId, String cartItemPublicId, int quantity) {
        Cart cart = getCart(storeId, cartPublicId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getPublicId().equals(cartItemPublicId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", cartItemPublicId));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long storeId, String cartPublicId, String cartItemPublicId) {
        Cart cart = getCart(storeId, cartPublicId);
        boolean removed = cart.getItems().removeIf(i -> i.getPublicId().equals(cartItemPublicId));
        if (!removed) throw new ResourceNotFoundException("CartItem", cartItemPublicId);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartResponse(Long storeId, String cartPublicId) {
        Cart cart = getCart(storeId, cartPublicId);
        return toResponse(cart);
    }

    /** Get or create cart and return as response (e.g. when only cart token is sent). */
    @Transactional
    public CartResponse getOrCreateCartResponse(Long storeId, Long customerId, String cartToken) {
        Cart cart = getOrCreateCart(storeId, customerId, cartToken);
        return toResponse(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartLineDto> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        String currency = "NGN";
        for (CartItem i : cart.getItems()) {
            var variant = i.getProductVariant();
            var product = variant.getProduct();
            BigDecimal unitPrice = variant.getPriceAmount() != null ? variant.getPriceAmount() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(i.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            if (variant.getCurrency() != null) currency = variant.getCurrency();
            String productTitle = product != null ? product.getTitle() : null;
            String variantTitle = variant.getTitle();
            if (variantTitle == null || variantTitle.isBlank() || "Default".equals(variantTitle)) {
                variantTitle = productTitle != null ? productTitle : "Default";
            }
            lines.add(CartResponse.CartLineDto.builder()
                    .publicId(i.getPublicId())
                    .variantPublicId(variant.getPublicId())
                    .variantSku(variant.getSku())
                    .variantTitle(variantTitle)
                    .productTitle(productTitle)
                    .quantity(i.getQuantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build());
        }
        return CartResponse.from(cart, lines, subtotal, currency);
    }
}
