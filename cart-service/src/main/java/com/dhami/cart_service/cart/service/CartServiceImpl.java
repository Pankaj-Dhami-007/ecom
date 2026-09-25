package com.dhami.cart_service.cart.service;

import com.dhami.cart_service.cart.CartServiceException;
import com.dhami.cart_service.cart.dto.AddToCartRequest;
import com.dhami.cart_service.cart.dto.CartItemRequest;
import com.dhami.cart_service.cart.dto.CartResponse;
import com.dhami.cart_service.cart.dto.UpdateCartItemRequest;
import com.dhami.cart_service.cart.entity.CartEntity;
import com.dhami.cart_service.cart.entity.CartItemEntity;
import com.dhami.cart_service.cart.enums.CartStatusEnum;
import com.dhami.cart_service.cart.mapper.CartMapper;
import com.dhami.cart_service.cart.repository.CartEntityRepository;
import com.dhami.cart_service.cart.repository.CartItemEntityRepository;
import com.dhami.cart_service.client.product.ProductClient;
import com.dhami.cart_service.client.product.dto.ProductResponse;
import com.dhami.cart_service.common.context.RequestUserContext;
import com.dhami.cart_service.common.dto.ApiResponse;
import com.dhami.cart_service.common.dto.PageResponse;
import com.dhami.cart_service.common.exception.AccessDeniedException;
import com.dhami.cart_service.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private static final String DEFAULT_CURRENCY = "INR";
    private static final String ACTIVE_CART_NOT_FOUND = "No active cart found for user: ";

    private final CartEntityRepository cartRepository;
    private final CartItemEntityRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final ProductClient productClient;

    @Override
    @Transactional
    public CartResponse createCart() {
        Long userId = RequestUserContext.requireUserId();
        log.info("Creating cart for user id: {}", userId);
        CartEntity cart = findOrCreateActiveCart(userId);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse getMyCart() {
        Long userId = RequestUserContext.requireUserId();
        log.info("Fetching active cart for user id: {}", userId);
        CartEntity cart = findOrCreateActiveCart(userId);
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
    public PageResponse<CartResponse> getAllCarts(Pageable pageable) {
        log.debug("Fetching all carts - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                cartRepository.findAll(pageable).map(cartMapper::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CartResponse> getMyCarts(Pageable pageable) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Fetching carts for user id: {} - page: {}, size: {}", userId, pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                cartRepository.findByUserId(userId, pageable).map(cartMapper::toResponse)
        );
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(AddToCartRequest request) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Adding {} item(s) to cart for user id: {}", request.items().size(), userId);

        CartEntity cart = findOrCreateActiveCart(userId);

        for (CartItemRequest itemRequest : request.items()) {
            ProductResponse product = validateProductForQuantity(itemRequest.productId(), itemRequest.quantity());

            CartItemEntity existing = cartItemRepository
                    .findByCartIdAndProductId(cart.getId(), itemRequest.productId())
                    .orElse(null);

            if (existing == null) {
                CartItemEntity newItem = CartItemEntity.builder()
                        .cart(cart)
                        .productId(product.getId())
                        .productName(product.getName())
                        .quantity(itemRequest.quantity())
                        .unitPrice(product.getPrice())
                        .build();
                cart.addItem(newItem);
            } else {
                existing.setQuantity(existing.getQuantity() + itemRequest.quantity());
                existing.setUnitPrice(product.getPrice());
                existing.setProductName(product.getName());
                cartItemRepository.save(existing);
            }
        }

        CartEntity saved = cartRepository.save(cart);
        log.info("Items added successfully to cart id: {} for user id: {}", saved.getId(), userId);
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

        CartEntity cart = cartRepository
                .findFirstByUserIdAndStatusOrderByUpdatedAtDesc(userId, CartStatusEnum.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException(ACTIVE_CART_NOT_FOUND + userId));

        cart.getItems().clear();
        CartEntity saved = cartRepository.save(cart);
        log.info("Cart cleared successfully for user id: {}", userId);
        return cartMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CartResponse checkoutCart(Long cartId) {
        Long userId = RequestUserContext.requireUserId();
        log.info("Marking cart id: {} for checkout for user id: {}", cartId, userId);

        CartEntity cart = findCartOrThrow(cartId);
        assertOwnership(cart.getUserId(), userId);

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

        revalidateStock(cart);

        cart.setStatus(CartStatusEnum.CHECKOUT);
        CartEntity saved = cartRepository.save(cart);
        log.info("Cart id: {} moved to checkout successfully for user id: {}", cartId, userId);
        return cartMapper.toResponse(saved);
    }

    private CartEntity findOrCreateActiveCart(Long userId) {
        return cartRepository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(userId, CartStatusEnum.ACTIVE)
                .orElseGet(() -> {
                    CartEntity cart = CartEntity.builder()
                            .userId(userId)
                            .status(CartStatusEnum.ACTIVE)
                            .currency(DEFAULT_CURRENCY)
                            .build();
                    return cartRepository.save(cart);
                });
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

    private void revalidateStock(CartEntity cart) {
        for (CartItemEntity item : cart.getItems()) {
            validateProductForQuantity(item.getProductId(), item.getQuantity());
        }
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
