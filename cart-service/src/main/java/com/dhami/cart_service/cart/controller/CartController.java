package com.dhami.cart_service.cart.controller;

import com.dhami.cart_service.cart.dto.AddToCartRequest;
import com.dhami.cart_service.cart.dto.CartResponse;
import com.dhami.cart_service.cart.dto.UpdateCartItemRequest;
import com.dhami.cart_service.cart.service.CartService;
import com.dhami.cart_service.common.dto.ApiResponse;
import com.dhami.cart_service.common.dto.PageResponse;
import com.dhami.cart_service.constants.Routes;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> createCart() {
        log.info("Create cart request received");
        CartResponse response = cartService.createCart();
        log.info("Cart created successfully with id: {}", response.id());
        return new ResponseEntity<>(
                ApiResponse.success(response, "Cart created successfully"),
                HttpStatus.CREATED
        );
    }

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
        log.info("Add to cart request received with {} item(s)", request.items().size());
        CartResponse response = cartService.addItemToCart(request);
        log.info("Items added successfully to cart id: {}", response.id());
        return ResponseEntity.ok(
                ApiResponse.success(response, "Items added to cart successfully")
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
    public ResponseEntity<ApiResponse<CartResponse>> checkoutCart(@PathVariable Long id) {
        log.info("Checkout request received for cart id: {}", id);
        CartResponse response = cartService.checkoutCart(id);
        log.info("Cart id: {} moved to checkout successfully", id);
        return ResponseEntity.ok(
                ApiResponse.success(response, "Cart moved to checkout successfully")
        );
    }
}
