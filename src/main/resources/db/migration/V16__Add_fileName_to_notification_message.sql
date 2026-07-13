ALTER TABLE notification_message
ADD COLUMN IF NOT EXISTS file_name VARCHAR(255);
