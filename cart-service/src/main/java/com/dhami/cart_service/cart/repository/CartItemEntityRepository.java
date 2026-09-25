package com.dhami.cart_service.cart.repository;

import com.dhami.cart_service.cart.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemEntityRepository extends JpaRepository<CartItemEntity, Long> {

    Optional<CartItemEntity> findByCartIdAndProductId(
            Long cartId,
            Long productId
    );

    Optional<CartItemEntity> findByIdAndCart_UserId(
            Long itemId,
            Long userId
    );
}