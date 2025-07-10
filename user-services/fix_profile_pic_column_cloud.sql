-- Fix profile_pic column type to ensure it's properly bytea
-- Run this script in your cloud PostgreSQL database

-- First, let's check the current column type
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';

-- If the column is not bytea, we need to fix it
-- First, drop the column if it exists with wrong type
ALTER TABLE tourist_profiles DROP COLUMN IF EXISTS profile_pic;

-- Add the column back with correct bytea type
ALTER TABLE tourist_profiles ADD COLUMN profile_pic bytea;

-- Verify the column was created correctly
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';
