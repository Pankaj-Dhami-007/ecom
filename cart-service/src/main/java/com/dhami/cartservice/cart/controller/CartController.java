package com.dhami.cartservice.cart.controller;

import com.dhami.cartservice.cart.dto.AddToCartRequest;
import com.dhami.cartservice.cart.dto.CartResponse;
import com.dhami.cartservice.cart.dto.CheckoutRequest;
import com.dhami.cartservice.cart.dto.UpdateCartItemRequest;
import com.dhami.cartservice.cart.service.CartService;
import com.dhami.cartservice.client.order.dto.OrderResponse;
import com.dhami.cartservice.common.dto.ApiResponse;
import com.dhami.cartservice.common.dto.PageResponse;
import com.dhami.cartservice.constants.Routes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1 + Routes.Carts.ROOT)
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping(Routes.Carts.ME)
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart() {
        log.info("Fetching my cart");
        return ResponseEntity.ok(
                ApiResponse.success(cartService.getMyCart(), "Cart retrieved successfully")
        );
    }

    @GetMapping(Routes.Carts.ID)
    public ResponseEntity<ApiResponse<CartResponse>> getCartById(@PathVariable Long id) {
        log.info("Fetching cart by id: {}", id);
        return ResponseEntity.ok(
                ApiResponse.success(cartService.getCartById(id), "Cart retrieved successfully")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CartResponse>>> getMyCarts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Fetching my carts - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(
                ApiResponse.success(
                        cartService.getMyCarts(pageable),
                        "Carts retrieved successfully"
                )
        );
    }

    @PostMapping(Routes.Carts.ME_ITEMS)
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody AddToCartRequest request) {
        log.info("Add to cart request received for product id: {} with quantity: {}",
                request.productId(), request.quantity());
        CartResponse response = cartService.addItemToCart(request);
        log.info("Product id: {} added to cart id: {}", request.productId(), response.id());
        return ResponseEntity.ok(
                ApiResponse.success(response, "Item added to cart successfully")
        );
    }

    @PutMapping(Routes.Carts.ME_ITEM_ID)
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        log.info("Update cart item request received for item id: {}", itemId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        cartService.updateCartItem(itemId, request),
                        "Cart item updated successfully"
                )
        );
    }

    @DeleteMapping(Routes.Carts.ME_ITEM_ID)
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(@PathVariable Long itemId) {
        log.info("Remove cart item request received for item id: {}", itemId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        cartService.removeCartItem(itemId),
                        "Item removed from cart successfully"
                )
        );
    }

    @DeleteMapping(Routes.Carts.ME)
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        log.info("Clear cart request received");
        return ResponseEntity.ok(
                ApiResponse.success(cartService.clearCart(), "Cart cleared successfully")
        );
    }

    @PatchMapping(Routes.Carts.ID_CHECKOUT)
    public ResponseEntity<ApiResponse<OrderResponse>> checkoutCart(
            @PathVariable Long id,
            @Valid @RequestBody CheckoutRequest request) {
        log.info("Checkout request received for cart id: {}", id);
        OrderResponse order = cartService.checkoutCart(id, request);
        log.info("Order {} placed successfully from cart id: {}", order.getId(), id);
        return ResponseEntity.ok(
                ApiResponse.success(order, "Order placed successfully")
        );
    }

    @PatchMapping(Routes.Carts.ID_CONVERT)
    public ResponseEntity<ApiResponse<CartResponse>> markCartConverted(@PathVariable Long id) {
        log.info("Mark cart as converted request received for cart id: {}", id);
        CartResponse cart = cartService.markCartConverted(id);
        log.info("Cart id: {} marked as CONVERTED", id);
        return ResponseEntity.ok(
                ApiResponse.success(cart, "Cart marked as converted")
        );
    }
}
