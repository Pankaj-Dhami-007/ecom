package com.dhami.ecom.order_service.order.mapper;

import com.dhami.ecom.order_service.order.dto.OrderResponse;
import com.dhami.ecom.order_service.order.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = OrderItemMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface OrderMapper {

    // Converts OrderEntity into OrderResponse DTO
    OrderResponse toResponse(OrderEntity entity);
}


/*
 * MAPSTRUCT NOTES
 * ------------------------------------------------------------------
 * MapStruct is a code generator used to convert one object into another.
 *
 * Common Use Case:
 * Entity  <----> DTO
 *
 * Example:
 * OrderEntity  -> OrderResponse
 * ProductEntity -> ProductResponse
 *
 * Why use MapStruct?
 *
 * Without MapStruct:
 *
 * OrderResponse response = new OrderResponse();
 * response.setId(entity.getId());
 * response.setOrderNumber(entity.getOrderNumber());
 * response.setCustomerId(entity.getCustomerId());
 * ...
 *
 * We have to write this mapping code manually everywhere.
 *
 * With MapStruct:
 *
 * OrderResponse toResponse(OrderEntity entity);
 *
 * MapStruct automatically generates the implementation class
 * during compilation.
 *
 * Generated class example:
 *
 * OrderMapperImpl implements OrderMapper
 *
 * It copies matching fields automatically.
 *
 * ------------------------------------------------------------------
 * componentModel = "spring"
 *
 * Generates mapper as Spring Bean.
 *
 * So we can inject it:
 *
 * @RequiredArgsConstructor
 * public class OrderServiceImpl {
 *     private final OrderMapper orderMapper;
 * }
 *
 * ------------------------------------------------------------------
 * uses = OrderItemMapper.class
 *
 * Used for nested object/list mapping.
 *
 * Example:
 *
 * OrderEntity
 *     List<OrderItemEntity>
 *
 * OrderResponse
 *     List<OrderItemResponse>
 *
 * MapStruct will delegate item conversion to OrderItemMapper.
 *
 * ------------------------------------------------------------------
 * unmappedTargetPolicy = ReportingPolicy.IGNORE
 *
 * Ignore fields that exist in target DTO but are not mapped.
 *
 * Without IGNORE:
 *
 * MapStruct may show warnings like:
 *
 * Unmapped target property: createdAt
 *
 * With IGNORE:
 *
 * Such warnings are ignored.
 *
 * ------------------------------------------------------------------
 * @Mapping is required only when field names differ.
 *
 * Example:
 *
 * Entity:
 *      orderNumber
 *
 * DTO:
 *      orderCode
 *
 * Then:
 *
 * @Mapping(source = "orderNumber", target = "orderCode")
 *
 * If names are same:
 *
 * id -> id
 * customerId -> customerId
 * status -> status
 *
 * No @Mapping needed.
 *
 */
