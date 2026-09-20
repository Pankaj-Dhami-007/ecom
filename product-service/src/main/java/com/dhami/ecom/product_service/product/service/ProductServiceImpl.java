package com.dhami.ecom.product_service.product.service;

import com.dhami.ecom.product_service.category.dto.CategoryResponse;
import com.dhami.ecom.product_service.common.dto.PageResponse;
import com.dhami.ecom.product_service.product.dto.ProductRequest;
import com.dhami.ecom.product_service.product.dto.ProductResponse;
import com.dhami.ecom.product_service.category.CategoryEntity;
import com.dhami.ecom.product_service.product.ProductEntity;
import com.dhami.ecom.product_service.common.exception.DuplicateResourceException;
import com.dhami.ecom.product_service.common.exception.InsufficientStockException;
import com.dhami.ecom.product_service.common.exception.ResourceNotFoundException;
import com.dhami.ecom.product_service.category.repository.CategoryRepository;
import com.dhami.ecom.product_service.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Creating product with name: {} and SKU: {}", request.name(), request.sku());

        if (productRepository.existsBySku(request.sku())) {
            log.warn("Product creation failed - SKU already exists: {}", request.sku());
            throw new DuplicateResourceException("Product already exists with SKU: " + request.sku());
        }

        CategoryEntity category = findCategoryOrThrow(request.categoryId());
        String slug = toSlug(request.name());

        ProductEntity entity = ProductEntity.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .price(request.price())
                .comparePrice(request.comparePrice())
                .sku(request.sku())
                .stockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0)
                .imageUrl(request.imageUrl())
                .active(request.active() != null ? request.active() : true)
                .category(category)
                .build();

        ProductResponse response = toResponse(productRepository.save(entity));
        log.info("Product created successfully with id: {}", response.id());
        return response;
    }

    @Override
    public ProductResponse findById(Long id) {
        log.info("Fetching product by id: {}", id);
        return toResponse(findProductOrThrow(id));
    }

    @Override
    public PageResponse<ProductResponse> findAll(Pageable pageable) {
        log.debug("Fetching all products - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                productRepository.findAll(pageable).map(this::toResponse)
        );
    }

    @Override
    public PageResponse<ProductResponse> findByCategory(Long categoryId, Pageable pageable) {
        log.info("Fetching products by category id: {} - page: {}, size: {}", categoryId, pageable.getPageNumber(), pageable.getPageSize());
        findCategoryOrThrow(categoryId);
        return PageResponse.from(
                productRepository.findByCategoryId(categoryId, pageable).map(this::toResponse)
        );
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Updating product with id: {}", id);
        ProductEntity entity = findProductOrThrow(id);

        if (!entity.getSku().equals(request.sku())
                && productRepository.existsBySku(request.sku())) {
            log.warn("Product update failed - SKU already exists: {}", request.sku());
            throw new DuplicateResourceException("Product already exists with SKU: " + request.sku());
        }

        CategoryEntity category = findCategoryOrThrow(request.categoryId());

        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setPrice(request.price());
        entity.setComparePrice(request.comparePrice());
        entity.setSku(request.sku());
        entity.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0);
        entity.setImageUrl(request.imageUrl());
        entity.setCategory(category);
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        ProductResponse response = toResponse(productRepository.save(entity));
        log.info("Product updated successfully with id: {}", id);
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting product with id: {}", id);
        ProductEntity entity = findProductOrThrow(id);
        productRepository.delete(entity);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public ProductResponse deductStock(Long id, Integer quantity) {
        log.info("Deducting {} from stock for product id: {}", quantity, id);
        ProductEntity entity = productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        int currentStock = entity.getStockQuantity();
        if (currentStock < quantity) {
            log.warn("Stock deduction failed - insufficient stock for product {}. Available: {}, Requested: {}",
                    id, currentStock, quantity);
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + entity.getName()
                            + ". Available: " + currentStock + ", Requested: " + quantity
            );
        }

        entity.setStockQuantity(currentStock - quantity);
        ProductEntity saved = productRepository.save(entity);
        log.info("Stock deducted successfully for product id: {}. Remaining: {}", id, saved.getStockQuantity());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse restoreStock(Long id, Integer quantity) {
        log.info("Restoring {} to stock for product id: {}", quantity, id);
        ProductEntity entity = findProductOrThrow(id);

        entity.setStockQuantity(entity.getStockQuantity() + quantity);
        ProductEntity saved = productRepository.save(entity);
        log.info("Stock restored successfully for product id: {}. Current: {}", id, saved.getStockQuantity());
        return toResponse(saved);
    }

    private ProductEntity findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private CategoryEntity findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private CategoryResponse toCategoryResponse(CategoryEntity entity) {
        return new CategoryResponse(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ProductResponse toResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getComparePrice(),
                entity.getSku(),
                entity.getStockQuantity(),
                entity.getImageUrl(),
                entity.getActive(),
                toCategoryResponse(entity.getCategory()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized)
                .replaceAll("")
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }
}
