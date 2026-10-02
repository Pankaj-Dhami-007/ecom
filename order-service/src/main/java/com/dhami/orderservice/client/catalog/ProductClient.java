package com.dhami.orderservice.client.catalog;

import com.dhami.orderservice.constants.Routes;
import com.dhami.orderservice.client.catalog.dto.StockUpdateRequest;
import com.dhami.orderservice.common.dto.ApiResponse;
import com.dhami.orderservice.client.catalog.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "catalog-service", url = "${catalog-service.url}", path = Routes.API_V1 + Routes.Products.ROOT)
public interface ProductClient {

    @GetMapping(Routes.Products.ID)
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);

    @PutMapping(Routes.Products.ID_DEDUCT_STOCK)
    ApiResponse<ProductResponse> deductStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);

    @PutMapping(Routes.Products.ID_RESTORE_STOCK)
    ApiResponse<ProductResponse> restoreStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);

    @PutMapping(Routes.Products.ID_RESERVE_STOCK)
    ApiResponse<ProductResponse> reserveStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);

    @PutMapping(Routes.Products.ID_CONFIRM_RESERVATION)
    ApiResponse<ProductResponse> confirmReservation(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);

    @PutMapping(Routes.Products.ID_RELEASE_RESERVATION)
    ApiResponse<ProductResponse> releaseReservation(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);
}
