-- Execute em sua interface gráfica ou console

-- Drop database if exists
DROP DATABASE IF EXISTS `company`;

-- Create database
CREATE DATABASE `company` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Use database
USE `company`;

-- Table structure for table `employees`
DROP TABLE IF EXISTS `employees`;
CREATE TABLE `employees` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `birth_date` date NOT NULL,
  `age` int unsigned NOT NULL,
  `hire_date` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `last_update_date` datetime DEFAULT NULL,
  `document` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `document` (`document`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table `employees`
INSERT INTO `employees` (`id`, `name`, `birth_date`, `age`, `hire_date`, `document`) VALUES
(48, 'Jon', '2024-05-01', 10, '2024-05-01 16:01:06', '1'),
(50, 'Peter', '2024-05-01', 19, '2024-05-01 16:16:39', '2'),
(51, 'Normal', '2024-05-22', 19, '2024-05-22 08:56:21', '17162552'),
(52, 'Normal1', '2024-05-22', 19, '2024-05-22 08:58:03', '17162532'),
(61, 'Hireee', '2000-12-10', 99, '2024-05-28 08:47:18', '1918261652');

-- Table structure for table `departaments`
DROP TABLE IF EXISTS `departaments`;
CREATE TABLE `departaments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `creation_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `last_update_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table `departaments`
INSERT INTO `departaments` (`id`, `name`, `creation_date`) VALUES
(24, 'Sales', '2024-05-01 15:08:56'),
(27, 'Management', '2024-05-01 15:55:12');

-- Table structure for table `departaments_has_employees`
DROP TABLE IF EXISTS `departaments_has_employees`;
CREATE TABLE `departaments_has_employees` (
  `id_departament` bigint NOT NULL,
  `id_employee` bigint NOT NULL,
  `level` enum('JUNIOR','MID','SENIOR') DEFAULT NULL,
  `salary` decimal(15,2) unsigned NOT NULL,
  PRIMARY KEY (`id_departament`, `id_employee`),
  CONSTRAINT `fk_departments_has_employees_departments` FOREIGN KEY (`id_departament`) REFERENCES `departaments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_departments_has_employees_employees` FOREIGN KEY (`id_employee`) REFERENCES `employees` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table `departaments_has_employees`
INSERT INTO `departaments_has_employees` (`id_departament`, `id_employee`, `level`, `salary`) VALUES
(27, 50, 'JUNIOR', 1000.00),
(24, 51, 'JUNIOR', 1200.00),
(24, 52, 'JUNIOR', 1200.00),
(24, 61, 'JUNIOR', 1200.00);

-- Table structure for table `normal_employees`
DROP TABLE IF EXISTS `normal_employees`;
CREATE TABLE `normal_employees` (
  `id` bigint NOT NULL,
  `has_faculty` tinyint NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_normal_employees_employees` FOREIGN KEY (`id`) REFERENCES `employees` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table `normal_employees`
INSERT INTO `normal_employees` (`id`, `has_faculty`) VALUES
(50, 0),
(51, 1),
(52, 1);

-- Table structure for table `superior_employees`
DROP TABLE IF EXISTS `superior_employees`;
CREATE TABLE `superior_employees` (
  `id` bigint NOT NULL,
  `work_experience` int unsigned NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_superior_employees_employees` FOREIGN KEY (`id`) REFERENCES `employees` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table `superior_employees`
INSERT INTO `superior_employees` (`id`, `work_experience`) VALUES
(61, 10);

-- Table structure for table `users`
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `password` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_name` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Dumping data for table `users`
INSERT INTO `users` (`id`, `username`, `password`) VALUES
(15, 'Jayy', '*');
