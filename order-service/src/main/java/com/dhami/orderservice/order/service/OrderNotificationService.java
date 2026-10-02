package com.dhami.orderservice.order.service;

import com.dhami.orderservice.order.OrderEntity;

public interface OrderNotificationService {

    void onOrderCreated(OrderEntity order);

    void onOrderCancelled(OrderEntity order);

    void onOrderDelivered(OrderEntity order);
}
