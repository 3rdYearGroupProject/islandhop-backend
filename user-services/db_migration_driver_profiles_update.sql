-- Migration script for driver_profiles table
-- Add all required columns for comprehensive driver profile management

-- Drop existing columns that are being replaced
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS full_name;
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS contact_number;
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS nic_passport;
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS vehicle_type;
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS vehicle_number;
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS body_type;
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS ac_available;
ALTER TABLE driver_profiles DROP COLUMN IF EXISTS number_of_seats;

-- Add new personal information columns
ALTER TABLE driver_profiles ADD COLUMN first_name VARCHAR(100);
ALTER TABLE driver_profiles ADD COLUMN last_name VARCHAR(100);
ALTER TABLE driver_profiles ADD COLUMN phone_number VARCHAR(20);
ALTER TABLE driver_profiles ADD COLUMN date_of_birth DATE;
ALTER TABLE driver_profiles ADD COLUMN address TEXT;
ALTER TABLE driver_profiles ADD COLUMN emergency_contact_name VARCHAR(100);
ALTER TABLE driver_profiles ADD COLUMN emergency_contact_number VARCHAR(20);

-- Rename profile picture column if it exists
ALTER TABLE driver_profiles RENAME COLUMN profile_picture_url TO profile_picture_url;

-- Add driving license columns
ALTER TABLE driver_profiles ADD COLUMN driving_license_image TEXT;
ALTER TABLE driver_profiles ADD COLUMN driving_license_number VARCHAR(50);
ALTER TABLE driver_profiles ADD COLUMN driving_license_expiry_date DATE;
ALTER TABLE driver_profiles ADD COLUMN driving_license_uploaded_date DATE;
ALTER TABLE driver_profiles ADD COLUMN driving_license_verified INTEGER DEFAULT 0;

-- Add SLTDA license columns
ALTER TABLE driver_profiles ADD COLUMN sltda_license_image TEXT;
ALTER TABLE driver_profiles ADD COLUMN sltda_license_number VARCHAR(50);
ALTER TABLE driver_profiles ADD COLUMN sltda_license_expiry_date DATE;
ALTER TABLE driver_profiles ADD COLUMN sltda_license_uploaded_date DATE;
ALTER TABLE driver_profiles ADD COLUMN sltda_license_verified INTEGER DEFAULT 0;

-- Add trip preference columns
ALTER TABLE driver_profiles ADD COLUMN accept_partial_trips INTEGER DEFAULT 0;
ALTER TABLE driver_profiles ADD COLUMN auto_accept_trips INTEGER DEFAULT 0;
ALTER TABLE driver_profiles ADD COLUMN maximum_trip_distance INTEGER;

-- Add driver statistics columns
ALTER TABLE driver_profiles ADD COLUMN rating DECIMAL(3,2) DEFAULT 0.0;
ALTER TABLE driver_profiles ADD COLUMN number_of_reviews INTEGER DEFAULT 0;
ALTER TABLE driver_profiles ADD COLUMN total_completed_trips INTEGER DEFAULT 0;

-- Ensure profile_completion column exists
ALTER TABLE driver_profiles ADD COLUMN IF NOT EXISTS profile_completion INTEGER DEFAULT 0;

-- Update existing records to set appropriate default values
UPDATE driver_profiles SET 
    rating = COALESCE(rating, 0.0),
    number_of_reviews = COALESCE(number_of_reviews, 0),
    total_completed_trips = COALESCE(total_completed_trips, 0),
    accept_partial_trips = COALESCE(accept_partial_trips, 0),
    auto_accept_trips = COALESCE(auto_accept_trips, 0),
    driving_license_verified = COALESCE(driving_license_verified, 0),
    sltda_license_verified = COALESCE(sltda_license_verified, 0),
    profile_completion = COALESCE(profile_completion, 0);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_driver_profiles_email ON driver_profiles(email);
CREATE INDEX IF NOT EXISTS idx_driver_profiles_completion ON driver_profiles(profile_completion);
CREATE INDEX IF NOT EXISTS idx_driver_profiles_rating ON driver_profiles(rating);
CREATE INDEX IF NOT EXISTS idx_driver_profiles_license_verified ON driver_profiles(driving_license_verified, sltda_license_verified);
