-- liguibase formatted sql
-- changeset dtaczkowski:1
CREATE TABLE `job` (
  `job_id` varchar(255) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  PRIMARY KEY (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci