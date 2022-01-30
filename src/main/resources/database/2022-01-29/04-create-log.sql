-- liguibase formatted sql
-- changeset dtaczkowski:1
CREATE TABLE `log` (
  `log_id` varchar(45) NOT NULL,
  `operation_type` varchar(255) DEFAULT NULL,
  `path` varchar(260) DEFAULT NULL,
  `job_start_date` datetime DEFAULT NULL,
  `job_end_date` datetime DEFAULT NULL,
  PRIMARY KEY (`log_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci