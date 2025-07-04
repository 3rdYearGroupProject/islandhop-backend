# Location Search Testing Examples
# Use these examples to test the enhanced location search functionality

## 1. Search for popular Sri Lankan attractions

### Search for Sigiriya
curl -X GET "http://localhost:8081/trip/search-locations?query=Sigiriya&maxResults=5" \
  -H "Content-Type: application/json" \
  -b cookies.txt

### Search for Temple of the Tooth in Kandy
curl -X GET "http://localhost:8081/trip/search-locations?query=Temple%20of%20the%20Tooth&city=Kandy&maxResults=3" \
  -H "Content-Type: application/json" \
  -b cookies.txt

### Search near Colombo with location bias
curl -X GET "http://localhost:8081/trip/search-locations?query=museum&biasLat=6.9271&biasLng=79.8612&maxResults=10" \
  -H "Content-Type: application/json" \
  -b cookies.txt

## 2. Validate place information

### Validate a place with coordinates
curl -X POST "http://localhost:8081/trip/validate-place" \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "placeName": "Galle Dutch Fort",
    "city": "Galle",
    "description": "Historic Dutch colonial fort",
    "latitude": 6.0329,
    "longitude": 80.2168
  }'

### Validate a place without coordinates (will search and suggest)
curl -X POST "http://localhost:8081/trip/validate-place" \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "placeName": "Adam Peak",
    "city": "Hatton",
    "description": "Sacred mountain pilgrimage site"
  }'

### Validate with potentially incorrect coordinates
curl -X POST "http://localhost:8081/trip/validate-place" \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "placeName": "Ella Rock",
    "city": "Ella",
    "latitude": 40.7128,
    "longitude": -74.0060
  }'

## 3. Get detailed place information

### Get details for a Google Place ID (example)
curl -X GET "http://localhost:8081/trip/place-details/ChIJkxHhCHm2-lsRVmSAlbdJz0I" \
  -H "Content-Type: application/json" \
  -b cookies.txt

## 4. Test location search with different queries

### Search for beaches
curl -X GET "http://localhost:8081/trip/search-locations?query=beach&maxResults=8" \
  -H "Content-Type: application/json" \
  -b cookies.txt

### Search for hotels in Colombo
curl -X GET "http://localhost:8081/trip/search-locations?query=hotel&city=Colombo&maxResults=10" \
  -H "Content-Type: application/json" \
  -b cookies.txt

### Search for restaurants near Kandy
curl -X GET "http://localhost:8081/trip/search-locations?query=restaurant&city=Kandy&biasLat=7.2906&biasLng=80.6337&maxResults=6" \
  -H "Content-Type: application/json" \
  -b cookies.txt

## 5. Test edge cases

### Search with ambiguous query
curl -X GET "http://localhost:8081/trip/search-locations?query=temple&maxResults=15" \
  -H "Content-Type: application/json" \
  -b cookies.txt

### Search with non-Sri Lankan query (should still filter to Sri Lanka)
curl -X GET "http://localhost:8081/trip/search-locations?query=Eiffel%20Tower&maxResults=5" \
  -H "Content-Type: application/json" \
  -b cookies.txt

### Validate place with coordinates outside Sri Lanka
curl -X POST "http://localhost:8081/trip/validate-place" \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "placeName": "Invalid Location",
    "latitude": 51.5074,
    "longitude": -0.1278
  }'

## Expected Behaviors:

1. **Location Search**:
   - Returns results filtered to Sri Lankan locations
   - Sorts by distance if bias coordinates provided
   - Combines Google Places and TripAdvisor results
   - Handles both specific and general queries

2. **Place Validation**:
   - Validates coordinates are within Sri Lanka bounds
   - Suggests coordinates for places without them
   - Provides formatted addresses using Google's geocoding
   - Warns about potential issues (multiple matches, invalid coordinates)

3. **Error Handling**:
   - Gracefully handles API failures with fallback to mock data
   - Provides meaningful error messages
   - Validates session before processing requests

4. **Performance**:
   - Caches frequently searched locations (if implemented)
   - Combines multiple API calls efficiently
   - Filters duplicates across different data sources

## Notes:
- Make sure to have a valid session cookie in cookies.txt
- Replace example coordinates with actual locations for testing
- API keys should be configured in environment variables
- Mock data will be returned if API keys are not configured
