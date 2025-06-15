-- Create emergency_alerts table
CREATE TABLE IF NOT EXISTS emergency_alerts (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    booking_id VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    triggered_by VARCHAR(50) NOT NULL
);

-- Create notification_logs table
CREATE TABLE IF NOT EXISTS notification_logs (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    recipient_id VARCHAR(255) NOT NULL,
    delivery_time TIMESTAMP NOT NULL,
    read BOOLEAN NOT NULL,
    type VARCHAR(50) NOT NULL,
    FOREIGN KEY (alert_id) REFERENCES emergency_alerts(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_user_id ON emergency_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_booking_id ON emergency_alerts(booking_id);
CREATE INDEX IF NOT EXISTS idx_emergency_alerts_status ON emergency_alerts(status);
CREATE INDEX IF NOT EXISTS idx_notification_logs_alert_id ON notification_logs(alert_id);
CREATE INDEX IF NOT EXISTS idx_notification_logs_recipient_id ON notification_logs(recipient_id); 