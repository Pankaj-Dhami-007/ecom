package com.dhami.cartservice.cart.service;

import com.dhami.cartservice.cart.CartServiceException;
import com.dhami.cartservice.cart.dto.AddToCartRequest;
import com.dhami.cartservice.cart.dto.CartResponse;
import com.dhami.cartservice.cart.dto.CheckoutRequest;
import com.dhami.cartservice.cart.dto.UpdateCartItemRequest;
import com.dhami.cartservice.cart.entity.CartEntity;
import com.dhami.cartservice.cart.entity.CartItemEntity;
import com.dhami.cartservice.cart.enums.CartStatusEnum;
import com.dhami.cartservice.cart.mapper.CartMapper;
import com.dhami.cartservice.cart.repository.CartEntityRepository;
import com.dhami.cartservice.cart.repository.CartItemEntityRepository;
import com.dhami.cartservice.client.order.OrderClient;
import com.dhami.cartservice.client.order.dto.CreateOrderRequest;
import com.dhami.cartservice.client.order.dto.OrderItemRequest;
import com.dhami.cartservice.client.order.dto.OrderResponse;
import com.dhami.cartservice.client.catalog.ProductClient;
import com.dhami.cartservice.client.catalog.dto.ProductResponse;
import com.dhami.cartservice.common.context.RequestUserContext;
import com.dhami.cartservice.common.dto.ApiResponse;
import com.dhami.cartservice.common.dto.PageResponse;
import com.dhami.cartservice.common.exception.AccessDeniedException;
import com.dhami.cartservice.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";

    private static final String DEFAULT_CURRENCY = "INR";
    private static final String ACTIVE_CART_NOT_FOUND = "No active cart found for user: ";

    private final CartEntityRepository cartRepository;
    private final CartItemEntityRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        Long userId = RequestUserContext.requireUserId();
        CartEntity cart = findActiveCart(userId).orElseGet(() -> emptyCart(userId));
        log.info("Fetching active cart for user id: {}", userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartById(Long cartId) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Fetching cart by id: {} for user id: {}", cartId, userId);
        CartEntity cart = findCartOrThrow(cartId);
        assertOwnership(cart.getUserId(), userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CartResponse> getMyCarts(Pageable pageable) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Fetching active carts for user id: {} - page: {}, size: {}", userId, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                cartRepository.findByUserIdAndStatus(userId, CartStatusEnum.ACTIVE, pageable)
                        .map(cartMapper::toResponse)
        );
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(AddToCartRequest request) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Adding product id: {} with quantity: {} to cart for user id: {}",
                request.productId(), request.quantity(), userId);

        CartEntity cart = findOrCreateActiveCart(userId);

        CartItemEntity existing = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.productId())
                .orElse(null);

        int quantityInCart = existing == null || existing.getQuantity() == null
                ? 0
                : existing.getQuantity();

        // check the quantity the cart will end up with, not just the increment: adding to a
        // line that already holds most of the stock must not slip past the stock check
        int resultingQuantity = quantityInCart + request.quantity();
        ProductResponse product = validateProductForQuantity(request.productId(), resultingQuantity);

        if (existing == null) {
            CartItemEntity newItem = CartItemEntity.builder()
                    .cart(cart)
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(request.quantity())
                    .unitPrice(product.getPrice())
                    .build();
            cart.addItem(newItem);
        } else {
            log.info("Product id: {} is already in cart id: {} with quantity {} - raising it to {}",
                    request.productId(), cart.getId(), quantityInCart, resultingQuantity);
            existing.setQuantity(resultingQuantity);
            existing.setUnitPrice(product.getPrice());
            existing.setProductName(product.getName());
            cartItemRepository.save(existing);
        }

        CartEntity saved = cartRepository.save(cart);
        log.info("Product id: {} added to cart id: {} for user id: {}",
                request.productId(), saved.getId(), userId);
        return cartMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long itemId, UpdateCartItemRequest request) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Updating cart item id: {} for user id: {} with quantity: {}", itemId, userId, request.quantity());

        CartItemEntity item = cartItemRepository
                .findByIdAndCart_UserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));

        CartEntity cart = item.getCart();
        requireActiveCart(cart);

        ProductResponse product = validateProductForQuantity(item.getProductId(), request.quantity());

        item.setQuantity(request.quantity());
        item.setUnitPrice(product.getPrice());
        item.setProductName(product.getName());

        cartItemRepository.save(item);
        log.info("Cart item id: {} updated successfully for user id: {}", itemId, userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long itemId) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Removing cart item id: {} for user id: {}", itemId, userId);

        CartItemEntity item = cartItemRepository
                .findByIdAndCart_UserId(itemId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", itemId));

        CartEntity cart = item.getCart();
        requireActiveCart(cart);

        cart.removeItem(item);
        cartRepository.save(cart);
        log.info("Cart item id: {} removed successfully for user id: {}", itemId, userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clearCart() {
        Long userId = RequestUserContext.requireUserId();
        log.info("Clearing cart for user id: {}", userId);

        CartEntity cart = findActiveCart(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ACTIVE_CART_NOT_FOUND + userId));

        cart.getItems().clear();
        CartEntity saved = cartRepository.save(cart);
        log.info("Cart cleared successfully for user id: {}", userId);
        return cartMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse checkoutCart(Long cartId, CheckoutRequest request) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Checkout requested for cart id: {} and user id: {}", cartId, userId);

        CartEntity cart = findCartOrThrow(cartId);
        assertOwnership(cart.getUserId(), userId);

        if (cart.getStatus() == CartStatusEnum.CHECKOUT) {
            // order already exists for this cart and payment is not settled yet.
            // return the same order instead of creating a duplicate one.
            return resumeCheckout(cart);
        }

        if (cart.getStatus() != CartStatusEnum.ACTIVE) {
            log.warn("Checkout failed - cart {} status is {}", cartId, cart.getStatus());
            throw new CartServiceException(
                    "Cart can only be checked out when status is ACTIVE. Current status: " + cart.getStatus()
            );
        }

        if (cart.getItems().isEmpty()) {
            log.warn("Checkout failed - cart {} is empty", cartId);
            throw new CartServiceException("Cart is empty, cannot proceed to checkout");
        }

        OrderResponse order = placeOrder(cart, request);

        // The order is already settled by now: order-service reserved the stock, called
        // payment-service, and payment-service called back to confirm or fail the order.
        // Re-reading it is what tells the cart whether it may become CONVERTED.
        OrderResponse settled = fetchSettledOrder(order.getId());

        cart.setOrderId(order.getId());
        if (settled != null && ORDER_STATUS_CONFIRMED.equals(settled.getStatus())) {
            cart.changeStatus(CartStatusEnum.CONVERTED);
            log.info("Payment confirmed - cart {} is now CONVERTED against order {}", cartId, order.getId());
        } else {
            // payment did not succeed: stay CHECKOUT so the user can retry this same
            // order instead of losing the cart
            cart.changeStatus(CartStatusEnum.CHECKOUT);
            log.info("Payment not confirmed - cart {} stays CHECKOUT against order {}", cartId, order.getId());
        }
        cartRepository.save(cart);

        log.info("Checkout placed - cart {} resolved against order {} for user id: {}",
                cartId, order.getId(), userId);
        return settled != null ? settled : order;
    }

    private OrderResponse fetchSettledOrder(Long orderId) {
        try {
            ApiResponse<OrderResponse> response = orderClient.getOrderById(orderId);
            if (response == null || response.getData() == null) {
                log.error("Could not read settled state of order id: {} - treating it as not confirmed", orderId);
                return null;
            }
            return response.getData();
        } catch (Exception e) {
            log.error("Could not reach order service to settle order id: {} - treating it as not confirmed", orderId, e);
            return null;
        }
    }

    private OrderResponse resumeCheckout(CartEntity cart) {
        Long orderId = cart.getOrderId();
        if (orderId == null) {
            log.warn("Cannot resume checkout - cart {} is CHECKOUT without an order id", cart.getId());
            throw new CartServiceException("Cart is in an incomplete checkout state, please try again");
        }

        log.info("Resuming checkout for cart {} - returning existing order {}", cart.getId(), orderId);
        OrderResponse order = loadOrderOrThrow(orderId);

        // a retry can be the moment the payment finally succeeds, so settle the cart here too
        if (ORDER_STATUS_CONFIRMED.equals(order.getStatus())) {
            cart.changeStatus(CartStatusEnum.CONVERTED);
            cartRepository.save(cart);
            log.info("Retry settled - cart {} is now CONVERTED against order {}", cart.getId(), orderId);
        }

        return order;
    }

    private OrderResponse loadOrderOrThrow(Long orderId) {
        ApiResponse<OrderResponse> response = orderClient.getOrderById(orderId);

        if (response == null || response.getData() == null) {
            log.error("Order service returned empty response for existing order id: {}", orderId);
            throw new CartServiceException("Unable to load the existing order for this cart, please try again");
        }

        return response.getData();
    }

    /**
     * Marks a CHECKOUT cart as CONVERTED on demand. Checkout normally settles the cart
     * itself by reading the order status, so this exists as the idempotent repair hook for
     * the case where the order was confirmed but the cart update did not stick.
     */
    @Override
    @Transactional
    public CartResponse markCartConverted(Long cartId) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Marking cart id: {} as CONVERTED for user id: {}", cartId, userId);

        CartEntity cart = findCartOrThrow(cartId);
        assertOwnership(cart.getUserId(), userId);

        if (cart.getStatus() == CartStatusEnum.CONVERTED) {
            log.warn("Cart {} is already CONVERTED", cartId);
            return cartMapper.toResponse(cart);
        }

        if (cart.getStatus() != CartStatusEnum.CHECKOUT) {
            log.warn("Cart convert failed - cart {} status is {}", cartId, cart.getStatus());
            throw new CartServiceException(
                    "Cart can only be converted when status is CHECKOUT. Current status: " + cart.getStatus()
            );
        }

        cart.changeStatus(CartStatusEnum.CONVERTED);
        CartEntity saved = cartRepository.save(cart);
        log.info("Cart {} marked CONVERTED against order {}", cartId, cart.getOrderId());
        return cartMapper.toResponse(saved);
    }

    private OrderResponse placeOrder(CartEntity cart, CheckoutRequest request) {
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .shippingAddress(request.getShippingAddress())
                .items(toOrderItems(cart.getItems()))
                .cartId(cart.getId())
                .build();

        ApiResponse<OrderResponse> response = orderClient.createOrder(orderRequest);

        if (response == null || response.getData() == null) {
            log.error("Order service returned empty response for cart id: {}", cart.getId());
            throw new CartServiceException("Unable to place order, please try again");
        }

        log.info("Order {} created for cart id: {}", response.getData().getId(), cart.getId());
        return response.getData();
    }

    private List<OrderItemRequest> toOrderItems(List<CartItemEntity> cartItems) {
        return cartItems.stream()
                .map(cartItem -> OrderItemRequest.builder()
                        .productId(cartItem.getProductId())
                        .quantity(cartItem.getQuantity())
                        .build())
                .toList();
    }

    private CartEntity findOrCreateActiveCart(Long userId) {
        return findActiveCart(userId).orElseGet(() -> createActiveCart(userId));
    }

    private Optional<CartEntity> findActiveCart(Long userId) {
        return cartRepository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(userId, CartStatusEnum.ACTIVE);
    }

    private CartEntity createActiveCart(Long userId) {
        CartEntity cart = CartEntity.builder()
                .userId(userId)
                .currency(DEFAULT_CURRENCY)
                .build();
        cart.changeStatus(CartStatusEnum.ACTIVE);

        try {
            return cartRepository.saveAndFlush(cart);
        } catch (DataIntegrityViolationException ex) {
            log.info("Active cart was created concurrently for user id: {}, using existing one", userId);
            return findActiveCart(userId).orElseThrow(() -> ex);
        }
    }

    private CartEntity emptyCart(Long userId) {
        CartEntity cart = CartEntity.builder()
                .userId(userId)
                .currency(DEFAULT_CURRENCY)
                .build();
        cart.changeStatus(CartStatusEnum.ACTIVE);
        return cart;
    }

    private CartEntity findCartOrThrow(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId));
    }

    private void requireActiveCart(CartEntity cart) {
        if (cart.getStatus() != CartStatusEnum.ACTIVE) {
            throw new CartServiceException(
                    "Cart items can only be modified when status is ACTIVE. Current status: " + cart.getStatus()
            );
        }
    }

    private void assertOwnership(Long cartUserId, Long userId) {
        if (!Objects.equals(cartUserId, userId)) {
            log.warn("Access denied - cart belongs to user id: {}, requested by user id: {}", cartUserId, userId);
            throw new AccessDeniedException("You do not have access to this cart");
        }
    }

    private ProductResponse validateProductForQuantity(Long productId, int quantity) {
        ProductResponse product = fetchProduct(productId);
        validateProductAvailable(product);
        validateStock(product, quantity);
        return product;
    }

    private ProductResponse fetchProduct(Long productId) {
        log.debug("Validating product id: {} against product service", productId);
        ApiResponse<ProductResponse> response = productClient.getProductById(productId);
        if (response == null || response.getData() == null) {
            throw new ResourceNotFoundException("Product", productId);
        }
        return response.getData();
    }

    private void validateProductAvailable(ProductResponse product) {
        if (!Boolean.TRUE.equals(product.getActive())) {
            log.warn("Product validation failed - product not active: {}", product.getName());
            throw new CartServiceException("Product is not active: " + product.getName());
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            log.warn("Product validation failed - invalid price for product: {}", product.getName());
            throw new CartServiceException("Product has invalid price: " + product.getName());
        }
    }

    private void validateStock(ProductResponse product, int quantity) {
        Integer stock = product.getStockQuantity();
        if (stock != null && stock < quantity) {
            log.warn("Product validation failed - insufficient stock for: {}. Available: {}, Requested: {}",
                    product.getName(), stock, quantity);
            throw new CartServiceException(
                    "Insufficient stock for product: " + product.getName()
                            + ". Available: " + stock
                            + ", Requested: " + quantity
            );
        }
    }
}
