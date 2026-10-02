package com.dhami.cartservice.cart.mapper;

import com.dhami.cartservice.cart.dto.CartItemResponse;
import com.dhami.cartservice.cart.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartItemMapper {

    CartItemResponse toResponse(CartItemEntity entity);
}