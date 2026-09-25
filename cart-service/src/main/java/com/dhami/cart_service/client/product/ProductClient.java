package com.dhami.cart_service.client.product;

import com.dhami.cart_service.client.product.dto.ProductResponse;
import com.dhami.cart_service.common.dto.ApiResponse;
import com.dhami.cart_service.constants.Routes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product-service.url}", path = Routes.API_V1 + Routes.Products.ROOT)
public interface ProductClient {

    @GetMapping(Routes.Products.ID)
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);
}
