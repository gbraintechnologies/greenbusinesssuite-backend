-- Add the column safely
ALTER TABLE payment
ADD COLUMN IF NOT EXISTS response_id BIGINT;

-- Create the index only if it doesn't already exist
CREATE INDEX IF NOT EXISTS idx_payment_response_id ON payment(response_id);
