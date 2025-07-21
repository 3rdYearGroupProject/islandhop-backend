# Driver Profile Implementation Documentation

## Overview

Complete implementation of driver profile management system with comprehensive fields for personal information, licenses, preferences, and statistics.

## Database Schema Changes

### Driver Profiles Table Structure

The `driver_profiles` table now includes the following columns:

#### Personal Information

- `email` (VARCHAR, UNIQUE, NOT NULL) - Driver's email address
- `first_name` (VARCHAR(100)) - Driver's first name
- `last_name` (VARCHAR(100)) - Driver's last name
- `phone_number` (VARCHAR(20)) - Driver's contact number
- `date_of_birth` (DATE) - Driver's date of birth
- `address` (TEXT) - Driver's residential address
- `emergency_contact_name` (VARCHAR(100)) - Emergency contact person's name
- `emergency_contact_number` (VARCHAR(20)) - Emergency contact phone number
- `profile_picture_url` (TEXT) - Base64 encoded profile picture or URL

#### License Information

**Driving License:**

- `driving_license_image` (TEXT) - Base64 encoded license image
- `driving_license_number` (VARCHAR(50)) - License number
- `driving_license_expiry_date` (DATE) - License expiry date
- `driving_license_uploaded_date` (DATE) - Date when license was uploaded
- `driving_license_verified` (INTEGER) - Verification status (0=not verified, 1=verified)

**SLTDA License:**

- `sltda_license_image` (TEXT) - Base64 encoded license image
- `sltda_license_number` (VARCHAR(50)) - License number
- `sltda_license_expiry_date` (DATE) - License expiry date
- `sltda_license_uploaded_date` (DATE) - Date when license was uploaded
- `sltda_license_verified` (INTEGER) - Verification status (0=not verified, 1=verified)

#### Trip Preferences

- `accept_partial_trips` (INTEGER) - Whether driver accepts partial trips (0=no, 1=yes)
- `auto_accept_trips` (INTEGER) - Whether to auto-accept trips (0=no, 1=yes)
- `maximum_trip_distance` (INTEGER) - Maximum trip distance in kilometers

#### Driver Statistics

- `rating` (DECIMAL(3,2)) - Average driver rating (0.0-5.0)
- `number_of_reviews` (INTEGER) - Total number of reviews received
- `total_completed_trips` (INTEGER) - Total trips completed

#### System Fields

- `profile_completion` (INTEGER) - Profile completion status (0=incomplete, 1=complete)
- `created_at` (TIMESTAMP) - Profile creation timestamp
- `updated_at` (TIMESTAMP) - Last update timestamp

## API Endpoints

### GET /driver/profile

Retrieves driver profile information.

**Parameters:**

- `email` (optional query parameter) - Driver's email. If not provided, uses session email.

**Response:**

```json
{
  "id": "uuid",
  "email": "driver@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+94771234567",
  "dateOfBirth": "1990-05-15",
  "address": "123 Main St, Colombo",
  "emergencyContactName": "Jane Doe",
  "emergencyContactNumber": "+94771234568",
  "profilePictureUrl": "base64_encoded_image",
  "drivingLicenseNumber": "DL123456",
  "drivingLicenseExpiryDate": "2025-12-31",
  "drivingLicenseVerified": 1,
  "sltdaLicenseNumber": "SLTDA789",
  "sltdaLicenseExpiryDate": "2026-06-30",
  "sltdaLicenseVerified": 1,
  "acceptPartialTrips": 1,
  "autoAcceptTrips": 0,
  "maximumTripDistance": 200,
  "rating": 4.5,
  "numberOfReviews": 150,
  "totalCompletedTrips": 300,
  "profileCompletion": 1,
  "createdAt": "2023-01-15T10:30:00",
  "updatedAt": "2024-12-01T15:45:00"
}
```

### PUT /driver/profile

Updates driver profile information.

**Request Body:**

```json
{
  "email": "driver@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+94771234567",
  "dateOfBirth": "1990-05-15",
  "address": "123 Main St, Colombo",
  "emergencyContactName": "Jane Doe",
  "emergencyContact": "+94771234568",
  "profilePicture": "base64_encoded_image",
  "acceptPartialTrips": true,
  "autoAcceptTrips": false,
  "maxDistance": 200
}
```

**Response:**
Returns the updated profile object (same structure as GET response).

## Profile Completion Logic

A driver profile is considered complete when all of the following fields are filled:

- `firstName`
- `lastName`
- `phoneNumber`
- `dateOfBirth`
- `address`
- `emergencyContactName`
- `emergencyContactNumber`

When a profile is complete, `profileCompletion` is automatically set to 1.

## Frontend Integration

The implementation supports the existing React frontend structure with:

### Data Mapping

- Frontend `phone` → Backend `phoneNumber`
- Frontend `emergencyContact` → Backend `emergencyContactNumber`
- Frontend `profilePicture` → Backend `profilePictureUrl`
- Frontend `maxDistance` → Backend `maximumTripDistance`

### Boolean/Integer Handling

Trip preferences are handled flexibly:

- Frontend can send boolean values (`true`/`false`)
- Backend converts to integers (1/0) for database storage
- Frontend can also send integer values directly

### Document Management

License information is stored with separate fields for:

- Image data (Base64 encoded)
- License numbers
- Expiry dates
- Upload dates
- Verification status

## Migration Instructions

1. **Backup existing data** before running migration
2. **Run the migration script**: `db_migration_driver_profiles_update.sql`
3. **Update application** with new code
4. **Test endpoints** to ensure proper functionality

## Security Considerations

- **Email validation**: Email can come from request body or session
- **Authentication**: Session validation for unauthorized access prevention
- **Data sanitization**: All input fields are validated and sanitized
- **File size limits**: Profile pictures should have size restrictions (implement in frontend)

## Performance Optimizations

- **Database indexes** created for frequently queried fields
- **Lazy loading** for large fields like images
- **Efficient queries** using JPA repository methods

## Error Handling

- **Profile not found**: Returns 404 status
- **Invalid data**: Returns 400 with error message
- **Authentication issues**: Returns 401 status
- **Server errors**: Returns 500 with error details

## Testing Recommendations

1. **Unit tests** for service layer methods
2. **Integration tests** for API endpoints
3. **Database tests** for repository operations
4. **Frontend integration tests** for complete workflow

## Future Enhancements

1. **Image compression** for profile pictures
2. **License verification automation** with external services
3. **Advanced trip preferences** (time windows, route preferences)
4. **Real-time statistics updates** from booking system
5. **Document expiry notifications** via email/SMS
