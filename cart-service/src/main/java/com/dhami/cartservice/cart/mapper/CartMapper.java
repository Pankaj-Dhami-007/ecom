package com.dhami.cartservice.cart.mapper;

import com.dhami.cartservice.cart.dto.CartResponse;
import com.dhami.cartservice.cart.entity.CartEntity;
import com.dhami.cartservice.cart.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = CartItemMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CartMapper {

    @Mapping(target = "itemCount", expression = "java(computeItemCount(cart.getItems()))")
    @Mapping(target = "totalAmount", expression = "java(computeTotalAmount(cart.getItems()))")
    CartResponse toResponse(CartEntity cart);

    default Integer computeItemCount(List<CartItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream().mapToInt(CartItemEntity::getQuantity).sum();
    }

    default BigDecimal computeTotalAmount(List<CartItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .filter(item -> item.getUnitPrice() != null)
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}