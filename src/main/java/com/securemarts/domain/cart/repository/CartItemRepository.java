package com.securemarts.domain.cart.repository;

import com.securemarts.domain.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    void deleteByCartId(Long cartId);
}
