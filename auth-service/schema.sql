
CREATE DATABASE IF NOT EXISTS ecom_auth_db
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ecom_auth_db;


CREATE TABLE IF NOT EXISTS roles
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50),
    email          VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    phone_number   VARCHAR(20),
    status         VARCHAR(20)  NOT NULL DEFAULT 'INACTIVE',
    email_verified BIT(1)       NOT NULL DEFAULT b'0',
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,

    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role_id (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id              BINARY(16)   NOT NULL,
    jti             VARCHAR(255) NOT NULL,
    user_id         BIGINT       NOT NULL,
    created_at      DATETIME(6)  NOT NULL,
    expires_at      DATETIME(6)  NOT NULL,
    revoked         BIT(1)       NOT NULL DEFAULT b'0',
    replaced_by_jti VARCHAR(255),

    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_jti (jti),
    KEY idx_refresh_tokens_user_id (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


CREATE TABLE IF NOT EXISTS password_reset_tokens
(
    id         BINARY(16)   NOT NULL,
    token      VARCHAR(255) NOT NULL,
    user_id    BIGINT       NOT NULL,
    created_at DATETIME(6)  NOT NULL,
    expires_at DATETIME(6)  NOT NULL,
    used       BIT(1)       NOT NULL DEFAULT b'0',

    PRIMARY KEY (id),
    UNIQUE KEY uk_password_reset_tokens_token (token),
    KEY idx_password_reset_tokens_user_id (user_id),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;


INSERT IGNORE INTO roles (name, description)
VALUES ('ROLE_CUSTOMER', 'Shopper. The only role self-service registration can grant.'),
       ('ROLE_ADMIN', 'Full access across every service.'),
       ('ROLE_SELLER', 'Vendor who lists products and manages their own orders.');

-- Sanity check: should print 6.
SELECT name FROM roles ORDER BY name;
