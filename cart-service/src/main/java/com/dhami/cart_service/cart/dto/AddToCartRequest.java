package com.dhami.cart_service.cart.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddToCartRequest(

        @NotEmpty(message = "Cart must contain at least one item")
        List<@Valid CartItemRequest> items
) {
}