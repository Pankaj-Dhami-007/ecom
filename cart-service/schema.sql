CREATE DATABASE IF NOT EXISTS ecom_cart_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ecom_cart_db;

CREATE TABLE IF NOT EXISTS cart
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    currency      VARCHAR(3)  NOT NULL,
    active_marker TINYINT     NULL,
    order_id      BIGINT      NULL,
    version       BIGINT      NOT NULL DEFAULT 0,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_user_active (user_id, active_marker),
    KEY idx_cart_user_id_status (user_id, status),
    KEY idx_cart_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cart_item
(
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    cart_id      BIGINT        NOT NULL,
    product_id   BIGINT        NOT NULL,
    product_name VARCHAR(255)  NOT NULL,
    quantity     INT           NOT NULL,
    unit_price   DECIMAL(19,2) NOT NULL,
    version      BIGINT        NOT NULL DEFAULT 0,
    created_at   DATETIME(6)   NOT NULL,
    updated_at   DATETIME(6)   NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_product (cart_id, product_id),
    KEY idx_cart_item_cart_id (cart_id),
    CONSTRAINT fk_cart_item_cart
        FOREIGN KEY (cart_id) REFERENCES cart (id)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;