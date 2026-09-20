package com.dhami.ecom.order_service.service;

import com.dhami.ecom.order_service.entity.OrderEntity;

public interface OrderNotificationService {

    void onOrderCreated(OrderEntity order);

    void onOrderCancelled(OrderEntity order);

    void onOrderDelivered(OrderEntity order);
}
