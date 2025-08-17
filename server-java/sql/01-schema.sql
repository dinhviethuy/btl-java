-- Schema for job_finder matching JPA mappings
-- Run after 00-create-database.sql

USE `job_finder`;

-- Disable FK checks to allow dropping in any order
SET FOREIGN_KEY_CHECKS = 0;

-- Drop tables if they exist in correct order (children first)
DROP TABLE IF EXISTS `resume_history`;
DROP TABLE IF EXISTS `subscriber_skills`;
DROP TABLE IF EXISTS `job_skills`;
DROP TABLE IF EXISTS `role_permissions`;
DROP TABLE IF EXISTS `resumes`;
DROP TABLE IF EXISTS `subscribers`;
DROP TABLE IF EXISTS `jobs`;
DROP TABLE IF EXISTS `permissions`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `companies`;

-- Re-enable FK checks after schema recreation at the end

-- companies
CREATE TABLE `companies` (
  `id` varchar(36) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `logo` varchar(512) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_by_id` varchar(255) DEFAULT NULL,
  `created_by_email` varchar(255) DEFAULT NULL,
  `updated_by_id` varchar(255) DEFAULT NULL,
  `updated_by_email` varchar(255) DEFAULT NULL,
  `deleted_by_id` varchar(255) DEFAULT NULL,
  `deleted_by_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Turn FK checks back on
SET FOREIGN_KEY_CHECKS = 1;

-- roles
CREATE TABLE `roles` (
  `id` varchar(36) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `is_active` bit(1) DEFAULT b'1',
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_by_id` varchar(255) DEFAULT NULL,
  `created_by_email` varchar(255) DEFAULT NULL,
  `updated_by_id` varchar(255) DEFAULT NULL,
  `updated_by_email` varchar(255) DEFAULT NULL,
  `deleted_by_id` varchar(255) DEFAULT NULL,
  `deleted_by_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- permissions
CREATE TABLE `permissions` (
  `id` varchar(36) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `api_path` varchar(255) DEFAULT NULL,
  `method` varchar(16) DEFAULT NULL,
  `module` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_by_id` varchar(255) DEFAULT NULL,
  `created_by_email` varchar(255) DEFAULT NULL,
  `updated_by_id` varchar(255) DEFAULT NULL,
  `updated_by_email` varchar(255) DEFAULT NULL,
  `deleted_by_id` varchar(255) DEFAULT NULL,
  `deleted_by_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- users
CREATE TABLE `users` (
  `id` varchar(36) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `gender` varchar(50) DEFAULT NULL,
  `refresh_token` TEXT DEFAULT NULL,
  `company_id` varchar(36) DEFAULT NULL,
  `role_id` varchar(36) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_by_id` varchar(255) DEFAULT NULL,
  `created_by_email` varchar(255) DEFAULT NULL,
  `updated_by_id` varchar(255) DEFAULT NULL,
  `updated_by_email` varchar(255) DEFAULT NULL,
  `deleted_by_id` varchar(255) DEFAULT NULL,
  `deleted_by_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_users_company_id` (`company_id`),
  KEY `idx_users_role_id` (`role_id`),
  CONSTRAINT `fk_users_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- jobs
CREATE TABLE `jobs` (
  `id` varchar(36) NOT NULL,
  `location` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `salary` double DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `level` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `is_active` bit(1) DEFAULT b'1',
  `company_id` varchar(36) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_by_id` varchar(255) DEFAULT NULL,
  `created_by_email` varchar(255) DEFAULT NULL,
  `updated_by_id` varchar(255) DEFAULT NULL,
  `updated_by_email` varchar(255) DEFAULT NULL,
  `deleted_by_id` varchar(255) DEFAULT NULL,
  `deleted_by_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_jobs_company_id` (`company_id`),
  CONSTRAINT `fk_jobs_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- job_skills (ElementCollection)
CREATE TABLE `job_skills` (
  `job_id` varchar(36) NOT NULL,
  `skill` varchar(100) NOT NULL,
  PRIMARY KEY (`job_id`, `skill`),
  KEY `idx_job_skills_job_id` (`job_id`),
  CONSTRAINT `fk_job_skills_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- role_permissions (ManyToMany)
CREATE TABLE `role_permissions` (
  `role_id` varchar(36) NOT NULL,
  `permission_id` varchar(36) NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`),
  KEY `idx_role_permissions_role_id` (`role_id`),
  KEY `idx_role_permissions_permission_id` (`permission_id`),
  CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `fk_role_permissions_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- subscribers
CREATE TABLE `subscribers` (
  `id` varchar(36) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- subscriber_skills (ElementCollection)
CREATE TABLE `subscriber_skills` (
  `subscriber_id` varchar(36) NOT NULL,
  `skill` varchar(100) NOT NULL,
  PRIMARY KEY (`subscriber_id`, `skill`),
  KEY `idx_subscriber_skills_subscriber_id` (`subscriber_id`),
  CONSTRAINT `fk_subscriber_skills_subscriber` FOREIGN KEY (`subscriber_id`) REFERENCES `subscribers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- resumes
CREATE TABLE `resumes` (
  `id` varchar(36) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `url` varchar(512) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `company_id` varchar(36) DEFAULT NULL,
  `job_id` varchar(36) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `created_by_id` varchar(255) DEFAULT NULL,
  `created_by_email` varchar(255) DEFAULT NULL,
  `updated_by_id` varchar(255) DEFAULT NULL,
  `updated_by_email` varchar(255) DEFAULT NULL,
  `deleted_by_id` varchar(255) DEFAULT NULL,
  `deleted_by_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_resumes_company_id` (`company_id`),
  KEY `idx_resumes_job_id` (`job_id`),
  CONSTRAINT `fk_resumes_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`),
  CONSTRAINT `fk_resumes_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- resume_history (ElementCollection of embeddable)
CREATE TABLE `resume_history` (
  `resume_id` varchar(36) NOT NULL,
  `status` varchar(50) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `updated_by_id` varchar(255) DEFAULT NULL,
  `updated_by_email` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`resume_id`, `updated_at`),
  KEY `idx_resume_history_resume_id` (`resume_id`),
  CONSTRAINT `fk_resume_history_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


