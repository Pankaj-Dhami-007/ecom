package com.dhami.catalogservice.category.service;

import com.dhami.catalogservice.category.dto.CategoryRequest;
import com.dhami.catalogservice.category.dto.CategoryResponse;
import com.dhami.catalogservice.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse create(CategoryRequest request);

    CategoryResponse findById(Long id);

    PageResponse<CategoryResponse> findAll(Pageable pageable);

    CategoryResponse update(Long id, CategoryRequest request);

    void delete(Long id);
}
