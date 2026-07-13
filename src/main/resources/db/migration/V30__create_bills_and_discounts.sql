
--  invoice table
CREATE TABLE IF NOT EXISTS invoice (
    id SERIAL PRIMARY KEY,
    bill_id BIGINT UNIQUE,
    invoice_number VARCHAR(255) UNIQUE NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--  discounts table
CREATE TABLE IF NOT EXISTS discounts (
    id SERIAL PRIMARY KEY,
    discount_type VARCHAR(50) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL CHECK (discount_value >= 0),
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--  bills table with foreign key constraint
CREATE TABLE IF NOT EXISTS bills (
    id SERIAL PRIMARY KEY,
    response_id BIGINT,
    form_id BIGINT,
    service_name VARCHAR(255) NOT NULL,
    billing_type VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    frequency VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    discount_id BIGINT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS discount_data (
    id SERIAL PRIMARY KEY,
    discount_id BIGINT,
    service_name VARCHAR(255),
    original_amount DECIMAL(10,2),
    discount_type VARCHAR(50),
    discount_percentage DECIMAL(5,2),
    discount_amount DECIMAL(10,2),
    discounted_price DECIMAL(10,2),
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_bills_response_id ON bills(response_id);
CREATE INDEX IF NOT EXISTS idx_bills_form_id ON bills(form_id);
CREATE INDEX IF NOT EXISTS idx_bills_discount_id ON bills(discount_id);