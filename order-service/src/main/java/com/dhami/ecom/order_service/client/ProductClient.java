package com.dhami.ecom.order_service.client;

import com.dhami.ecom.order_service.constants.Routes;
import com.dhami.ecom.order_service.dto.request.StockUpdateRequest;
import com.dhami.ecom.order_service.dto.response.ApiResponse;
import com.dhami.ecom.order_service.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service", url = "${product-service.url}", path = Routes.API_V1 + Routes.Products.ROOT)
public interface ProductClient {

    @GetMapping(Routes.Products.ID)
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);

    @PutMapping(Routes.Products.ID_DEDUCT_STOCK)
    ApiResponse<ProductResponse> deductStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);

    @PutMapping(Routes.Products.ID_RESTORE_STOCK)
    ApiResponse<ProductResponse> restoreStock(@PathVariable("id") Long id, @RequestBody StockUpdateRequest request);
}
