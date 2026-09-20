package com.dhami.ecom.product_service.product;

import com.dhami.ecom.product_service.category.CategoryEntity;
import com.dhami.ecom.product_service.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    @DecimalMin("0.00")
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal comparePrice; // original/MRP

    @Column(nullable = false, unique = true)
    private String sku;

    @Builder.Default
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    private String imageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
}

// slug -> A slug is a unique, URL-friendly text used to identify a page or record in a readable way

/*
sku
SKU (Stock Keeping Unit) is a unique code used to identify and track a product in inventory.
Think of it as a product's internal ID for business operations.

example ->
Product : iPhone 16 Pro 256GB Black
SKU: IPH16P-256-BLK

Another product: iPhone 16 Pro 512GB Black
SKU : IPH16P-512-BLK
conc -> Even though both are iPhones, they have different SKUs because they are different variants.

Why use SKU?
1. Inventory Management
Instead of searching by product name: iPhone 16 Pro 256GB Black
you can search -> IPH16P-256-BLK
Much faster and unique.

2. Avoid Duplicate Products

3. Used in Billing & Warehouses
Invoices, stock reports, warehouses, barcode systems often use SKU instead of product names.
ex :
SKU              Stock
IPH16P-256-BLK   25
SMG-S25-128-WHT  12

Difference Between ID and SKU

| ID                   | SKU                         |
| -------------------- | --------------------------- |
| Database primary key | Business product code       |
| Auto-generated       | Usually created by business |
| Users don't see it   | Staff often use it          |
| Example: 101         | Example: IPH16P-256-BLK     |


SKU (Stock Keeping Unit) is a unique product code used to identify and manage products in inventory.
 */
