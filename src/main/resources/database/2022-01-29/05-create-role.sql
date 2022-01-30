-- liguibase formatted sql
-- changeset dtaczkowski:1
CREATE TABLE `role` (
  `role_id` varchar(255) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci