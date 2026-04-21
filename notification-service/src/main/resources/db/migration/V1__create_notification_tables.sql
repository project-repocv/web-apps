-- Create notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP WITH TIME ZONE,
    retry_count INTEGER DEFAULT 0,
    transaction_id VARCHAR(100)
);

-- Create notification_preferences table
CREATE TABLE notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(50) UNIQUE NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create preference_channels table
CREATE TABLE preference_channels (
    preference_id BIGINT REFERENCES notification_preferences(id) ON DELETE CASCADE,
    channel VARCHAR(30) NOT NULL,
    PRIMARY KEY (preference_id, channel)
);

-- Create preference_types table
CREATE TABLE preference_types (
    preference_id BIGINT REFERENCES notification_preferences(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY (preference_id, type)
);

-- Create indexes
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_transaction_id ON notifications(transaction_id);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notification_preferences_customer_id ON notification_preferences(customer_id);
