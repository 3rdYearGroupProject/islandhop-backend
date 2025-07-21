# Driver Profile API Test Script

## Test Cases

### 1. Get Driver Profile (Empty Profile)

```bash
curl -X GET "http://localhost:8080/driver/profile?email=test@example.com" \
  -H "Content-Type: application/json"
```

**Expected Response:**

- Status: 404 (if profile doesn't exist) or 200 with minimal data

### 2. Update Driver Profile (Complete Profile)

```bash
curl -X PUT "http://localhost:8080/driver/profile" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+94771234567",
    "dateOfBirth": "1990-05-15",
    "address": "123 Main Street, Colombo 03",
    "emergencyContactName": "Jane Doe",
    "emergencyContact": "+94771234568",
    "profilePicture": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD...",
    "acceptPartialTrips": true,
    "autoAcceptTrips": false,
    "maxDistance": 200
  }'
```

**Expected Response:**

- Status: 200
- Profile completion: 1
- All fields properly saved

### 3. Get Driver Profile (Complete Profile)

```bash
curl -X GET "http://localhost:8080/driver/profile?email=test@example.com" \
  -H "Content-Type: application/json"
```

**Expected Response:**

```json
{
  "email": "test@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+94771234567",
  "dateOfBirth": "1990-05-15",
  "address": "123 Main Street, Colombo 03",
  "emergencyContactName": "Jane Doe",
  "emergencyContactNumber": "+94771234568",
  "acceptPartialTrips": 1,
  "autoAcceptTrips": 0,
  "maximumTripDistance": 200,
  "profileCompletion": 1,
  "rating": 0.0,
  "numberOfReviews": 0,
  "totalCompletedTrips": 0
}
```

### 4. Partial Update Test

```bash
curl -X PUT "http://localhost:8080/driver/profile" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "acceptPartialTrips": 0,
    "maxDistance": 150
  }'
```

**Expected Response:**

- Status: 200
- Only specified fields updated
- Profile completion remains 1

### 5. Frontend Data Format Test

```bash
curl -X PUT "http://localhost:8080/driver/profile" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "frontend@example.com",
    "firstName": "Frontend",
    "lastName": "User",
    "phone": "+94777777777",
    "dateOfBirth": "1995-08-20",
    "address": "456 Frontend Street",
    "emergencyContactName": "Emergency Contact",
    "emergencyContact": "+94778888888",
    "acceptPartialTrips": 1,
    "autoAcceptTrips": 0,
    "maxDistance": "250"
  }'
```

**Expected Response:**

- Status: 200
- Boolean/integer conversion works correctly
- String numbers parsed correctly

## Database Verification Queries

### Check Profile Data

```sql
SELECT
  email,
  first_name,
  last_name,
  phone_number,
  date_of_birth,
  address,
  emergency_contact_name,
  emergency_contact_number,
  accept_partial_trips,
  auto_accept_trips,
  maximum_trip_distance,
  profile_completion,
  rating,
  number_of_reviews,
  total_completed_trips
FROM driver_profiles
WHERE email = 'test@example.com';
```

### Check Profile Completion Logic

```sql
-- Should show profiles with completion status
SELECT
  email,
  CASE
    WHEN first_name IS NOT NULL AND last_name IS NOT NULL
         AND phone_number IS NOT NULL AND date_of_birth IS NOT NULL
         AND address IS NOT NULL AND emergency_contact_name IS NOT NULL
         AND emergency_contact_number IS NOT NULL
    THEN 'Complete'
    ELSE 'Incomplete'
  END AS calculated_status,
  profile_completion as stored_status
FROM driver_profiles;
```

## Performance Test

### Load Test with Multiple Requests

```bash
# Create test script for multiple concurrent requests
for i in {1..10}; do
  curl -X GET "http://localhost:8080/driver/profile?email=test$i@example.com" &
done
wait
```

## Error Handling Tests

### Invalid Email Format

```bash
curl -X PUT "http://localhost:8080/driver/profile" \
  -H "Content-Type: application/json" \
  -d '{"email": "invalid-email", "firstName": "Test"}'
```

### Missing Required Fields

```bash
curl -X PUT "http://localhost:8080/driver/profile" \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```

### Invalid Date Format

```bash
curl -X PUT "http://localhost:8080/driver/profile" \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "dateOfBirth": "invalid-date"}'
```

## Integration Test with Frontend

1. **Setup Frontend:** Ensure React app is running on localhost:3000
2. **Login as Driver:** Complete driver authentication flow
3. **Navigate to Profile:** Go to driver profile page
4. **Test CRUD Operations:**
   - View profile (GET request)
   - Edit profile (PUT request)
   - Save changes (PUT request)
   - Verify updates (GET request)

## Expected Logs

When testing, look for these log entries:

```
INFO  - PUT /driver/profile called with body: {...}
INFO  - Driver profile updated for: test@example.com
INFO  - Profile completion status set to 1 for email: test@example.com
```
