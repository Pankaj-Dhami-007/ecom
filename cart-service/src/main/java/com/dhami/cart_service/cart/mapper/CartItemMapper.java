package com.dhami.cart_service.cart.mapper;

import com.dhami.cart_service.cart.dto.CartItemResponse;
import com.dhami.cart_service.cart.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartItemMapper {

    CartItemResponse toResponse(CartItemEntity entity);
}