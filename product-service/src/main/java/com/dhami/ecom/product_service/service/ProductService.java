package com.dhami.ecom.product_service.service;

import com.dhami.ecom.product_service.dto.PageResponse;
import com.dhami.ecom.product_service.dto.ProductRequest;
import com.dhami.ecom.product_service.dto.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse findById(Long id);

    PageResponse<ProductResponse> findAll(Pageable pageable);

    PageResponse<ProductResponse> findByCategory(Long categoryId, Pageable pageable);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    ProductResponse deductStock(Long id, Integer quantity);

    ProductResponse restoreStock(Long id, Integer quantity);
}
