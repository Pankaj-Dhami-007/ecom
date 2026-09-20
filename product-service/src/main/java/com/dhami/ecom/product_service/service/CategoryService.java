package com.dhami.ecom.product_service.service;

import com.dhami.ecom.product_service.dto.CategoryRequest;
import com.dhami.ecom.product_service.dto.CategoryResponse;
import com.dhami.ecom.product_service.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse findById(Long id);

    PageResponse<CategoryResponse> findAll(Pageable pageable);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
