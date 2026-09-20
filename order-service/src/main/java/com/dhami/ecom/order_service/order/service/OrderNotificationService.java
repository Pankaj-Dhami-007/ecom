package com.dhami.ecom.order_service.order.service;

import com.dhami.ecom.order_service.order.OrderEntity;

public interface OrderNotificationService {

    void onOrderCreated(OrderEntity order);

    void onOrderCancelled(OrderEntity order);

    void onOrderDelivered(OrderEntity order);
}
