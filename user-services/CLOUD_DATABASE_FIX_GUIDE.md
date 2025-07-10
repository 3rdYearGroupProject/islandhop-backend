# Profile Picture Cloud Database Fix Guide

## Issue Description

The application is encountering an error when trying to update the profile picture in the cloud PostgreSQL database:

```
ERROR: column "profile_pic" is of type bytea but expression is of type bigint
```

## Root Cause

The issue occurs when PostgreSQL's bytea column type is not properly handled by Hibernate/JPA when storing byte arrays.

## Solution Steps

### 1. Database Schema Fix (Run in your cloud PostgreSQL)

Connect to your cloud PostgreSQL database and run the following SQL commands:

```sql
-- Check current column type
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';

-- If the column exists with wrong type, drop it
ALTER TABLE tourist_profiles DROP COLUMN IF EXISTS profile_pic;

-- Add the column back with correct bytea type
ALTER TABLE tourist_profiles ADD COLUMN profile_pic bytea;

-- Verify the column was created correctly
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';
```

### 2. Code Changes Made

The following code changes have been implemented:

#### TouristProfile.java

- Removed manual getter/setter methods (Lombok handles this)
- Kept the `@Column(name = "profile_pic", columnDefinition = "bytea")` annotation

#### TouristController.java

- Improved byte array conversion with proper byte range handling
- Added better error handling and logging
- Added validation for empty arrays

#### application.yml

- Added PostgreSQL-specific JPA properties for better LOB handling

### 3. How to Apply the Fix

1. **Run the SQL script** on your cloud database to fix the column type:

   ```bash
   # Use your cloud database management tool or command line
   psql -h your-cloud-host -U your-username -d islandhop -f fix_profile_pic_column_cloud.sql
   ```

2. **Restart your application** to apply the new JPA configuration

3. **Test the profile update** with a profile picture

### 4. Frontend Integration

The frontend should continue to send the profile picture as a byte array (list of integers):

```javascript
// Frontend code example
const profilePictureBytes = [51, 127, 182, 157, 135, 101, 99, 197, ...];

const updateData = {
  firstName: "John",
  lastName: "Doe",
  nationality: "Sri Lanka",
  languages: ["English", "සිංහල"],
  profilePicture: profilePictureBytes  // Array of integers (0-255)
};

fetch('/api/v1/tourist/profile', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(updateData)
});
```

### 5. Verification

After applying the fix, you should see:

- No more "bytea vs bigint" errors
- Successful profile picture updates
- Proper storage and retrieval of profile pictures

### 6. Monitoring

The application now includes enhanced logging to help monitor profile picture operations:

- Log messages show the data type and size of incoming profile pictures
- Better error handling with specific error messages
- Validation of byte array conversion

## Additional Notes

- The `non_contextual_creation: true` property helps with PostgreSQL LOB handling
- Profile pictures are stored as bytea (binary data) in the database
- The application correctly converts JavaScript arrays to Java byte arrays
- Empty arrays are handled by setting the profile picture to null

If you continue to experience issues, check the application logs for the detailed error messages and data type information.
