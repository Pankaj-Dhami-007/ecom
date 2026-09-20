package com.dhami.ecom.product_service.service.impl;

import com.dhami.ecom.product_service.dto.CategoryRequest;
import com.dhami.ecom.product_service.dto.CategoryResponse;
import com.dhami.ecom.product_service.dto.PageResponse;
import com.dhami.ecom.product_service.entity.CategoryEntity;
import com.dhami.ecom.product_service.exception.DuplicateResourceException;
import com.dhami.ecom.product_service.exception.ResourceNotFoundException;
import com.dhami.ecom.product_service.repository.CategoryRepository;
import com.dhami.ecom.product_service.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        log.info("Creating category with name: {}", request.name());

        if (categoryRepository.existsByName(request.name())) {
            log.warn("Category creation failed - name already exists: {}", request.name());
            throw new DuplicateResourceException("Category already exists with name: " + request.name());
        }

        String slug = toSlug(request.name());
        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        CategoryEntity entity = CategoryEntity.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .active(request.active() != null ? request.active() : true)
                .build();

        CategoryResponse response = toResponse(categoryRepository.save(entity));
        log.info("Category created successfully with id: {}", response.id());
        return response;
    }

    @Override
    public CategoryResponse findById(Long id) {
        log.info("Fetching category by id: {}", id);
        return toResponse(findCategoryOrThrow(id));
    }

    @Override
    public PageResponse<CategoryResponse> findAll(Pageable pageable) {
        log.debug("Fetching categories - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return PageResponse.from(
                categoryRepository.findAll(pageable).map(this::toResponse)
        );
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        log.info("Updating category with id: {}", id);
        CategoryEntity entity = findCategoryOrThrow(id);

        if (!entity.getName().equals(request.name())
                && categoryRepository.existsByName(request.name())) {
            log.warn("Category update failed - name already exists: {}", request.name());
            throw new DuplicateResourceException("Category already exists with name: " + request.name());
        }

        String slug = entity.getSlug();
        if (!entity.getName().equals(request.name())) {
            slug = toSlug(request.name());
            if (categoryRepository.existsBySlug(slug)) {
                slug = slug + "-" + System.currentTimeMillis();
            }
        }

        entity.setName(request.name());
        entity.setSlug(slug);
        entity.setDescription(request.description());
        if (request.active() != null) {
            entity.setActive(request.active());
        }

        CategoryResponse response = toResponse(categoryRepository.save(entity));
        log.info("Category updated successfully with id: {}", id);
        return response;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting category with id: {}", id);
        CategoryEntity entity = findCategoryOrThrow(id);
        categoryRepository.delete(entity);
        log.info("Category deleted successfully with id: {}", id);
    }

    private CategoryEntity findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private CategoryResponse toResponse(CategoryEntity entity) {
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
