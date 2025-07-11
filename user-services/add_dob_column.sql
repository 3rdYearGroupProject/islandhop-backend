-- Add dob column to tourist_profiles table if it doesn't exist
-- Run this in your cloud PostgreSQL database

-- Check if the column exists
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'tourist_profiles' AND column_name = 'dob';

-- Add the column if it doesn't exist (uncomment if needed)
-- ALTER TABLE tourist_profiles ADD COLUMN IF NOT EXISTS dob DATE;

-- Verify the column was added
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'tourist_profiles' 
ORDER BY ordinal_position;
