
--  Dev credentials
--  ---------------------------------------------------------------------------
--      email    : admin@ecom.com
--      password : Admin@123
--
--  The hash below is a real BCrypt strength-10 digest of "Admin@123"
--  (generated with the same BCryptPasswordEncoder the service uses, and
--  verified with matches()). CHANGE THE PASSWORD before this reaches any
--  shared environment.
-- ============================================================================

USE ecom_auth_db;

-- 1. The user. INSERT IGNORE on the unique email makes a re-run a no-op.
INSERT IGNORE INTO users (email, password, status, email_verified)
VALUES ('admin@ecom.com',
        '$2a$10$jvg036ALHRmcWebGy654TuWvZORMD7buFhGWuW1Lba8/AoOIF5ZJu',
        'ACTIVE', b'1');

-- 2. Grant ROLE_ADMIN. Resolved by name so the script does not hardcode an id.
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ROLE_ADMIN'
WHERE u.email = 'admin@ecom.com';

-- Sanity check: should print admin@ecom.com | ROLE_ADMIN.
SELECT u.email, GROUP_CONCAT(r.name ORDER BY r.name) AS roles
FROM users u
         JOIN user_roles ur ON ur.user_id = u.id
         JOIN roles r ON r.id = ur.role_id
WHERE u.email = 'admin@ecom.com'
GROUP BY u.email;
