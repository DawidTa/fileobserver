-- liguibase formatted sql
-- changeset dtaczkowski:1
CREATE TABLE `users_jobs` (
  `account_id` varchar(255) NOT NULL,
  `job_id` varchar(255) NOT NULL,
  PRIMARY KEY (`account_id`,`job_id`),
  KEY `job_id` (`job_id`),
  CONSTRAINT `users_jobs_ibfk_1` FOREIGN KEY (`account_id`) REFERENCES `account` (`account_id`),
  CONSTRAINT `users_jobs_ibfk_2` FOREIGN KEY (`job_id`) REFERENCES `job` (`job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci