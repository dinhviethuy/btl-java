-- Seed admin super role, permissions, and a default super admin user
USE `job_finder`;

SET FOREIGN_KEY_CHECKS = 0;

-- Create SUPER_ADMIN role
INSERT INTO `roles` (`id`, `name`, `description`, `is_active`, `created_at`, `updated_at`)
VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'SUPER_ADMIN', 'Super admin with full permissions', b'1', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Create permissions for common modules (method * means all)
INSERT INTO `permissions` (`id`, `name`, `api_path`, `method`, `module`, `created_at`, `updated_at`) VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01', 'FULL_USERS', '/api/v1/users', '*', 'users', NOW(6), NOW(6)),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02', 'FULL_COMPANIES', '/api/v1/companies', '*', 'companies', NOW(6), NOW(6)),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03', 'FULL_JOBS', '/api/v1/jobs', '*', 'jobs', NOW(6), NOW(6)),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb04', 'FULL_PERMISSIONS', '/api/v1/permissions', '*', 'permissions', NOW(6), NOW(6)),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb05', 'FULL_ROLES', '/api/v1/roles', '*', 'roles', NOW(6), NOW(6)),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb06', 'FULL_RESUMES', '/api/v1/resumes', '*', 'resumes', NOW(6), NOW(6)),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb07', 'FULL_FILES', '/api/v1/files', '*', 'files', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Map all permissions to SUPER_ADMIN role
INSERT IGNORE INTO `role_permissions` (`role_id`, `permission_id`) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb01'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb02'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb03'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb04'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb05'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb06'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbb07');

-- Create a default company to attach admin (optional)
INSERT INTO `companies` (`id`, `name`, `description`, `address`, `created_at`, `updated_at`)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'System Company', 'System default company', 'VN', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Create super admin user with bcrypt password 'admin123'
INSERT INTO `users` (`id`, `email`, `password`, `name`, `age`, `gender`, `address`, `company_id`, `role_id`, `created_at`, `updated_at`)
VALUES ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'superadmin@example.com', '$2b$12$5nUEqoniCfaI1jpBT0FkwejaWqbEvZEnIyJeyqg1OmhOLxgSpI6SS', 'Super Admin', 30, 'male', 'HN', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', NOW(6), NOW(6))
ON DUPLICATE KEY UPDATE email = VALUES(email), role_id = VALUES(role_id);

SET FOREIGN_KEY_CHECKS = 1;

-- Note: Importing companies.json/jobs.json programmatically is recommended (via Java runner) due to JSON shape (OID/date fields).

