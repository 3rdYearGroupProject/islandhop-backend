# Complete Data Flow Architecture - Trip Planning System

## Overview
This document explains the complete data flow and how data is retrieved from different APIs and services in the trip planning system. The system uses a hybrid approach combining local data storage with external API integrations.

## System Architecture

### Core Components
1. **Controllers**: Handle HTTP requests and responses
2. **Services**: Business logic and data processing
3. **Repositories**: Data persistence layer (MongoDB)
4. **External APIs**: Third-party data sources
5. **DTOs**: Data transfer objects for API communication

### Data Sources
- **MongoDB**: Primary data storage for trips, user preferences, and cached data
- **Google Places API**: Location search, place details, and geocoding
- **TripAdvisor API**: Additional place information and reviews
- **OpenStreetMap/Nominatim**: Backup geocoding service
- **Route Optimization Services**: Travel time and route calculations

## Complete Data Flow Walkthrough

### 1. Trip Creation Flow (`POST /trip/initiate`)

#### Request Flow:
```
Frontend → TripPlanningController → TripPlanningService → TripRepository → MongoDB
```

#### Data Processing:
1. **Controller Layer** (`TripPlanningController.initiateTrip()`)
   - Receives `CreateTripRequest` with user preferences
   - Validates session and extracts userId
   - Calls service layer

2. **Service Layer** (`TripPlanningService.createTrip()`)
   - Creates new Trip entity with unique ID
   - Sets user preferences and trip metadata
   - Initializes empty collections for places and day plans
   - Saves to MongoDB

3. **Data Stored**:
   ```json
   {
     "tripId": "uuid",
     "userId": "user123",
     "tripName": "Trip to Colombo",
     "startDate": "2024-01-15",
     "endDate": "2024-01-20",
     "baseCity": "Colombo",
     "categories": ["culture", "nature"],
     "pacing": "moderate",
     "places": [],
     "dayPlans": [],
     "preferences": {
       "maxDailyTravelHours": 8,
       "bufferTimeMinutes": 30
     }
   }
   ```

### 2. Location Search Flow (`GET /trip/search-locations`)

#### Multi-Source Data Retrieval:
```
Frontend → TripPlanningController → LocationService → [GooglePlacesService + TripAdvisorService] → External APIs
```

#### Data Processing:
1. **Primary Source - Google Places API**
   ```java
   // LocationService.searchLocations()
   List<GooglePlacesService.LocationSearchResult> googleResults = 
       googlePlacesService.searchPlacesByText(fullQuery, biasLat, biasLng, maxResults);
   ```

2. **Secondary Source - TripAdvisor API**
   ```java
   // Fallback if Google results are insufficient
   if (results.size() < maxResults) {
       List<PlannedPlace> tripAdvisorResults = tripAdvisorService.searchByName(query, city);
   }
   ```

3. **Data Enrichment Process**:
   - Combine results from multiple sources
   - Validate Sri Lankan locations
   - Remove duplicates
   - Enhance with additional metadata

4. **Response Structure**:
   ```json
   {
     "results": [
       {
         "placeId": "google_place_id",
         "name": "Temple of the Tooth",
         "address": "Sri Dalada Veediya, Kandy",
         "latitude": 7.2906,
         "longitude": 80.6337,
         "rating": 4.6,
         "categories": ["temple", "cultural"],
         "photoUrl": "https://...",
         "source": "google"
       }
     ],
     "count": 10,
     "query": "temple kandy"
   }
   ```

### 3. Add Place to Trip Flow (`POST /trip/{tripId}/add-place`)

#### Data Integration Flow:
```
Frontend → TripPlanningController → TripPlanningService → PlaceService → [External APIs] → TripRepository
```

#### Data Processing:
1. **Place Validation & Enrichment**
   ```java
   // PlaceService.createPlannedPlace()
   PlannedPlace place = placeService.createPlannedPlace(request);
   ```

2. **External Data Retrieval**:
   - Google Places API: Get detailed place information
   - TripAdvisor API: Get reviews and additional details
   - Geocoding services: Validate coordinates

3. **Trip Data Update**:
   ```java
   // Add to existing trip
   trip.getPlaces().add(place);
   trip.setUpdatedAt(LocalDateTime.now());
   Trip savedTrip = tripRepository.save(trip);
   ```

4. **Complete Place Data Structure**:
   ```json
   {
     "placeId": "place_123",
     "name": "Sigiriya Rock Fortress",
     "address": "Sigiriya, Sri Lanka",
     "latitude": 7.9570,
     "longitude": 80.7603,
     "category": "historical",
     "rating": 4.7,
     "estimatedDuration": "3 hours",
     "openingHours": "7:00 AM - 5:30 PM",
     "ticketPrice": "LKR 4500",
     "userAdded": true,
     "dayNumber": null,
     "visitOrder": 0,
     "travelTimeFromPrevious": 0
   }
   ```

### 4. Day Planning Flow (`GET /trip/{tripId}/day/{day}`)

#### Smart Data Assembly:
```
Frontend → TripPlanningController → TripPlanningService → [Multiple Services] → Aggregated Response
```

#### Data Sources Combined:
1. **Trip Data from MongoDB**
   ```java
   Trip trip = getTripByIdAndUserId(tripId, userId);
   ```

2. **Day-Specific Processing**
   ```java
   // TripPlanningService.getDayPlan()
   DayPlan dayPlan = generateDayPlan(trip, day);
   ```

3. **External Data Integration**:
   - **Travel Time Calculation**: Route optimization services
   - **Weather Data**: Weather APIs
   - **Real-time Updates**: Place availability, traffic conditions

4. **Complete Day Plan Response**:
   ```json
   {
     "dayNumber": 2,
     "date": "2024-01-16",
     "places": [
       {
         "placeId": "place_123",
         "name": "Dambulla Cave Temple",
         "visitTime": "09:00",
         "estimatedDuration": "2 hours",
         "travelTimeFromPrevious": "45 minutes",
         "transportMode": "car"
       }
     ],
     "totalTravelTime": "3 hours",
     "estimatedCost": "LKR 12000",
     "suggestions": [...],
     "weatherForecast": {...}
   }
   ```

### 5. AI Suggestions Flow (`GET /trip/{tripId}/suggestions`)

#### Multi-Algorithm Data Processing:
```
Frontend → TripPlanningController → TripPlanningService → RecommendationEngine → [ML Models + External APIs]
```

#### Data Processing Pipeline:
1. **Context Analysis**
   ```java
   // RecommendationEngine.recommendAttractions()
   Trip trip = getCurrentTrip();
   UserPreferences preferences = extractPreferences(trip);
   LocationContext context = analyzeLocation(trip.getBaseCity());
   ```

2. **Multi-Source Recommendation**:
   - **Collaborative Filtering**: Based on similar user trips
   - **Content-Based**: Based on place categories and user preferences
   - **Location-Based**: Based on proximity and travel patterns
   - **Temporal**: Based on time of day, season, weather

3. **External Data Integration**:
   ```java
   // Combine multiple data sources
   List<Recommendation> attractions = recommendationEngine.recommendAttractions(trip, day);
   List<Recommendation> hotels = recommendationEngine.recommendHotels(trip, day);
   List<Recommendation> restaurants = recommendationEngine.recommendRestaurants(trip, day);
   ```

4. **Response Structure**:
   ```json
   {
     "attractions": [
       {
         "placeId": "rec_123",
         "name": "Royal Botanical Gardens",
         "confidence": 0.89,
         "reasoning": "Matches your nature preference",
         "category": "botanical",
         "estimatedDuration": "2-3 hours",
         "bestTimeToVisit": "morning"
       }
     ],
     "hotels": [...],
     "restaurants": [...],
     "insights": [
       "Consider visiting gardens in the morning for better weather"
     ]
   }
   ```

### 6. Route Optimization Flow (`POST /trip/{tripId}/optimize-order`)

#### Complex Algorithm Integration:
```
Frontend → TripPlanningController → TripPlanningService → RouteOptimizationService → [TSP Algorithm + External APIs]
```

#### Data Processing:
1. **Distance Matrix Calculation**
   ```java
   // RouteOptimizationService.optimizeRoute()
   double[][] distanceMatrix = calculateDistanceMatrix(places);
   ```

2. **External API Calls**:
   - **Google Maps Distance Matrix API**: Real travel times
   - **OpenStreetMap**: Alternative routing data
   - **Traffic APIs**: Real-time traffic conditions

3. **Algorithm Application**:
   ```java
   // Apply Traveling Salesman Problem (TSP) algorithm
   List<PlannedPlace> optimizedOrder = tspSolver.solve(distanceMatrix, constraints);
   ```

4. **Trip Update**:
   ```java
   // Update trip with optimized order
   trip.setPlaces(optimizedOrder);
   updateDayPlans(trip);
   ```

### 7. Trip Summary Flow (`GET /trip/{tripId}/summary`)

#### Comprehensive Data Aggregation:
```
Frontend → TripPlanningController → TripPlanningService → [Multiple Data Sources] → Aggregated Summary
```

#### Data Assembly:
1. **Core Trip Data** (from MongoDB)
2. **Real-time Enhancements**:
   - Current weather conditions
   - Place availability status
   - Updated pricing information
   - Traffic conditions

3. **Summary Response**:
   ```json
   {
     "tripId": "trip_123",
     "tripName": "Sri Lanka Adventure",
     "totalDays": 5,
     "totalPlaces": 12,
     "estimatedCost": "LKR 150000",
     "totalTravelTime": "18 hours",
     "status": "PLANNING",
     "dayBreakdown": [
       {
         "day": 1,
         "placesCount": 3,
         "travelTime": "4 hours",
         "highlights": ["Colombo Fort", "Galle Face Green"]
       }
     ],
     "recommendations": [...],
     "weatherOverview": {...}
   }
   ```

## Data Persistence Strategy

### MongoDB Collections:
1. **trips**: Complete trip documents with embedded places and day plans
2. **users**: User preferences and travel history
3. **places_cache**: Cached place data from external APIs
4. **recommendations_cache**: Cached ML recommendation results

### Caching Strategy:
- **L1 Cache**: In-memory caching for frequently accessed data
- **L2 Cache**: Redis for session-based data
- **L3 Cache**: MongoDB for persistent caching of external API data

## External API Integration Details

### Google Places API Integration:
```java
// GooglePlacesService
public List<LocationSearchResult> searchPlacesByText(String query, Double lat, Double lng, Integer maxResults) {
    // Build Google Places API request
    String url = buildPlacesSearchUrl(query, lat, lng, maxResults);
    
    // Execute HTTP request
    ResponseEntity<GooglePlacesResponse> response = restTemplate.getForEntity(url, GooglePlacesResponse.class);
    
    // Process and enrich results
    return processGoogleResults(response.getBody());
}
```

### TripAdvisor API Integration:
```java
// TripAdvisorService
public List<PlannedPlace> searchByName(String name, String city) {
    // Construct TripAdvisor search
    TripAdvisorSearchRequest request = buildSearchRequest(name, city);
    
    // Execute and process
    return processTripAdvisorResults(request);
}
```

### Error Handling and Fallbacks:
1. **Primary API Failure**: Automatic fallback to secondary APIs
2. **Rate Limiting**: Implement backoff strategies and caching
3. **Data Validation**: Validate all external data before storage
4. **Offline Mode**: Use cached data when APIs are unavailable

## Performance Optimizations

### Data Retrieval Optimizations:
1. **Batch API Calls**: Combine multiple requests where possible
2. **Parallel Processing**: Execute independent API calls in parallel
3. **Smart Caching**: Cache frequently requested data with TTL
4. **Lazy Loading**: Load detailed data only when requested

### Database Optimizations:
1. **Indexing**: Proper indexes on frequently queried fields
2. **Aggregation Pipelines**: Use MongoDB aggregation for complex queries
3. **Connection Pooling**: Efficient database connection management
4. **Query Optimization**: Minimize database round trips

## Security and Data Privacy

### API Security:
1. **API Key Management**: Secure storage of external API keys
2. **Rate Limiting**: Implement request rate limiting
3. **Input Validation**: Validate all user inputs before processing
4. **HTTPS**: All external API calls use HTTPS

### Data Privacy:
1. **User Data**: Minimal storage of personal information
2. **Location Privacy**: Anonymize location data where possible
3. **GDPR Compliance**: Data deletion and export capabilities
4. **Audit Logging**: Track all data access and modifications

## Monitoring and Analytics

### System Monitoring:
- API response times and error rates
- Database query performance
- External API availability and performance
- User behavior and system usage patterns

### Data Quality:
- External API data validation
- Duplicate detection and removal
- Data freshness monitoring
- Accuracy verification through user feedback

This architecture ensures robust, scalable, and efficient data flow throughout the trip planning system while maintaining high data quality and user experience.

## Multi-City/Province Trip Support

### Yes! Multi-City Capability
Your system **fully supports multi-city and multi-province trips** within a single trip planning session. Here's how:

#### 1. **Trip Configuration**
```java
// CreateTripRequest.java
private boolean multiCity = false;  // Can be set to true for multi-city trips
private String baseCity;            // Starting city (e.g., "Colombo")
```

#### 2. **Multi-City Trip Example**
```json
{
  "tripName": "Sri Lanka Grand Tour",
  "startDate": "2024-01-15",
  "endDate": "2024-01-25",
  "baseCity": "Colombo",
  "multiCity": true,
  "categories": ["culture", "nature", "historical"],
  "pacing": "NORMAL"
}
```

#### 3. **How Multi-City Works in Practice**

**Day-by-Day Multi-City Planning:**
```json
{
  "day": 1,
  "city": "Colombo",
  "places": [
    {"name": "Gangaramaya Temple", "city": "Colombo"},
    {"name": "Galle Face Green", "city": "Colombo"}
  ]
},
{
  "day": 2,
  "city": "Kandy", 
  "places": [
    {"name": "Temple of the Tooth", "city": "Kandy"},
    {"name": "Royal Botanical Gardens", "city": "Peradeniya"}
  ]
},
{
  "day": 3,
  "city": "Galle",
  "places": [
    {"name": "Galle Fort", "city": "Galle"},
    {"name": "Unawatuna Beach", "city": "Unawatuna"}
  ]
}
```

#### 4. **Smart Location Search Across Cities**
```java
// LocationService handles multi-city searches
public List<LocationSearchResult> searchLocations(String query, String city, 
                                                 Double biasLat, Double biasLng, Integer maxResults) {
    // Can search in any city: "temple kandy", "beach galle", "fort colombo"
    List<LocationSearchResult> results = googlePlacesService.searchPlacesByText(
        buildSearchQuery(query, city), biasLat, biasLng, maxResults);
}
```

#### 5. **Multi-Province Coverage Examples**

**Western Province (Colombo):**
- Colombo Fort, Pettah Market, Galle Face Green
- Mount Lavinia Beach, Kelaniya Temple

**Central Province (Kandy, Nuwara Eliya):**
- Temple of the Tooth, Botanical Gardens Peradeniya
- Tea plantations, Gregory Lake, Horton Plains

**Southern Province (Galle, Matara):**
- Galle Fort, Mirissa Beach, Yala National Park
- Kataragama Temple, Tangalle Beach

**Eastern Province (Trincomalee, Batticaloa):**
- Koneswaram Temple, Nilaveli Beach
- Pasikudah Beach, Batticaloa Lagoon

#### 6. **Route Optimization Across Cities**
```java
// RouteOptimizationService handles inter-city travel
public Trip optimizeVisitingOrder(String tripId, String userId) {
    // Considers:
    // - Travel time between cities (2-6 hours)
    // - Logical geographical flow (Colombo → Kandy → Nuwara Eliya → Galle)
    // - Transportation options (train, bus, car)
    // - Accommodation locations
}
```

## Why We Use Spring WebFlux (WebClient)

### WebFlux Usage for External API Calls

Your system uses **Spring WebFlux's WebClient** specifically for **external API communications**, not for the main web framework (which uses Spring MVC). Here's why and how:

#### 1. **Hybrid Architecture: MVC + WebFlux**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>    <!-- Spring MVC for REST endpoints -->
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId> <!-- WebClient for external APIs -->
</dependency>
```

#### 2. **WebFlux Usage Examples**

**Google Places API Calls:**
```java
@Service
public class GooglePlacesService {
    private final WebClient webClient;  // Non-blocking HTTP client
    
    public List<LocationSearchResult> searchPlacesByText(String query, Double biasLat, Double biasLng) {
        try {
            // Non-blocking external API call
            Mono<Map> responseMono = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/textsearch/json")
                            .queryParam("query", query)
                            .queryParam("key", apiKey)
                            .queryParam("location", biasLat + "," + biasLng)
                            .queryParam("radius", "50000")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class);
                    
            // Block only when needed for synchronous response
            Map response = responseMono.block(Duration.ofSeconds(10));
            return processGoogleResults(response);
        } catch (Exception e) {
            log.error("Google Places API error: {}", e.getMessage());
            return fallbackToMockData(query);
        }
    }
}
```

**TripAdvisor API Integration:**
```java
@Service  
public class TripAdvisorService {
    private final WebClient webClient;
    
    public List<PlannedPlace> searchByName(String name, String city) {
        try {
            Mono<Map> responseMono = webClient.get()
                    .uri("/locations/search")
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", "tripadvisor1.p.rapidapi.com")
                    .retrieve()
                    .bodyToMono(Map.class);
                    
            Map response = responseMono.block(Duration.ofSeconds(15));
            return processTripAdvisorResults(response);
        } catch (Exception e) {
            return Collections.emptyList(); // Graceful fallback
        }
    }
}
```

#### 3. **Why WebFlux for External APIs?**

**Performance Benefits:**
```java
// Parallel API calls using WebFlux
public SuggestionsResponse generateSuggestions(String tripId, Integer day, String userId) {
    Trip trip = getTripByIdAndUserId(tripId, userId);
    
    // Execute multiple external API calls in parallel
    Mono<List<Recommendation>> attractionsMono = Mono.fromCallable(() -> 
        googlePlacesService.searchAttractions(trip.getBaseCity()))
        .subscribeOn(Schedulers.boundedElastic());
        
    Mono<List<Recommendation>> restaurantsMono = Mono.fromCallable(() ->
        tripAdvisorService.searchRestaurants(trip.getBaseCity()))
        .subscribeOn(Schedulers.boundedElastic());
        
    Mono<List<Recommendation>> hotelsMono = Mono.fromCallable(() ->
        googlePlacesService.searchHotels(trip.getBaseCity()))
        .subscribeOn(Schedulers.boundedElastic());
    
    // Combine results when all complete
    return Mono.zip(attractionsMono, restaurantsMono, hotelsMono)
        .map(tuple -> new SuggestionsResponse(
            tuple.getT1(), // attractions
            tuple.getT2(), // restaurants  
            tuple.getT3()  // hotels
        ))
        .block(Duration.ofSeconds(30));
}
```

#### 4. **Real-World WebFlux Benefits in Your System**

**1. Multiple API Calls in Parallel:**
```java
// Instead of sequential calls (slow):
List<Place> googlePlaces = googlePlacesService.search(query);     // 2 seconds
List<Place> tripAdvisorPlaces = tripAdvisorService.search(query); // 3 seconds
List<Place> weatherData = weatherService.get(location);           // 1 second
// Total: 6 seconds

// WebFlux parallel execution (fast):
Mono.zip(googleMono, tripAdvisorMono, weatherMono)
    .map(this::combineResults)
    .block(); // Total: 3 seconds (max of individual calls)
```

**2. Resilient External API Handling:**
```java
public Mono<List<Recommendation>> getRecommendationsWithFallback(String city) {
    return webClient.get()
        .uri("/api/recommendations?city=" + city)
        .retrieve()
        .bodyToFlux(Recommendation.class)
        .collectList()
        .timeout(Duration.ofSeconds(10))                    // Timeout protection
        .retry(2)                                          // Automatic retry
        .onErrorReturn(Collections.emptyList())            // Graceful fallback
        .doOnError(e -> log.warn("API call failed: {}", e.getMessage()));
}
```

**3. Memory Efficient Streaming:**
```java
// For large datasets (like processing many places)
public Flux<PlaceDetails> getPlaceDetailsStream(List<String> placeIds) {
    return Flux.fromIterable(placeIds)
        .flatMap(placeId -> 
            webClient.get()
                .uri("/place/details/" + placeId)
                .retrieve()
                .bodyToMono(PlaceDetails.class)
                .onErrorReturn(PlaceDetails.empty())
        )
        .buffer(10); // Process in batches of 10
}
```

#### 5. **Key Advantages in Your Trip Planning System**

1. **Faster Response Times**: Parallel API calls to Google Places + TripAdvisor
2. **Better Resource Utilization**: Non-blocking I/O doesn't tie up threads
3. **Resilience**: Built-in timeout, retry, and fallback mechanisms
4. **Scalability**: Can handle more concurrent users with fewer resources
5. **Real-time Updates**: Can stream live data (weather, traffic, availability)

#### 6. **Architecture Decision Summary**

```
Frontend Request → Spring MVC Controller (synchronous, familiar)
        ↓
Business Logic → Spring Services (synchronous processing)
        ↓
External APIs → WebFlux WebClient (asynchronous, non-blocking)
        ↓
Response Assembly → Spring MVC (synchronous response)
```

This hybrid approach gives you:
- **Familiar development** with Spring MVC for main application logic
- **High performance** with WebFlux for external integrations
- **Best of both worlds** without full reactive complexity

The result is a **fast, resilient system** that can efficiently handle multiple external API calls while providing multi-city trip planning across all provinces of Sri Lanka!
