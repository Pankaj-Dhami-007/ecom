package com.dhami.cartservice.client.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    private ShippingAddressRequest shippingAddress;
    private List<OrderItemRequest> items;

    // order-service stores this so payment-service can convert this cart
    // once the payment succeeds
    private Long cartId;
}
