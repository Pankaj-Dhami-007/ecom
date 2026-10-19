CREATE DATABASE IF NOT EXISTS ecom_product_db;

USE ecom_catalog_db;

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL,
    description TEXT,
    active      BIT          NOT NULL DEFAULT 1,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_categories_name (name),
    UNIQUE KEY uk_categories_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS products (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    name          VARCHAR(255)  NOT NULL,
    slug          VARCHAR(255)  NOT NULL,
    description   TEXT,
    price         DECIMAL(10,2) NOT NULL,
    compare_price DECIMAL(10,2),
    sku           VARCHAR(255)  NOT NULL,
    stock_quantity INT          NOT NULL DEFAULT 0,
    reserved_quantity INT       NOT NULL DEFAULT 0,
    image_url     VARCHAR(255),
    active        BIT           NOT NULL DEFAULT 1,
    category_id   BIGINT        NOT NULL,
    created_at    DATETIME(6)   NOT NULL,
    updated_at    DATETIME(6)   NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_products_slug (slug),
    UNIQUE KEY uk_products_sku (sku),
    KEY idx_products_category (category_id),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES categories (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
