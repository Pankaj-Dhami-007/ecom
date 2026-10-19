package com.dhami.cartservice.cart.repository;

import com.dhami.cartservice.cart.entity.CartEntity;
import com.dhami.cartservice.cart.enums.CartStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartEntityRepository extends JpaRepository<CartEntity, Long> {

    Optional<CartEntity> findFirstByUserIdAndStatusOrderByUpdatedAtDesc(
            Long userId,
            CartStatusEnum status
    );

    Page<CartEntity> findByUserIdAndStatus(Long userId, CartStatusEnum status, Pageable pageable);
    Optional<CartEntity> findByOrderId(Long orderId);
}