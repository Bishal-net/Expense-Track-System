CREATE DATABASE IF NOT EXISTS expensestracker;
USE expensestracker;

CREATE TABLE IF NOT EXISTS expensestracker_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    price INT,
    category VARCHAR(255),
    created_date DATETIME,
    modified_date DATETIME
);