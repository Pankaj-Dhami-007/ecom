package com.dhami.ecom.product_service.product;

import com.dhami.ecom.product_service.constants.Routes;
import com.dhami.ecom.product_service.common.dto.ApiResponse;
import com.dhami.ecom.product_service.common.dto.PageResponse;
import com.dhami.ecom.product_service.product.dto.ProductRequest;
import com.dhami.ecom.product_service.product.dto.ProductResponse;
import com.dhami.ecom.product_service.product.dto.StockUpdateRequest;
import com.dhami.ecom.product_service.product.service.ProductService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1 + Routes.Products.ROOT)
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        log.info("Creating product with name: {} and SKU: {}", request.name(), request.sku());
        ProductResponse response = productService.create(request);
        log.info("Product created successfully with id: {}", response.id());
        return new ResponseEntity<>(ApiResponse.success(response), HttpStatus.CREATED);
    }

    @GetMapping(Routes.Products.ID)
    public ResponseEntity<ApiResponse<ProductResponse>> findById(@PathVariable Long id) {
        log.info("Fetching product by id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(productService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> findAll(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        if (categoryId != null) {
            log.info("Fetching products by category id: {} - page: {}, size: {}", categoryId, pageable.getPageNumber(), pageable.getPageSize());
            return ResponseEntity.ok(ApiResponse.success(productService.findByCategory(categoryId, pageable)));
        }
        log.info("Fetching all products - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(ApiResponse.success(productService.findAll(pageable)));
    }

    @PutMapping(Routes.Products.ID)
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        log.info("Updating product with id: {}", id);
        return ResponseEntity.ok(ApiResponse.success(productService.update(id, request)));
    }

    @DeleteMapping(Routes.Products.ID)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("Deleting product with id: {}", id);
        productService.delete(id);
        log.info("Product deleted successfully with id: {}", id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(Routes.Products.ID_DEDUCT_STOCK)
    public ResponseEntity<ApiResponse<ProductResponse>> deductStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("Deduct stock request for product id: {}, quantity: {}", id, request.quantity());
        return ResponseEntity.ok(
                ApiResponse.success(productService.deductStock(id, request.quantity()))
        );
    }

    @PutMapping(Routes.Products.ID_RESTORE_STOCK)
    public ResponseEntity<ApiResponse<ProductResponse>> restoreStock(
            @PathVariable Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        log.info("Restore stock request for product id: {}, quantity: {}", id, request.quantity());
        return ResponseEntity.ok(
                ApiResponse.success(productService.restoreStock(id, request.quantity()))
        );
    }
}
