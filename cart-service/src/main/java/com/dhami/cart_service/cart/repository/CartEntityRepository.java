package com.dhami.cart_service.cart.repository;

import com.dhami.cart_service.cart.entity.CartEntity;
import com.dhami.cart_service.cart.enums.CartStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartEntityRepository extends JpaRepository<CartEntity, Long> {

    Optional<CartEntity> findFirstByUserIdAndStatusOrderByUpdatedAtDesc(
            Long userId,
            CartStatusEnum status
    );

    Optional<CartEntity> findByUserIdAndStatus(
            Long userId,
            CartStatusEnum status
    );

    Page<CartEntity> findByUserId(Long userId, Pageable pageable);

    Page<CartEntity> findByStatus(CartStatusEnum status, Pageable pageable);
}