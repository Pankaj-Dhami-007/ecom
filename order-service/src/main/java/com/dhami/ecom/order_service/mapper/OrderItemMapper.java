package com.dhami.ecom.order_service.mapper;

import com.dhami.ecom.order_service.dto.response.OrderItemResponse;
import com.dhami.ecom.order_service.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    OrderItemResponse toResponse(OrderItemEntity entity);
}
