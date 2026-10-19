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

    /**
     * Moves the cart behind an order to CONVERTED once its payment succeeds.
     *
     * <p>Called by the Kafka listener that consumes payment.succeeded, not by any HTTP
     * endpoint. It therefore takes an order id instead of a cart id, because that is what
     * the event carries, and it does not check the caller's user: there is no authenticated
     * caller on an event.
     *
     * <p>Safe to run more than once for the same order. See the implementation for how
     * repeats are handled.
     *
     * @param orderId the order whose payment succeeded
     */
    void markCartConvertedForOrder(Long orderId);
}
