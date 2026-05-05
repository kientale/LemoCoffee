CREATE TABLE IF NOT EXISTS customer_point_transaction (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    order_id INT NULL,
    type VARCHAR(30) NOT NULL,
    points INT NOT NULL,
    balance_after INT NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_customer_point_transaction_customer_id (customer_id),
    INDEX idx_customer_point_transaction_order_id (order_id),
    CONSTRAINT fk_customer_point_transaction_customer
        FOREIGN KEY (customer_id) REFERENCES customer(id),
    CONSTRAINT fk_customer_point_transaction_order
        FOREIGN KEY (order_id) REFERENCES coffee_order(id)
);
