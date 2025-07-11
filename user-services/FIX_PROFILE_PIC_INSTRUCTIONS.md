# Instructions to Fix the profile_pic Column Type Issue

## Problem

The error `column "profile_pic" is of type bytea but expression is of type oid` occurs because there's a mismatch between the database column type and what Hibernate expects.

## Solution 1: Use TEXT Column (RECOMMENDED - SIMPLEST)

### Step 1: Connect to your PostgreSQL database

Use your preferred PostgreSQL client (pgAdmin, DBeaver, or psql command line):

- **Database**: `islandhop_db`
- **Username**: `postgres` (or your configured username)
- **Password**: Your PostgreSQL password

### Step 2: Run the TEXT column fix SQL

```sql
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
```

### Step 3: Restart your Spring Boot application

The Java code has already been updated to use `String profilePic` instead of `byte[] profilePic`.

### Step 4: Test the API

Try the `/api/v1/tourist/session-register` endpoint again.

## Solution 2: Use BYTEA Column (ALTERNATIVE)

If you prefer to store actual binary data:

### Step 1: Run the BYTEA column fix SQL

```sql
-- Check current column type
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';

-- Drop the problematic column
ALTER TABLE tourist_profiles
DROP COLUMN IF EXISTS profile_pic;

-- Add the column back as bytea
ALTER TABLE tourist_profiles
ADD COLUMN profile_pic bytea;

-- Verify the change
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'tourist_profiles' AND column_name = 'profile_pic';
```

### Step 2: Update Java Entity

Change the TouristProfile.java back to use byte[]:

```java
@Column(name = "profile_pic", columnDefinition = "bytea")
private byte[] profilePic;
```

## Files Changed (Current State)

- `TouristProfile.java`: Updated to use `String profilePic` for Base64 storage
- Database: Need to run one of the SQL scripts above

## Recommendation

Use **Solution 1 (TEXT column)** as it's simpler and avoids PostgreSQL-specific bytea/oid mapping issues. Base64 strings are also easier to work with in APIs and frontend applications.
