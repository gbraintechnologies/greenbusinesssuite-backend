-- Begin transaction for atomic execution
BEGIN;

-- ============================
-- Drop legacy billing tables
-- ============================

DROP TABLE IF EXISTS bills CASCADE;
DROP TABLE IF EXISTS bill_payment_methods;

-- ============================
-- Create new billing table
-- ============================

CREATE TABLE IF NOT EXISTS billing (
    id SERIAL PRIMARY KEY,
    form_id BIGINT,
    discount_id BIGINT NULL,
    service_name VARCHAR(255) NOT NULL,
    billing_type VARCHAR(50) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL CHECK (amount >= 0),
    frequency VARCHAR(50),
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- Create indexes safely
-- ============================

CREATE INDEX IF NOT EXISTS idx_billing_form_id ON billing(form_id);
CREATE INDEX IF NOT EXISTS idx_billing_discount_id ON billing(discount_id);
CREATE INDEX IF NOT EXISTS idx_billing_status ON billing(status);
CREATE INDEX IF NOT EXISTS idx_billing_payment_method ON billing(payment_method);

-- Commit transaction
COMMIT;
