-- Drop first to guarantee a clean state
DROP TABLE IF EXISTS public.bills CASCADE;

-- Recreate the table only if it doesn't exist
CREATE TABLE IF NOT EXISTS public.bills (
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

-- Create indexes safely
CREATE INDEX IF NOT EXISTS idx_bills_form_id ON public.bills(form_id);
CREATE INDEX IF NOT EXISTS idx_bills_discount_id ON public.bills(discount_id);
CREATE INDEX IF NOT EXISTS idx_bills_status ON public.bills(status);
CREATE INDEX IF NOT EXISTS idx_bills_payment_method ON public.bills(payment_method);
