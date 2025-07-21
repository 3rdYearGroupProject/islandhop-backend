-- Add profile_completion column to support_profiles table
ALTER TABLE support_profiles 
ADD COLUMN profile_completion INTEGER NOT NULL DEFAULT 0;

-- Add profile_completion column to tourist_profiles table  
ALTER TABLE tourist_profiles 
ADD COLUMN profile_completion INTEGER NOT NULL DEFAULT 0;

-- Update existing records to set appropriate profile_completion values
-- For support profiles, set to 1 if profile has all required fields filled
UPDATE support_profiles 
SET profile_completion = 1 
WHERE first_name IS NOT NULL 
  AND last_name IS NOT NULL 
  AND contact_no IS NOT NULL 
  AND address IS NOT NULL 
  AND TRIM(first_name) != '' 
  AND TRIM(last_name) != '' 
  AND TRIM(contact_no) != '' 
  AND TRIM(address) != '';

-- For tourist profiles, set to 1 if profile has all required fields filled
UPDATE tourist_profiles 
SET profile_completion = 1 
WHERE first_name IS NOT NULL 
  AND last_name IS NOT NULL 
  AND nationality IS NOT NULL 
  AND TRIM(first_name) != '' 
  AND TRIM(last_name) != '' 
  AND TRIM(nationality) != '';
