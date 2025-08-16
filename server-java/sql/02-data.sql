-- Seed initial data for job_finder
-- Run after 01-schema.sql

USE `job_finder`;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `resume_history`;
TRUNCATE TABLE `subscriber_skills`;
TRUNCATE TABLE `job_skills`;
TRUNCATE TABLE `role_permissions`;
TRUNCATE TABLE `resumes`;
TRUNCATE TABLE `subscribers`;
TRUNCATE TABLE `jobs`;
TRUNCATE TABLE `users`;
TRUNCATE TABLE `permissions`;
TRUNCATE TABLE `roles`;
TRUNCATE TABLE `companies`;
SET FOREIGN_KEY_CHECKS = 1;

-- Companies
INSERT INTO `companies` (`id`, `name`, `description`, `logo`, `address`, `created_at`, `updated_at`)
VALUES
('11111111-1111-1111-1111-111111111111', 'Acme Corp', 'Leading tech company', NULL, '123 Main St, HCMC', NOW(6), NOW(6));

-- Roles
INSERT INTO `roles` (`id`, `name`, `description`, `is_active`, `created_at`, `updated_at`)
VALUES
('22222222-2222-2222-2222-222222222222', 'ADMIN', 'Administrator role', b'1', NOW(6), NOW(6));

-- Permissions
INSERT INTO `permissions` (`id`, `name`, `api_path`, `method`, `module`, `created_at`, `updated_at`)
VALUES
('33333333-3333-3333-3333-333333333331', 'USERS', '/api/v1/users', 'GET', 'users', NOW(6), NOW(6)),
('33333333-3333-3333-3333-333333333332', 'JOBS', '/api/v1/jobs', 'POST', 'jobs', NOW(6), NOW(6)),
('33333333-3333-3333-3333-333333333333', 'COMPANIES', '/api/v1/companies', 'POST', 'companies', NOW(6), NOW(6));

-- Role-Permissions mapping
INSERT INTO `role_permissions` (`role_id`, `permission_id`) VALUES
('22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333331'),
('22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333332'),
('22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333');

-- Users (password is BCrypt-hashed: admin123)
INSERT INTO `users` (`id`, `email`, `password`, `address`, `name`, `age`, `gender`, `refresh_token`, `company_id`, `role_id`, `created_at`, `updated_at`)
VALUES
('44444444-4444-4444-4444-444444444444', 'admin@example.com', '$2b$12$5nUEqoniCfaI1jpBT0FkwejaWqbEvZEnIyJeyqg1OmhOLxgSpI6SS', '123 Main St, HCMC', 'Admin User', 30, 'male', NULL, '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', NOW(6), NOW(6));

-- Jobs
INSERT INTO `jobs` (`id`, `location`, `name`, `salary`, `quantity`, `level`, `description`, `start_date`, `end_date`, `is_active`, `company_id`, `created_at`, `updated_at`)
VALUES
('55555555-5555-5555-5555-555555555555', 'HCMC', 'Senior Java Developer', 2000, 2, 'Senior', 'Backend position', '2025-01-01 00:00:00.000000', '2025-12-31 23:59:59.000000', b'1', '11111111-1111-1111-1111-111111111111', NOW(6), NOW(6));

-- Job skills
INSERT INTO `job_skills` (`job_id`, `skill`) VALUES
('55555555-5555-5555-5555-555555555555', 'Java'),
('55555555-5555-5555-5555-555555555555', 'Spring Boot');

-- Subscribers
INSERT INTO `subscribers` (`id`, `name`, `email`, `created_at`, `updated_at`) VALUES
('66666666-6666-6666-6666-666666666666', 'John Doe', 'john@example.com', NOW(6), NOW(6));

-- Subscriber skills
INSERT INTO `subscriber_skills` (`subscriber_id`, `skill`) VALUES
('66666666-6666-6666-6666-666666666666', 'Java'),
('66666666-6666-6666-6666-666666666666', 'React');

-- Resumes
INSERT INTO `resumes` (`id`, `email`, `user_id`, `url`, `status`, `company_id`, `job_id`, `created_at`, `updated_at`) VALUES
('77777777-7777-7777-7777-777777777777', 'candidate@example.com', '44444444-4444-4444-4444-444444444444', 'https://example.com/resume.pdf', 'PENDING', '11111111-1111-1111-1111-111111111111', '55555555-5555-5555-5555-555555555555', NOW(6), NOW(6));

-- Resume history
INSERT INTO `resume_history` (`resume_id`, `status`, `updated_at`, `updated_by_id`, `updated_by_email`) VALUES
('77777777-7777-7777-7777-777777777777', 'PENDING', '2025-01-01 00:00:00.000000', NULL, NULL);


