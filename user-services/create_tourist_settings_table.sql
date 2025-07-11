-- Create tourist_settings table
-- Run this in your cloud PostgreSQL database

CREATE TABLE IF NOT EXISTS tourist_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    currency VARCHAR(10) DEFAULT 'USD',
    units VARCHAR(20) DEFAULT 'Imperial',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_tourist_settings_email ON tourist_settings(email);

-- Verify the table was created
SELECT column_name, data_type, character_maximum_length, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'tourist_settings'
ORDER BY ordinal_position;
