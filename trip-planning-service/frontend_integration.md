# Frontend Integration Guide - Trip Planning Service

This document provides integration details for the trip planning service API endpoints.

## Base URL
```
http://localhost:8080/v1/itinerary
```

## Authentication
Authentication is handled through the `userId` field in request bodies. No separate headers are required.

## CORS Configuration
The service is configured to accept requests from:
- `http://localhost:3000` (React development server)
- `http://127.0.0.1:3000`
- Any `localhost` origin on ports 3000-3009

## Endpoints

### 1. Create Trip Itinerary
**POST** `/initiate`

Creates a new trip plan with empty daily plans for the specified date range.

#### Request Body
```json
{
  "userId": "user123",
  "tripName": "My Amazing Trip",
  "startDate": "2024-07-15",
  "endDate": "2024-07-20",
  "arrivalTime": "10:30",
  "baseCity": "Colombo",
  "multiCityAllowed": true,
  "activityPacing": "Normal",
  "budgetLevel": "Medium",
  "preferredTerrains": ["Beach", "Mountain"],
  "preferredActivities": ["Sightseeing", "Adventure"]
}
```

#### Response (Success - 201 Created)
```json
{
  "status": "success",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "message": "Trip created successfully"
}
```

#### Response (Error - 400 Bad Request)
```json
{
  "status": "error",
  "tripId": null,
  "message": "Start date must be before or equal to end date"
}
```

### 2. Update City for Specific Day
**POST** `/{tripId}/day/{day}/city`

Updates the city for a specific day in a trip plan's daily plans.

#### Headers
```
Content-Type: application/json
```

#### Path Parameters
- `tripId`: The UUID of the trip to update
- `day`: The day number to update (1-based indexing)

#### Request Body
```json
{
  "userId": "user123",
  "city": "Kandy"
}
```

#### Response (Success - 200 OK)
```json
{
  "status": "success",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "day": 2,
  "city": "Kandy",
  "message": "City updated successfully"
}
```

#### Error Responses

**Trip Not Found (404 Not Found)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "Trip not found with ID: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Unauthorized Access (403 Forbidden)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "You are not authorized to modify this trip"
}
```

**Invalid Day Number (400 Bad Request)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "Day 10 is invalid. Trip has only 5 days"
}
```

**Validation Error (400 Bad Request)**
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": {
    "city": "City cannot be blank"
  }
}
```

### 3. Get Suggestions for Specific Day and Type
**GET** `/{tripId}/day/{day}/suggestions/{type}`

Fetches preference-based suggestions for attractions, hotels, or restaurants for a specific day in a trip. The service integrates with **TripAdvisor** and **Google Places APIs** to provide real-time suggestions based on user preferences, with Redis caching for improved performance.

#### Features
- **Real API Integration**: Primary data from TripAdvisor API (attractions) and Google Places API (hotels, restaurants)
- **Intelligent Fallback**: Falls back to Google Places if TripAdvisor is unavailable
- **Preference Matching**: Filters results based on user's preferred activities and terrains
- **Budget Filtering**: Filters hotels and restaurants by user's budget level
- **Smart Ranking**: Results sorted by preference matches, ratings, and popularity
- **Caching**: Results cached in Redis for 15 minutes to improve performance
- **Mock Fallback**: Development-friendly mock data when API keys are not configured

#### Path Parameters
- `tripId`: The UUID of the trip
- `day`: The day number (1-based indexing)
- `type`: The type of suggestions - one of: `attractions`, `hotels`, `restaurants`

#### Query Parameters
- `userId`: The user ID for ownership validation (required)

#### API Configuration Required
For real data, set these environment variables:
```bash
TRIPADVISOR_API_KEY=your-tripadvisor-content-api-key
GOOGLE_MAPS_API_KEY=your-google-places-api-key
```

#### Response (Success - 200 OK)
```json
{
  "status": "success",
  "tripId": "550e8400-e29b-41d4-a716-446655440000",
  "day": 2,
  "type": "attractions",
  "city": "Colombo",
  "cached": false,
  "dataSource": "TripAdvisor + Google Places",
  "suggestions": [
    {
      "id": "tripadvisor_123456",
      "name": "Gangaramaya Temple",
      "location": "Colombo",
      "address": "61 Sri Jinaratana Rd, Colombo 00200",
      "category": "Attraction",
      "duration": "2-3 hours",
      "price": "$5-15",
      "rating": 4.5,
      "reviews": 1847,
      "image": "https://media.tacdn.com/media/attractions-splice-spp-674x446/06/74/6c/9c.jpg",
      "description": "A beautiful Buddhist temple complex featuring serene lake views, intricate architecture, and cultural exhibits in the heart of Colombo.",
      "openHours": "6:00 AM - 10:00 PM",
      "latitude": 6.9162,
      "longitude": 79.8570,
      "distanceKm": 2.3,
      "source": "TripAdvisor",
      "googlePlaceId": "ChIJkd_Q5rN3WjMRw6nQj2nZzVs",
      "matchedActivities": ["Cultural Tours", "Photography", "Sightseeing"],
      "matchedTerrains": ["Historical", "Cultural"],
      "popularityLevel": "High",
      "isRecommended": true,
      "bookingUrl": "https://www.tripadvisor.com/Attraction_Review-g293962-d317505-Reviews-Gangaramaya_Temple-Colombo_Western_Province.html"
    },
    {
      "id": "google_ChIJbdx3wgF4WjMRhZnZzVs",
      "name": "Independence Memorial Hall",
      "location": "Colombo",
      "address": "Independence Ave, Colombo 00700",
      "category": "Attraction", 
      "duration": "1-2 hours",
      "price": "Free",
      "rating": 4.2,
      "reviews": 1256,
      "image": "https://maps.googleapis.com/maps/api/place/photo?photoreference=ATtYBwKqY4...",
      "description": "National monument commemorating Sri Lanka's independence from British rule.",
      "openHours": "8:00 AM - 6:00 PM",
      "isOpenNow": true,
      "latitude": 6.9034,
      "longitude": 79.8690,
      "distanceKm": 3.1,
      "source": "Google Places",
      "googlePlaceId": "ChIJbdx3wgF4WjMRhZnZzVs",
      "matchedActivities": ["Sightseeing", "Cultural Tours"],
      "matchedTerrains": ["Historical"],
      "popularityLevel": "High",
      "isRecommended": true
    }
  ]
}
```

#### Hotels Response Example
```json
{
  "status": "success",
  "suggestions": [
    {
      "id": "google_ChIJ8a9QwgF4WjMR...",
      "name": "Shangri-La Hotel Colombo",
      "category": "Hotel",
      "price": "$280-350/night",
      "priceLevel": "High",
      "rating": 4.6,
      "reviews": 2134,
      "image": "https://maps.googleapis.com/maps/api/place/photo?photoreference=...",
      "description": "Luxury hotel with ocean views, multiple restaurants, and world-class amenities.",
      "latitude": 6.9320,
      "longitude": 79.8441,
      "distanceKm": 1.2,
      "source": "Google Places",
      "googlePlaceId": "ChIJ8a9QwgF4WjMR...",
      "popularityLevel": "High",
      "isRecommended": true,
      "isOpenNow": true
    }
  ]
}
```

#### Restaurants Response Example
```json
{
  "status": "success",
  "suggestions": [
    {
      "id": "google_ChIJ_XdxwgF4WjMR...",
      "name": "Ministry of Crab",
      "category": "Restaurant",
      "cuisine": "Seafood",
      "priceRange": "$40-80",
      "priceLevel": "High",
      "rating": 4.7,
      "reviews": 3421,
      "image": "https://maps.googleapis.com/maps/api/place/photo?photoreference=...",
      "description": "World-renowned restaurant specializing in Sri Lankan crab dishes.",
      "openHours": "12:00 PM - 11:00 PM",
      "isOpenNow": true,
      "latitude": 6.9162,
      "longitude": 79.8570,
      "distanceKm": 0.8,
      "source": "Google Places",
      "googlePlaceId": "ChIJ_XdxwgF4WjMR...",
      "popularityLevel": "High",
      "isRecommended": true
    }
  ]
}
```

#### Error Responses

**Trip Not Found (404 Not Found)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "Trip not found with ID: 550e8400-e29b-41d4-a716-446655440000"
}
```

**Unauthorized Access (403 Forbidden)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "You are not authorized to access this trip"
}
```

**Invalid Day or Type (400 Bad Request)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "Invalid suggestion type: shopping. Must be one of: attractions, hotels, restaurants"
}
```

**City Not Set (400 Bad Request)**
```json
{
  "status": "error",
  "tripId": null,
  "message": "City not set for day 2. Please set a city first before getting suggestions."
}
```

## Frontend Usage Examples

### JavaScript/React Examples

#### 1. Create a Trip
```javascript
const createTrip = async (tripData) => {
  try {
    const response = await fetch('http://localhost:8080/v1/itinerary/initiate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(tripData)
    });

    const result = await response.json();
    
    if (response.ok) {
      console.log('Trip created:', result.tripId);
      return result;
    } else {
      console.error('Error creating trip:', result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Network error:', error);
    throw error;
  }
};
```

#### 2. Update City for a Day
```javascript
const updateCity = async (tripId, day, userId, city) => {
  try {
    const response = await fetch(`http://localhost:8080/v1/itinerary/${tripId}/day/${day}/city`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ userId, city })
    });

    const result = await response.json();
    
    if (response.ok) {
      console.log('City updated successfully:', result);
      return result;
    } else {
      console.error('Error updating city:', result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Network error:', error);
    throw error;
  }
};

// Usage
updateCity('550e8400-e29b-41d4-a716-446655440000', 2, 'user123', 'Kandy')
  .then(result => {
    // Handle success
    console.log(`Day ${result.day} city updated to ${result.city}`);
  })
  .catch(error => {
    // Handle error
    console.error('Failed to update city:', error.message);
  });
```

#### 3. Get Suggestions for a Day (Real API Integration)
```javascript
const getSuggestions = async (tripId, day, type, userId) => {
  try {
    const response = await fetch(`http://localhost:8080/v1/itinerary/${tripId}/day/${day}/suggestions/${type}?userId=${userId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    const result = await response.json();
    
    if (response.ok) {
      console.log('Suggestions retrieved successfully:', result);
      console.log(`Data source: ${result.dataSource}, Cached: ${result.cached}`);
      return result.suggestions;
    } else {
      console.error('Error getting suggestions:', result.message);
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('Network error:', error);
    throw error;
  }
};

// Usage examples with real API data
getSuggestions('550e8400-e29b-41d4-a716-446655440000', 2, 'attractions', 'user123')
  .then(suggestions => {
    console.log(`Found ${suggestions.length} attractions for day 2`);
    suggestions.forEach(suggestion => {
      console.log(`${suggestion.name} - Rating: ${suggestion.rating}/5 (${suggestion.reviews} reviews)`);
      console.log(`  Source: ${suggestion.source}`);
      console.log(`  Distance: ${suggestion.distanceKm?.toFixed(1)}km`);
      console.log(`  Matched preferences: ${suggestion.matchedActivities?.join(', ')}`);
      console.log(`  Price: ${suggestion.price}`);
      console.log(`  Recommended: ${suggestion.isRecommended ? 'Yes' : 'No'}`);
      console.log('---');
    });
  })
  .catch(error => {
    console.error('Failed to get suggestions:', error.message);
  });

// Get hotel suggestions with budget filtering
getSuggestions('550e8400-e29b-41d4-a716-446655440000', 1, 'hotels', 'user123')
  .then(hotels => {
    console.log('Available hotels:');
    hotels.forEach(hotel => {
      console.log(`${hotel.name} - ${hotel.price} (${hotel.priceLevel} budget)`);
      console.log(`  Rating: ${hotel.rating}/5 stars`);
      console.log(`  Distance: ${hotel.distanceKm?.toFixed(1)}km from city center`);
      if (hotel.isOpenNow !== undefined) {
        console.log(`  Currently: ${hotel.isOpenNow ? 'Open' : 'Closed'}`);
      }
    });
  });

// Get restaurant suggestions with cuisine info
getSuggestions('550e8400-e29b-41d4-a716-446655440000', 3, 'restaurants', 'user123')
  .then(restaurants => {
    console.log('Restaurant options:');
    restaurants.forEach(restaurant => {
      console.log(`${restaurant.name} - ${restaurant.cuisine || 'International'}`);
      console.log(`  Price range: ${restaurant.priceRange}`);
      console.log(`  Rating: ${restaurant.rating}/5 (${restaurant.reviews} reviews)`);
      console.log(`  Hours: ${restaurant.openHours || 'Hours not available'}`);
      if (restaurant.isOpenNow !== undefined) {
        console.log(`  Currently: ${restaurant.isOpenNow ? 'Open' : 'Closed'}`);
      }
    });
  });

// Advanced usage with error handling and loading states
const SuggestionsComponent = ({ tripId, day, type, userId }) => {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [dataSource, setDataSource] = useState('');

  const loadSuggestions = useCallback(async () => {
    if (!tripId || !day || !type || !userId) return;

    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`http://localhost:8080/v1/itinerary/${tripId}/day/${day}/suggestions/${type}?userId=${userId}`);
      const result = await response.json();

      if (response.ok) {
        setSuggestions(result.suggestions || []);
        setDataSource(result.dataSource || 'Unknown');
        console.log(`Loaded ${result.suggestions?.length || 0} suggestions from ${result.dataSource}`);
      } else {
        throw new Error(result.message || 'Failed to load suggestions');
      }
    } catch (err) {
      setError(err.message);
      console.error('Error loading suggestions:', err);
    } finally {
      setLoading(false);
    }
  }, [tripId, day, type, userId]);

  useEffect(() => {
    loadSuggestions();
  }, [loadSuggestions]);

  if (loading) return <div>Loading suggestions...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h3>{type.charAt(0).toUpperCase() + type.slice(1)} Suggestions</h3>
      <p>Data from: {dataSource}</p>
      {suggestions.map(suggestion => (
        <div key={suggestion.id} className="suggestion-card">
          <h4>{suggestion.name}</h4>
          <p>{suggestion.description}</p>
          <div className="rating">
            Rating: {suggestion.rating}/5 ({suggestion.reviews} reviews)
          </div>
          <div className="price">Price: {suggestion.price || suggestion.priceRange}</div>
          {suggestion.matchedActivities?.length > 0 && (
            <div className="matches">
              Matches your interests: {suggestion.matchedActivities.join(', ')}
            </div>
          )}
          {suggestion.bookingUrl && (
            <a href={suggestion.bookingUrl} target="_blank" rel="noopener noreferrer">
              View Details
            </a>
          )}
        </div>
      ))}
    </div>
  );
};
```

#### 4. Error Handling in React Component
```javascript
const TripDayEditor = ({ tripId, day, userId }) => {
  const [city, setCity] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  const handleUpdateCity = async () => {
    if (!city.trim()) {
      setError('City cannot be empty');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const result = await updateCity(tripId, day, userId, city.trim());
      // Handle success - maybe show a toast notification
      console.log('City updated successfully');
    } catch (error) {
      setError(error.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div>
      <input
        type="text"
        value={city}
        onChange={(e) => setCity(e.target.value)}
        placeholder="Enter city name"
        disabled={isLoading}
      />
      <button onClick={handleUpdateCity} disabled={isLoading}>
        {isLoading ? 'Updating...' : 'Update City'}
      </button>
      {error && <div className="error">{error}</div>}
    </div>
  );
};
```

#### 5. Error Handling with Suggestions
```javascript
const SuggestionsComponent = ({ tripId, day, userId }) => {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [selectedType, setSelectedType] = useState('attractions');

  const loadSuggestions = async (type) => {
    setLoading(true);
    setError('');
    
    try {
      const result = await getSuggestions(tripId, day, type, userId);
      setSuggestions(result);
      setSelectedType(type);
    } catch (error) {
      setError(error.message);
      setSuggestions([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSuggestions('attractions');
  }, [tripId, day, userId]);

  return (
    <div>
      <div className="suggestion-tabs">
        {['attractions', 'hotels', 'restaurants'].map(type => (
          <button 
            key={type}
            onClick={() => loadSuggestions(type)}
            className={selectedType === type ? 'active' : ''}
            disabled={loading}
          >
            {type.charAt(0).toUpperCase() + type.slice(1)}
          </button>
        ))}
      </div>
      
      {loading && <div className="loading">Loading suggestions...</div>}
      {error && <div className="error">Error: {error}</div>}
      
      <div className="suggestions-grid">
        {suggestions.map(suggestion => (
          <div key={suggestion.id} className="suggestion-card">
            <img src={suggestion.image} alt={suggestion.name} />
            <h3>{suggestion.name}</h3>
            <p>{suggestion.description}</p>
            <div className="rating">
              ⭐ {suggestion.rating} ({suggestion.reviews} reviews)
            </div>
            {suggestion.price && (
              <div className="price">{suggestion.price}</div>
            )}
            {suggestion.duration && (
              <div className="duration">Duration: {suggestion.duration}</div>
            )}
            {suggestion.cuisine && (
              <div className="cuisine">Cuisine: {suggestion.cuisine}</div>
            )}
            {suggestion.isRecommended && (
              <div className="recommended-badge">Recommended</div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};
```

## Testing with curl

### Create a Trip
```bash
curl -X POST http://localhost:8080/v1/itinerary/initiate \
  -H "Content-Type: application/json" \
  -H "X-User-Id: user123" \
  -d '{
    "userId": "user123",
    "tripName": "Test Trip",
    "startDate": "2024-07-15",
    "endDate": "2024-07-17",
    "baseCity": "Colombo"
  }'
```

### Update City for Day 2
```bash
curl -X POST http://localhost:8080/v1/itinerary/550e8400-e29b-41d4-a716-446655440000/day/2/city \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "city": "Kandy"}'
```

### Get Suggestions for Day 2 Attractions
```bash
curl -X GET "http://localhost:8080/v1/itinerary/550e8400-e29b-41d4-a716-446655440000/day/2/suggestions/attractions?userId=user123" \
  -H "Content-Type: application/json"
```

### Get Hotel Suggestions for Day 1
```bash
curl -X GET "http://localhost:8080/v1/itinerary/550e8400-e29b-41d4-a716-446655440000/day/1/suggestions/hotels?userId=user123" \
  -H "Content-Type: application/json"
```

### Get Restaurant Suggestions for Day 3
```bash
curl -X GET "http://localhost:8080/v1/itinerary/550e8400-e29b-41d4-a716-446655440000/day/3/suggestions/restaurants?userId=user123" \
  -H "Content-Type: application/json"
```

## Notes

1. **User ID**: Always include the `userId` field in request bodies or query parameters for authentication and authorization.

2. **CORS**: The service is configured for local development. For production, update the CORS configuration accordingly.

3. **Day Indexing**: Day numbers are 1-based (day 1, day 2, etc.), not 0-based.

4. **Error Handling**: Always check the HTTP status code and the `status` field in the response.

5. **Validation**: Both `userId` and `city` fields in update requests cannot be blank or empty.

6. **Database Updates**: The update endpoint uses MongoDB array updates for efficient partial document updates.

7. **Timestamps**: The `lastUpdated` timestamp is automatically updated when a city is changed.

8. **User Selection Flag**: When a city is updated, the `userSelected` flag for that day is automatically set to `true`.

9. **Caching**: Suggestions are cached for 15 minutes in Redis to improve performance. Cache keys include trip ID, day, type, and city.

10. **Suggestions**: Before getting suggestions, ensure the city is set for the specific day using the update city endpoint.

11. **External APIs**: The service integrates with TripAdvisor Content API and Google Places API for real-time data. If API keys are not configured, development-friendly mock data will be returned.

12. **Preferences**: Suggestions are filtered and ranked based on user preferences (activities, terrains, budget level) set during trip creation.

13. **Sorting**: Suggestions are sorted by preference matches, recommendation status, rating, and distance from city center.

## API Configuration

### Environment Variables Required

For production use with real data, configure these environment variables:

```bash
# TripAdvisor Content API (Primary for attractions)
TRIPADVISOR_API_KEY=your-tripadvisor-content-api-key

# Google Places API (Hotels, restaurants, and attraction fallback)
GOOGLE_MAPS_API_KEY=your-google-places-api-key

# Redis Configuration (for caching)
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=redis123
```

### Getting API Keys

#### TripAdvisor Content API
1. Visit [TripAdvisor Content API](https://developer-tripadvisor.com/content-api/)
2. Sign up for developer account
3. Create a new application
4. Copy the API key

#### Google Places API
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Places API
4. Create credentials (API Key)
5. Restrict the key to Places API for security

### Development Mode

When API keys are not configured or set to "demo-key", the service automatically uses mock data that simulates real API responses for development and testing purposes.

## Testing the API

### Using cURL

```bash
# Test with real APIs (requires valid keys)
curl -X GET "http://localhost:8083/api/trip/550e8400-e29b-41d4-a716-446655440000/day/2/suggestions/attractions?userId=user123" \
  -H "Content-Type: application/json"

# Test different types
curl -X GET "http://localhost:8083/api/trip/550e8400-e29b-41d4-a716-446655440000/day/1/suggestions/hotels?userId=user123"

curl -X GET "http://localhost:8083/api/trip/550e8400-e29b-41d4-a716-446655440000/day/3/suggestions/restaurants?userId=user123"
```

### Response Analysis

Check the response for these indicators:

```json
{
  "dataSource": "TripAdvisor + Google Places",  // Real APIs
  "cached": false,                             // Fresh from API
  "suggestions": [
    {
      "source": "TripAdvisor",                 // Data source per suggestion
      "id": "tripadvisor_123456",              // Real TripAdvisor ID
      "googlePlaceId": "ChIJ...",              // Real Google Place ID
      "matchedActivities": [...],              // User preference matches
      "isRecommended": true                    // Smart recommendation
    }
  ]
}
```

### Performance Monitoring

Monitor these aspects:

1. **Response Time**: Should be <2 seconds for cached results, <10 seconds for fresh API calls
2. **Cache Hit Rate**: Monitor Redis cache hit rate for optimization
3. **API Failures**: Handle graceful fallbacks when external APIs are unavailable
4. **Rate Limiting**: Be aware of API rate limits (TripAdvisor: 500 requests/hour, Google Places: varies by plan)

### Error Scenarios to Test

1. **Invalid API Keys**: Should fall back to mock data
2. **API Rate Limits**: Should handle gracefully with cached data or mock fallback
3. **Network Failures**: Should not crash the application
4. **Invalid City Names**: Should return empty results with proper error messages
5. **Missing Trip Data**: Should return appropriate 404 errors