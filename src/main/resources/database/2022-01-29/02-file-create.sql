-- liguibase formatted sql
-- changeset dtaczkowski:1
CREATE TABLE `file` (
  `file_id` varchar(255) NOT NULL,
  `file_path` varchar(255) DEFAULT NULL,
  `file_content` longtext,
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci