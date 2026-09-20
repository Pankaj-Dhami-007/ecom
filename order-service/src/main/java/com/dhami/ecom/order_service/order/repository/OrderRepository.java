package com.dhami.ecom.order_service.order.repository;

import com.dhami.ecom.order_service.order.OrderEntity;
import com.dhami.ecom.order_service.order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);
}
