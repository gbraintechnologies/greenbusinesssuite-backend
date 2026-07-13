-- Dropping obsolete columns from bills table
ALTER TABLE bills
DROP COLUMN payment_method;

ALTER TABLE bills
DROP COLUMN response_id;

ALTER TABLE bills
DROP COLUMN customer_email;

ALTER TABLE bills
DROP COLUMN customer_name;

-- Creating bill_payment_methods table for payment methods
CREATE TABLE IF NOT EXISTS bill_payment_methods (
    bill_id BIGINT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    PRIMARY KEY (bill_id, payment_method),
    FOREIGN KEY (bill_id) REFERENCES bills (id)
);

-- Adding index for performance optimization
CREATE INDEX IF NOT EXISTS idx_bill_id ON bill_payment_methods (bill_id);