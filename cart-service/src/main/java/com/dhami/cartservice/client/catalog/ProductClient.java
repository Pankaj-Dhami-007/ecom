package com.dhami.cartservice.client.catalog;

import com.dhami.cartservice.client.catalog.dto.ProductResponse;
import com.dhami.cartservice.common.dto.ApiResponse;
import com.dhami.cartservice.constants.Routes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${catalog-service.url}", path = Routes.API_V1 + Routes.Products.ROOT)
public interface ProductClient {

    @GetMapping(Routes.Products.ID)
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);
}
