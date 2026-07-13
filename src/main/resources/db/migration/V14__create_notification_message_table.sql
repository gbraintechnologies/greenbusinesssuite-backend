CREATE TABLE IF NOT EXISTS notification_message (
    id SERIAL PRIMARY KEY,
    sender VARCHAR(255) NOT NULL,
    total_recipients INT,
    subject VARCHAR(255) NOT NULL,
    body TEXT,
    is_html BOOLEAN DEFAULT false,
    recurring_type VARCHAR(50),
    message_type VARCHAR(50),
    trigger_time TIMESTAMP,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_date TIMESTAMP,
    end_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification_recipients (
    notification_id BIGINT NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    PRIMARY KEY (notification_id, recipient),
    FOREIGN KEY (notification_id) REFERENCES notification_message(id) ON DELETE CASCADE
);
