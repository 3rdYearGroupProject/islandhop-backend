-- Fix profile_pic column type mismatch
-- This script fixes the PostgreSQL error: column "profile_pic" is of type bytea but expression is of type oid

-- First, let's check the current column type
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';

-- If the column exists and is of type 'oid', we need to alter it to 'bytea'
-- If it's already 'bytea', the issue might be with Hibernate mapping

-- Drop the column if it exists and recreate it as bytea
ALTER TABLE tourist_profiles 
DROP COLUMN IF EXISTS profile_pic;

-- Add the column back as bytea
ALTER TABLE tourist_profiles 
ADD COLUMN profile_pic bytea;

-- Verify the change
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';

-- Show the table structure
\d tourist_profiles;
