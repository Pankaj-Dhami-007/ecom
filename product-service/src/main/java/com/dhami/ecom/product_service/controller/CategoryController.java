package com.dhami.ecom.product_service.controller;

import com.dhami.ecom.product_service.constants.Routes;
import com.dhami.ecom.product_service.dto.ApiResponse;
import com.dhami.ecom.product_service.dto.CategoryRequest;
import com.dhami.ecom.product_service.dto.CategoryResponse;
import com.dhami.ecom.product_service.dto.PageResponse;
import com.dhami.ecom.product_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1 + Routes.Categories.ROOT)
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        log.info("Creating category with name: {}", request.name());
        CategoryResponse response = categoryService.create(request);
        log.info("Category created successfully with id: {}", response.id());
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    @GetMapping(Routes.Categories.ID)
    public ResponseEntity<ApiResponse<CategoryResponse>> findById(@PathVariable Long id) {
        log.info("Fetching category by id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(categoryService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> findAll(
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Fetching all categories - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(ApiResponse.success(categoryService.findAll(pageable)));
    }

    @PutMapping(Routes.Categories.ID)
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Updating category with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(categoryService.update(id, request)));
    }

    @DeleteMapping(Routes.Categories.ID)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting category with id: {}", id);
        categoryService.delete(id);
        log.info("Category deleted successfully with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
