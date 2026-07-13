
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    amount_paid NUMERIC(19, 2),
    payment_method VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    service_name VARCHAR(255),
    customer_email VARCHAR(255),
    customer_name VARCHAR(255),
    phone_number VARCHAR(50),
    date_paid TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_payment_bill_id ON payment (bill_id);
CREATE INDEX IF NOT EXISTS idx_payment_transaction_id ON payment (transaction_id);
CREATE INDEX IF NOT EXISTS idx_payment_customer_email ON payment (customer_email);
CREATE INDEX IF NOT EXISTS idx_payment_date_paid ON payment (date_paid);
CREATE INDEX IF NOT EXISTS idx_payment_status ON payment (status);
CREATE INDEX IF NOT EXISTS idx_payment_method ON payment (payment_method);
