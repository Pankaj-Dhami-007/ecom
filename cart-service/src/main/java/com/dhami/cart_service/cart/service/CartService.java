package com.dhami.cart_service.cart.service;

import com.dhami.cart_service.cart.dto.AddToCartRequest;
import com.dhami.cart_service.cart.dto.CartResponse;
import com.dhami.cart_service.cart.dto.UpdateCartItemRequest;
import com.dhami.cart_service.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CartService {

    CartResponse createCart();

    CartResponse getMyCart();

    CartResponse getCartById(Long cartId);

    PageResponse<CartResponse> getAllCarts(Pageable pageable);

    PageResponse<CartResponse> getMyCarts(Pageable pageable);

    CartResponse addItemToCart(AddToCartRequest request);

    CartResponse updateCartItem(Long itemId, UpdateCartItemRequest request);

    CartResponse removeCartItem(Long itemId);

    CartResponse clearCart();

    CartResponse checkoutCart(Long cartId);
}
