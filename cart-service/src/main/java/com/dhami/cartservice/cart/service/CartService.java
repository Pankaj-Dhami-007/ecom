package com.dhami.cartservice.cart.service;

import com.dhami.cartservice.cart.dto.AddToCartRequest;
import com.dhami.cartservice.cart.dto.CartResponse;
import com.dhami.cartservice.cart.dto.CheckoutRequest;
import com.dhami.cartservice.cart.dto.UpdateCartItemRequest;
import com.dhami.cartservice.client.order.dto.OrderResponse;
import com.dhami.cartservice.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CartService {

    CartResponse getMyCart();

    CartResponse getCartById(Long cartId);

    PageResponse<CartResponse> getMyCarts(Pageable pageable);

    CartResponse addItemToCart(AddToCartRequest request);

    CartResponse updateCartItem(Long itemId, UpdateCartItemRequest request);

    CartResponse removeCartItem(Long itemId);

    CartResponse clearCart();

    OrderResponse checkoutCart(Long cartId, CheckoutRequest request);

    CartResponse markCartConverted(Long cartId);
}
