package com.shopper.domain.cart.repository;

import com.shopper.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByPublicId(String publicId);

    Optional<Cart> findByCartToken(String cartToken);

    Optional<Cart> findByStoreIdAndCustomerId(Long storeId, Long customerId);
}
