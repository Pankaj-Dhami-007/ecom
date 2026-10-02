CREATE DATABASE IF NOT EXISTS ecom_payment_db;

USE ecom_payment_db;

CREATE TABLE IF NOT EXISTS payments (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    payment_reference VARCHAR(40)   NOT NULL,
    order_id          BIGINT        NOT NULL,
    cart_id           BIGINT,
    customer_id       BIGINT        NOT NULL,
    amount            DECIMAL(10,2) NOT NULL,
    currency          VARCHAR(3)    NOT NULL DEFAULT 'INR',
    status            VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    gateway           VARCHAR(30)   NOT NULL,
    gateway_reference VARCHAR(80),
    failure_reason    VARCHAR(255),
    created_at        DATETIME(6)   NOT NULL,
    updated_at        DATETIME(6)   NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_payments_reference (payment_reference),
    KEY idx_payments_order_id (order_id),
    KEY idx_payments_customer_id (customer_id),
    KEY idx_payments_cart_id (cart_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
