package com.dhami.orderservice.order.mapper;

import com.dhami.orderservice.order.dto.OrderItemResponse;
import com.dhami.orderservice.order.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItemEntity entity);
}
