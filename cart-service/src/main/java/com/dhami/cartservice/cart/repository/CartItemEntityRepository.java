package com.dhami.cartservice.cart.repository;

import com.dhami.cartservice.cart.entity.CartItemEntity;
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