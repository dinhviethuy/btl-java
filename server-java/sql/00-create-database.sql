-- Initialize database (drop & create)
-- Usage: mysql -u root -p < server-java/sql/00-create-database.sql

DROP DATABASE IF EXISTS `job_finder`;
CREATE DATABASE `job_finder` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `job_finder`;

-- Optionally create an application user (edit credentials as needed)
-- CREATE USER IF NOT EXISTS 'job_user'@'localhost' IDENTIFIED BY 'password';
-- GRANT ALL PRIVILEGES ON `job_finder`.* TO 'job_user'@'localhost';
-- FLUSH PRIVILEGES;


