-- Alternative fix: Change profile_pic to TEXT type for Base64 storage
-- This avoids the bytea/oid mapping issues

-- Drop the existing column
ALTER TABLE tourist_profiles 
DROP COLUMN IF EXISTS profile_pic;

-- Add the column as TEXT to store Base64 encoded images
ALTER TABLE tourist_profiles 
ADD COLUMN profile_pic TEXT;

-- Verify the change
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';
