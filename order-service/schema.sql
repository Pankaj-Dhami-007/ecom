CREATE DATABASE IF NOT EXISTS ecom_order_db;

USE ecom_order_db;

CREATE TABLE IF NOT EXISTS shipping_address (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    full_name    VARCHAR(100) NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    state        VARCHAR(100) NOT NULL,
    pincode      VARCHAR(20)  NOT NULL,
    created_at   DATETIME(6)  NOT NULL,
    updated_at   DATETIME(6)  NOT NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS orders (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    order_number        VARCHAR(255)  NOT NULL,
    customer_id         BIGINT        NOT NULL,
    cart_id             BIGINT,
    total_amount        DECIMAL(10,2) NOT NULL,
    status              VARCHAR(20)   NOT NULL DEFAULT 'PENDING_PAYMENT',
    shipping_address_id BIGINT        NOT NULL,
    created_at          DATETIME(6)   NOT NULL,
    updated_at          DATETIME(6)   NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_orders_order_number (order_number),
    KEY idx_orders_customer_id (customer_id),
    CONSTRAINT fk_orders_shipping_address
        FOREIGN KEY (shipping_address_id) REFERENCES shipping_address (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_items (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    quantity     INT           NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    subtotal     DECIMAL(10,2) NOT NULL,
    order_id     BIGINT        NOT NULL,

    PRIMARY KEY (id),
    KEY idx_order_items_order_id (order_id),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
