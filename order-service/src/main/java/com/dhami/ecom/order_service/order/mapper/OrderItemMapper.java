package com.dhami.ecom.order_service.order.mapper;

import com.dhami.ecom.order_service.order.dto.OrderItemResponse;
import com.dhami.ecom.order_service.order.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItemEntity entity);
}
