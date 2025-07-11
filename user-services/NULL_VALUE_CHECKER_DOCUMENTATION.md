# Tourist Null Value Checker Endpoint Documentation

## Endpoint Overview

**GET** `/tourist/check-null-values`

This endpoint checks for null values across all tourist-related database tables for a specific tourist email address.

## Purpose

- Diagnose data integrity issues in tourist accounts
- Identify incomplete profile information
- Debug null pointer exceptions in API responses
- Validate data consistency across related tables

## Tables Checked

1. **tourist_accounts** - Main account information
2. **tourist_profiles** - Personal profile details
3. **tourist_profile_languages** - Language preferences (via languages field)
4. **tourist_settings** - User preferences and settings

## Request Parameters

- `email` (optional) - Email address to check. If not provided, uses session email

## Authentication

- Requires active session OR email parameter
- Returns 401 if not authenticated

## Response Format

### Success Response (200 OK)

```json
{
  "email": "user@example.com",
  "nullFields": {
    "tourist_accounts": {
      "status": "null"
    },
    "tourist_profiles": {
      "firstName": "null",
      "lastName": "null",
      "dob": "null",
      "nationality": "null",
      "languages": "null",
      "profilePic": "null"
    },
    "tourist_profile_languages": {
      "languages": "empty/null"
    },
    "tourist_settings": {
      "entire_settings": "null"
    }
  },
  "message": "Null value check completed"
}
```

### When No Null Values Found

```json
{
  "email": "user@example.com",
  "nullFields": {},
  "message": "Null value check completed"
}
```

### Error Responses

#### 401 Unauthorized

```json
"Not authenticated"
```

#### 400 Bad Request

```json
"Tourist account does not exist"
```

#### 500 Internal Server Error

```json
"Error checking null values: [error message]"
```

## Field Descriptions

### tourist_accounts table

- `id` - Primary key UUID
- `email` - User's email address
- `status` - Account status (ACTIVE, DEACTIVATED, etc.)

### tourist_profiles table

- `id` - Primary key UUID
- `email` - User's email address
- `firstName` - User's first name
- `lastName` - User's last name
- `dob` - Date of birth
- `nationality` - User's nationality
- `languages` - List of preferred languages
- `profileCompletion` - Profile completion status (0 or 1)
- `profilePic` - Profile picture as byte array

### tourist_profile_languages table

- Checked implicitly via the `languages` field in profiles table
- Reports "empty/null" if no languages are set

### tourist_settings table

- `id` - Primary key UUID
- `email` - User's email address
- `currency` - Preferred currency
- `units` - Preferred units (Imperial/Metric)
- `createdAt` - Record creation timestamp
- `updatedAt` - Last update timestamp

## Usage Examples

### JavaScript (Frontend)

```javascript
// Check current logged-in user's null values
fetch("/tourist/check-null-values", {
  method: "GET",
  credentials: "include",
})
  .then((response) => response.json())
  .then((data) => {
    console.log("Null fields:", data.nullFields);

    // Check if any null values exist
    if (Object.keys(data.nullFields).length > 0) {
      console.log("Found null values in tables:", Object.keys(data.nullFields));
    } else {
      console.log("No null values found");
    }
  });

// Check specific user's null values
fetch("/tourist/check-null-values?email=user@example.com", {
  method: "GET",
  credentials: "include",
})
  .then((response) => response.json())
  .then((data) => console.log(data));
```

### PowerShell (Testing)

```powershell
# Check null values for authenticated user
Invoke-RestMethod -Uri "http://localhost:8080/tourist/check-null-values" -Method Get -WebSession $session

# Check null values for specific email
Invoke-RestMethod -Uri "http://localhost:8080/tourist/check-null-values?email=test@example.com" -Method Get -WebSession $session
```

### cURL

```bash
# Check current user's null values
curl -X GET "http://localhost:8080/tourist/check-null-values" \
  -H "Content-Type: application/json" \
  --cookie-jar cookies.txt --cookie cookies.txt

# Check specific user's null values
curl -X GET "http://localhost:8080/tourist/check-null-values?email=user@example.com" \
  -H "Content-Type: application/json" \
  --cookie-jar cookies.txt --cookie cookies.txt
```

## Common Use Cases

### 1. Profile Completion Analysis

Check which profile fields are missing to guide user through completion:

```javascript
if (data.nullFields.tourist_profiles) {
  const missingFields = Object.keys(data.nullFields.tourist_profiles);
  console.log("Missing profile fields:", missingFields);
}
```

### 2. Settings Initialization

Check if user needs default settings created:

```javascript
if (data.nullFields.tourist_settings?.entire_settings === "null") {
  console.log("User needs settings initialized");
}
```

### 3. Data Integrity Validation

Verify all core fields are populated:

```javascript
const criticalFields = ["firstName", "lastName", "nationality"];
const profileNulls = data.nullFields.tourist_profiles || {};
const missingCritical = criticalFields.filter(
  (field) => profileNulls[field] === "null"
);

if (missingCritical.length > 0) {
  console.log("Critical fields missing:", missingCritical);
}
```

## Implementation Notes

- **Non-destructive**: This endpoint only reads data, never modifies it
- **Performance**: Uses existing repository methods for efficient queries
- **Error Handling**: Comprehensive error handling with detailed logging
- **Session Support**: Works with both session-based and parameter-based authentication
- **Null vs Empty**: Distinguishes between null values and empty collections

## Related Endpoints

- `GET /tourist/profile` - Get profile information
- `GET /tourist/settings` - Get user settings
- `PUT /tourist/profile` - Update profile (to fix null values)
- `PUT /tourist/settings` - Update settings (to fix null values)

## Security Considerations

- Requires valid session or email parameter
- Only checks data for the authenticated user or specified email
- Does not expose sensitive data, only reports field names that are null
- All operations are read-only for security

## Troubleshooting

### Common Issues

1. **"Not authenticated"** - Ensure valid session or provide email parameter
2. **"Tourist account does not exist"** - Verify email exists in database
3. **Empty nullFields object** - All fields have values (good!)
4. **"entire_profile": "null"** - Profile was never created for this user

### Debug Steps

1. Check if user account exists: `GET /tourist/me`
2. Verify session validity: `GET /tourist/session/validate`
3. Check individual endpoints: `GET /tourist/profile`, `GET /tourist/settings`
4. Review server logs for detailed error messages
