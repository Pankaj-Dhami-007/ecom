package com.dhami.ecom.product_service.repository;

import com.dhami.ecom.product_service.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findBySlug(String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);
}
