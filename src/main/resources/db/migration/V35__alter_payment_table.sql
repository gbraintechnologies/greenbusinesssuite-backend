-- Add 'bank_code' column to payment table
ALTER TABLE payment
ADD COLUMN IF NOT EXISTS bank_code VARCHAR(255);

-- Add 'network' column to payment table
ALTER TABLE payment
ADD COLUMN IF NOT EXISTS network VARCHAR(50);

-- Drop constraint only if it exists
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'invoice_bill_id_key'
    ) THEN
        ALTER TABLE invoice DROP CONSTRAINT invoice_bill_id_key;
    END IF;
END $$;

-- Add 'is_deleted' column to discount_data table
ALTER TABLE discount_data
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN DEFAULT FALSE;
