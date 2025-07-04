# Complete Trip Planning System - User Journey & Implementation

## 🎯 **Your Scenario Implementation - How It All Works**

Based on your detailed scenario, here's how the complete system works with all services and endpoints:

### **Step 1: User Creates a Trip** 
**Endpoint**: `POST /trip/initiate`

```json
{
  "tripName": "My Sri Lanka Adventure",
  "startDate": "2025-07-15",
  "endDate": "2025-07-22",
  "baseCity": "Colombo",
  "categories": ["Adventure", "Culture", "Nature"], // hiking, beaches, cultural places
  "pacing": "MODERATE"
}
```

**What Happens**:
- `TripPlanningService.createTrip()` creates trip with user preferences
- User preferences stored for recommendation engine
- Trip gets unique ID and empty day plans initialized
- Collaborative filtering data updated for future recommendations

---

### **Step 2: User Plans Day-by-Day**

#### **2a. User goes to Day 1 - Gets Initial Suggestions**
**Endpoint**: `GET /trip/{tripId}/day/1/contextual-suggestions?contextType=initial`

**What Happens**:
- `ContextualRecommendationService.generateContextualSuggestions()` 
- Since no places selected yet, shows country-wide suggestions based on preferences:
  - **Hiking spots** (Adventure preference): Adam's Peak, Ella Rock, Knuckles Range
  - **Beaches** (Nature preference): Unawatuna, Mirissa, Bentota  
  - **Cultural places** (Culture preference): Temple of Tooth, Sigiriya, Polonnaruwa
- Results filtered by proximity to base city (Colombo)
- Suggestions include accommodation options first

#### **2b. User Searches for Specific Place**
**Endpoint**: `GET /trip/{tripId}/contextual-search?query=Sigiriya&dayNumber=1`

**What Happens**:
- `LocationService.searchLocations()` with trip context
- **GooglePlacesService** + **TripAdvisorService** hybrid search
- Results automatically filtered to Sri Lanka bounds
- Returns with travel insights: "2.5 hours from Colombo"

#### **2c. User Selects First Place (e.g., Sigiriya)**
**Endpoint**: `POST /trip/{tripId}/day/1/add-place`

```json
{
  "placeName": "Sigiriya Rock Fortress",
  "city": "Dambulla", 
  "placeType": "ATTRACTION",
  "estimatedVisitDurationMinutes": 180,
  "preferredTimeSlot": "morning"
}
```

**What Happens**:
- `TripPlanningService.addPlaceToSpecificDay()` 
- `LocationService.validateAndEnrichPlace()` validates coordinates
- Place added to Day 1 with validated coordinates and enriched data
- **User preference profile updated** for recommendation learning

---

### **Step 3: System Provides Contextual Next Suggestions**

#### **3a. Get Nearby Suggestions After Adding Sigiriya**
**Endpoint**: `GET /trip/{tripId}/day/1/contextual-suggestions?contextType=next_place`

**What Happens**:
- `ContextualRecommendationService.generateNextPlaceSuggestions()`
- **Proximity Analysis**: Finds places within reasonable distance from Sigiriya
- **Preference Matching**: Filters by user's Adventure/Culture/Nature preferences
- **Travel Time Calculation**: Uses `TravelTimeService` to calculate distances

**Results Include**:
- **Nearby accommodations**: Hotels in Dambulla (15 min from Sigiriya)
- **Cultural attractions**: Dambulla Cave Temple (20 min), Polonnaruwa (1 hour)
- **Adventure activities**: Hiking trails in Minneriya (45 min)
- **Each suggestion shows**: Distance, travel time, reason for suggestion

#### **3b. User Searches Near Current Location**
**Endpoint**: `GET /trip/{tripId}/contextual-search?query=temple&lastPlaceId=sigiriya-place-id`

**What Happens**:
- Search biased to Sigiriya's coordinates (7.9570° N, 80.7603° E)
- Results sorted by distance from Sigiriya
- Cultural places prioritized due to user preferences

---

### **Step 4: User Changes Cities/Provinces**

#### **4a. User Selects Place in Different City (e.g., Kandy)**
**Endpoint**: `POST /trip/{tripId}/day/2/add-place`

```json
{
  "placeName": "Temple of the Sacred Tooth Relic",
  "city": "Kandy",
  "placeType": "ATTRACTION",
  "previousPlaceId": "sigiriya-place-id"
}
```

**What Happens**:
- `TravelTimeService.estimateTravelTime()` calculates Sigiriya → Kandy (2 hours)
- System provides **travel insights**: 
  - "This is a 2-hour drive from your previous location"
  - "Consider visiting Matale spice gardens on the way"
- **Province context switches** to Central Province

#### **4b. Next Suggestions Change to Kandy Area**
**Endpoint**: `GET /trip/{tripId}/day/2/contextual-suggestions?contextType=next_place`

**Results Now Include**:
- **Kandy area attractions**: Royal Botanic Gardens (15 min), Kandy Lake (5 min)
- **Cultural experiences**: Traditional dance shows, spice gardens
- **Nature activities**: Udawattakele Forest Reserve (hiking preference)
- **Travel insights**: All calculated from current position in Kandy

---

### **Step 5: Search with Category Filtering**

**Endpoint**: `GET /trip/{tripId}/contextual-search?query=restaurant&lastPlaceId=kandy-temple-id&placeType=RESTAURANT`

**What Happens**:
- Search filtered to restaurants only
- Results show Kandy restaurants sorted by:
  1. **Distance** from Temple of Tooth
  2. **Rating** and reviews
  3. **Cultural cuisine preference** (based on user's Culture interest)
- Each result includes: "5-minute walk", "Traditional Sri Lankan cuisine", "Highly rated"

---

### **Step 6: Daily Optimization & Route Planning**

#### **6a. Optimize Single Day**
**Endpoint**: `POST /trip/{tripId}/day/1/optimize-order`

**What Happens**:
- `RouteOptimizationService.optimizeDayRoute()` 
- **TSP Algorithm** finds optimal visiting order
- **Time-based constraints**: Morning/afternoon preferences respected
- **Travel time minimization**: Reduces driving between places

#### **6b. Optimize Entire Trip**
**Endpoint**: `POST /trip/{tripId}/optimize-order`

**What Happens**:
- Multi-day route optimization
- **Province clustering**: Groups nearby places by day
- **Travel efficiency**: Minimizes long-distance travel
- **Preference satisfaction**: Ensures all user categories covered

---

### **Step 7: Collaborative Recommendations**

#### **7a. Similar User Recommendations**
**How It Works Automatically**:
- `CollaborativeRecommender.recommendBasedOnSimilarUsers()`
- **User Similarity**: Finds users with similar preferences (Adventure+Culture+Nature)
- **Place Popularity**: Shows places highly rated by similar users
- **Contextual Filtering**: Only shows places relevant to current location

**Example Logic**:
```
User A (you): Adventure + Culture + Nature, currently in Kandy
User B: Adventure + Culture + Nature, visited Kandy → went to Ella (rated 5★)
User C: Adventure + Culture + Nature, visited Kandy → went to Nuwara Eliya (rated 5★)
→ System suggests Ella and Nuwara Eliya with high confidence
```

#### **7b. Content-Based Recommendations**
- `ContentBasedRecommender.recommendSimilarPlaces()`
- **Place Similarity**: Finds places with similar categories to ones you liked
- **Preference Matching**: Scores places based on your Adventure/Culture/Nature interests

---

### **Step 8: Travel Insights & Warnings**

#### **8a. Get Travel Information**
**Endpoint**: `GET /trip/{tripId}/travel-info?fromPlaceId=kandy-temple&toPlaceId=ella-rock`

**Response Includes**:
```json
{
  "distanceKm": 65.2,
  "travelTimeMinutes": 120,
  "travelMode": "driving",
  "insights": [
    "Scenic mountain drive through tea plantations",
    "Consider stopping at tea factories en route",
    "Weather can change quickly in hill country"
  ]
}
```

#### **8b. Automatic Warnings**
- **Long Distance Warning**: "This is over 100km, consider splitting across days"
- **Time Constraints**: "You have limited time remaining today"
- **Province Changes**: "You're moving to a different climate zone"

---

### **Step 9: Editable Suggestions & Final Saving**

#### **9a. Accept/Modify Optimizations**
- All suggestions are **editable**
- User can **reorder**, **remove**, or **add new places**
- System **recalculates** travel times and insights automatically

#### **9b. Save Complete Trip**
- Trip automatically saved with every change
- **Final validation** ensures all coordinates are valid
- **Summary generation** with total distance, time, cost estimates

---

## 🛠️ **Complete Service Architecture**

### **Core Services & Their Roles**:

1. **TripPlanningService** (Main Orchestrator)
   - Manages trip lifecycle
   - Coordinates all other services
   - Handles day-by-day planning logic

2. **LocationService** (Location Intelligence)
   - Hybrid search (Google + TripAdvisor)
   - Coordinate validation
   - Geographic filtering

3. **ContextualRecommendationService** (Smart Suggestions) 
   - Context-aware recommendations
   - Proximity-based filtering
   - Travel time integration

4. **GooglePlacesService** (External Data)
   - Real-time place data
   - Geocoding/reverse geocoding
   - Place details and photos

5. **TripAdvisorService** (Tourism Data)
   - Tourism-specific information
   - Reviews and ratings
   - Activity suggestions

6. **RecommendationEngine** (AI/ML)
   - Content-based filtering
   - Collaborative filtering
   - User preference learning

7. **RouteOptimizationService** (Route Planning)
   - TSP algorithm for optimal routes
   - Multi-day optimization
   - Time constraint handling

8. **TravelTimeService** (Distance/Time)
   - Travel time estimation
   - Distance calculations
   - Route analysis

9. **DayPlanningService** (Day Management)
   - Inline day management
   - Real-time suggestions
   - TripAdvisor-style UX

10. **PlaceCategoryService** (Category Management)
    - Google-style place categories
    - Type mapping and validation
    - User-friendly categorization

### **Complete API Endpoints Summary**:

## 📋 **All Available Endpoints with Usage Examples**

### **🚀 Trip Management**

#### 1. **Create Trip** 
**`POST /trip/initiate`** - Create trip with user preferences

**Request:**
```json
{
  "tripName": "My Sri Lanka Adventure",
  "startDate": "2025-07-15",
  "endDate": "2025-07-22",
  "baseCity": "Colombo",
  "categories": ["Adventure", "Culture", "Nature"],
  "pacing": "MODERATE"
}
```

**Response:**
```json
{
  "message": "Trip created successfully",
  "tripId": "trip_67890abcdef",
  "trip": {
    "tripId": "trip_67890abcdef",
    "tripName": "My Sri Lanka Adventure",
    "startDate": "2025-07-15",
    "endDate": "2025-07-22",
    "baseCity": "Colombo",
    "totalDays": 8,
    "preferences": {
      "categories": ["Adventure", "Culture", "Nature"],
      "pacing": "MODERATE"
    },
    "places": [],
    "userId": "user_12345"
  }
}
```

#### 2. **Get User's Trips**
**`GET /trip/my-trips`** - Get all trips for authenticated user

**Response:**
```json
{
  "trips": [
    {
      "tripId": "trip_67890abcdef",
      "tripName": "My Sri Lanka Adventure",
      "startDate": "2025-07-15",
      "endDate": "2025-07-22",
      "totalDays": 8,
      "placesCount": 12,
      "status": "planning"
    }
  ]
}
```

#### 3. **Get Trip Summary**
**`GET /trip/{tripId}/summary`** - Get complete trip overview

**Response:**
```json
{
  "tripId": "trip_67890abcdef",
  "tripName": "My Sri Lanka Adventure",
  "totalDays": 8,
  "totalPlaces": 12,
  "estimatedBudget": "$1200",
  "totalDistanceKm": 845.2,
  "estimatedTravelTimeHours": 18.5,
  "dayBreakdown": [
    { "day": 1, "placesCount": 3, "mainCity": "Colombo" },
    { "day": 2, "placesCount": 2, "mainCity": "Kandy" }
  ]
}
```

#### 4. **Get Map Data**
**`GET /trip/{tripId}/map-data`** - Get GPS coordinates for map visualization

**Response:**
```json
{
  "tripId": "trip_67890abcdef",
  "bounds": {
    "north": 9.831,
    "south": 5.916,
    "east": 81.881,
    "west": 79.653
  },
  "places": [
    {
      "placeId": "place_123",
      "name": "Sigiriya Rock Fortress",
      "latitude": 7.9570,
      "longitude": 80.7603,
      "day": 1,
      "type": "ATTRACTION"
    }
  ],
  "routes": [
    {
      "from": "place_123",
      "to": "place_124",
      "distance": "65km",
      "duration": "2h 15m"
    }
  ]
}
```

---

### **📅 Day-by-Day Planning**

#### 5. **Get Day Plan**
**`GET /trip/{tripId}/day/{dayNumber}`** - Get basic day breakdown

**Response:**
```json
{
  "tripId": "trip_67890abcdef",
  "day": 1,
  "date": "2025-07-15",
  "places": [
    {
      "placeId": "place_123",
      "name": "Sigiriya Rock Fortress",
      "type": "ATTRACTION",
      "estimatedVisitTime": 180,
      "timeSlot": "morning"
    }
  ],
  "estimatedTravelTime": 90,
  "totalDayDuration": 480
}
```

#### 6. **Get Enhanced Day Plan with Suggestions**
**`GET /trip/{tripId}/day/{dayNumber}/plan`** - Get comprehensive day plan with inline suggestions

**Response:**
```json
{
  "tripId": "trip_67890abcdef",
  "dayNumber": 1,
  "dayDate": "2025-07-15",
  "dayName": "Day 1 - Monday",
  "places": [
    {
      "placeId": "place_123",
      "name": "Sigiriya Rock Fortress",
      "type": "ATTRACTION",
      "coordinates": [7.9570, 80.7603],
      "estimatedDuration": 180,
      "timeSlot": "morning",
      "travelFromPrevious": {
        "distance": "65km",
        "duration": "2h 15m",
        "mode": "driving"
      }
    }
  ],
  "inlineSuggestions": [
    {
      "insertAfter": "place_123",
      "suggestions": [
        {
          "placeId": "suggestion_456",
          "name": "Dambulla Cave Temple",
          "type": "ATTRACTION",
          "distance": "20km from Sigiriya",
          "travelTime": "25 minutes",
          "rating": 4.6,
          "reason": "Popular cultural site near your location"
        }
      ]
    }
  ],
  "dayStats": {
    "totalPlaces": 1,
    "totalTravelTime": "2h 15m",
    "totalVisitTime": "3h",
    "mainCity": "Dambulla"
  }
}
```

#### 7. **Add Place to Specific Day**
**`POST /trip/{tripId}/day/{dayNumber}/add-place`** - Add place with full details

**Request:**
```json
{
  "placeName": "Temple of the Sacred Tooth Relic",
  "city": "Kandy",
  "placeType": "ATTRACTION",
  "latitude": 7.2931,
  "longitude": 80.6350,
  "estimatedVisitDurationMinutes": 120,
  "preferredTimeSlot": "morning",
  "previousPlaceId": "place_123"
}
```

**Response:**
```json
{
  "message": "Place added to day 2 successfully",
  "tripId": "trip_67890abcdef",
  "dayNumber": 2,
  "placeName": "Temple of the Sacred Tooth Relic",
  "trip": {
    // Updated trip object with new place
  }
}
```

#### 8. **Quick Add Place (Inline)**
**`POST /trip/{tripId}/day/{dayNumber}/quick-add`** - Quick add without navigation

**Request Parameters:**
- `placeId`: ChIJd7zWn8Ag4joR1r8V8kF8vQI
- `placeName`: Dambulla Cave Temple  
- `placeType`: attraction
- `insertAfterPlaceId`: place_123 (optional)

**Response:**
```json
{
  "message": "Place added successfully",
  "dayPlan": {
    // Updated day plan with new place and refreshed suggestions
  },
  "addedPlace": "Dambulla Cave Temple"
}
```

---

### **🎯 Contextual Suggestions**

#### 9. **Get Contextual Suggestions**
**`GET /trip/{tripId}/day/{dayNumber}/contextual-suggestions?contextType=initial`**

**Context Types:**
- `initial` - First suggestions based on preferences
- `next_place` - Suggestions based on current selections
- `proximity` - Nearby places to last added location

**Response:**
```json
{
  "suggestions": {
    "dayNumber": 1,
    "contextType": "initial",
    "lastAddedPlace": null,
    "travelContext": {
      "currentCity": "Colombo",
      "currentProvince": "Western",
      "suggestedRadius": "50km"
    },
    "categorizedSuggestions": {
      "accommodation": [
        {
          "placeId": "hotel_789",
          "name": "Jetwing Vil Uyana",
          "type": "ACCOMMODATION",
          "location": "Sigiriya",
          "distance": "165km from Colombo",
          "travelTime": "3h 30m",
          "rating": 4.8,
          "priceRange": "$$$",
          "reason": "Luxury eco-resort near cultural sites"
        }
      ],
      "attractions": [
        {
          "placeId": "attr_456",
          "name": "Sigiriya Rock Fortress",
          "type": "ATTRACTION",
          "location": "Dambulla",
          "distance": "165km from Colombo",
          "travelTime": "3h 30m",
          "rating": 4.7,
          "reason": "UNESCO World Heritage Site matching your Culture preference"
        }
      ],
      "restaurants": [
        {
          "placeId": "rest_321",
          "name": "Sigiri Restaurant",
          "type": "RESTAURANT",
          "location": "Sigiriya",
          "distance": "2km from Sigiriya Rock",
          "travelTime": "5 minutes",
          "rating": 4.3,
          "reason": "Traditional Sri Lankan cuisine near attraction"
        }
      ]
    }
  }
}
```

#### 10. **Get Realtime Suggestions**
**`GET /trip/{tripId}/day/{dayNumber}/realtime-suggestions?lastPlaceId=place_123&category=restaurant`**

**Response:**
```json
{
  "suggestions": [
    {
      "placeId": "ChIJd7zWn8Ag4joR1r8V8kF8vQI",
      "name": "Heritage Kandalama",
      "type": "ACCOMMODATION",
      "distance": "15km",
      "travelTime": "20 minutes",
      "rating": 4.6,
      "quickAddable": true,
      "insights": ["Eco-friendly hotel", "Great views of reservoir"]
    }
  ],
  "tripId": "trip_67890abcdef",
  "dayNumber": 1,
  "basedOn": "proximity",
  "category": "restaurant"
}
```

#### 11. **Get Nearby Suggestions**
**`GET /trip/{tripId}/nearby-suggestions?placeId=place_123&placeType=restaurant&maxResults=5`**

**Response:**
```json
{
  "suggestions": [
    {
      "placeId": "rest_789",
      "name": "Sigiri Restaurant",
      "type": "RESTAURANT",
      "distance": "2.3km",
      "travelTime": "5 minutes walking",
      "rating": 4.3,
      "priceLevel": 2,
      "reason": "Highly rated restaurant within walking distance"
    }
  ],
  "basePlaceId": "place_123",
  "count": 1
}
```

---

### **🔍 Location Search & Intelligence**

#### 12. **Basic Location Search**
**`GET /trip/search-locations?query=temples in kandy&city=Kandy&maxResults=10`**

**Response:**
```json
{
  "results": [
    {
      "placeId": "ChIJYTN9T-lE4joRDDuq9bv_R2I",
      "name": "Temple of the Sacred Tooth Relic",
      "formattedAddress": "Kandy 20000, Sri Lanka",
      "latitude": 7.2931,
      "longitude": 80.6350,
      "rating": 4.4,
      "userRatingsTotal": 8234,
      "types": ["tourist_attraction", "place_of_worship"],
      "photoReference": "photo_ref_12345",
      "source": "google_places"
    }
  ],
  "count": 1,
  "query": "temples in kandy"
}
```

#### 13. **Contextual Location Search**
**`GET /trip/{tripId}/contextual-search?query=restaurants&placeType=restaurant&dayNumber=2&lastPlaceId=place_123`**

**Response:**
```json
{
  "results": [
    {
      "placeId": "rest_456",
      "name": "The Empire Cafe",
      "type": "RESTAURANT",
      "location": "Kandy",
      "distance": "5.2km from Temple of Tooth",
      "travelTime": "12 minutes",
      "rating": 4.2,
      "contextualScore": 0.89,
      "matchReason": "Popular restaurant near your planned location"
    }
  ],
  "searchContext": {
    "tripId": "trip_67890abcdef",
    "currentDay": 2,
    "basedOnPlace": "Temple of the Sacred Tooth Relic",
    "searchRadius": "10km",
    "userPreferences": ["Culture", "Adventure", "Nature"]
  },
  "totalResults": 1
}
```

#### 14. **Validate Place**
**`POST /trip/validate-place`** - Validate and enrich place information

**Request:**
```json
{
  "placeName": "Sigiriya Rock",
  "city": "Dambulla",
  "latitude": 7.9570,
  "longitude": 80.7603
}
```

**Response:**
```json
{
  "validation": {
    "valid": true,
    "enrichedData": {
      "officialName": "Sigiriya Rock Fortress",
      "verifiedCoordinates": [7.9570, 80.7603],
      "googlePlaceId": "ChIJtfKjyKMg4joRcDhkyZUCzl8",
      "category": "ATTRACTION",
      "rating": 4.7,
      "reviews": 12458
    },
    "warnings": [],
    "errors": []
  },
  "valid": true,
  "hasWarnings": false,
  "hasErrors": false
}
```

#### 15. **Get Place Details**
**`GET /trip/place-details/{placeId}`** - Get comprehensive place information

**Response:**
```json
{
  "details": {
    "placeId": "ChIJtfKjyKMg4joRcDhkyZUCzl8",
    "name": "Sigiriya Rock Fortress",
    "formattedAddress": "Sigiriya 21120, Sri Lanka",
    "latitude": 7.9570,
    "longitude": 80.7603,
    "rating": 4.7,
    "userRatingsTotal": 12458,
    "priceLevel": 2,
    "types": ["tourist_attraction", "point_of_interest"],
    "photos": ["photo_ref_1", "photo_ref_2"],
    "openingHours": {
      "openNow": true,
      "periods": [
        {
          "open": { "day": 0, "time": "0700" },
          "close": { "day": 0, "time": "1730" }
        }
      ]
    },
    "reviews": [
      {
        "authorName": "John Smith",
        "rating": 5,
        "text": "Amazing historical site with breathtaking views!"
      }
    ]
  },
  "found": true
}
```

---

### **🚗 Travel Information & Optimization**

#### 16. **Get Travel Information**
**`GET /trip/{tripId}/travel-info?fromPlaceId=place_123&toPlaceId=place_456`**

**Response:**
```json
{
  "fromPlace": "Sigiriya Rock Fortress",
  "toPlace": "Temple of the Sacred Tooth Relic",
  "distanceKm": 67.2,
  "travelTimeMinutes": 135,
  "travelMode": "driving",
  "route": {
    "steps": [
      "Head south on B162 toward Dambulla",
      "Turn right onto A1 toward Kandy"
    ],
    "waypoints": [
      { "lat": 7.8731, "lng": 80.6350, "name": "Dambulla Junction" }
    ]
  },
  "insights": [
    "Scenic mountain drive through tea plantations",
    "Consider visiting Matale Spice Gardens en route",
    "Traffic can be heavy during peak hours (7-9 AM, 5-7 PM)"
  ],
  "alternativeRoutes": [
    {
      "name": "Via Matale",
      "distance": "72.1km",
      "duration": "2h 25m",
      "highlights": ["Spice gardens", "Scenic views"]
    }
  ]
}
```

#### 17. **Get Enhanced Travel Information**
**`GET /trip/{tripId}/enhanced-travel-info?fromPlaceId=place_123&toPlaceId=place_456`**

**Response:**
```json
{
  "basicInfo": {
    "distance": "67.2km",
    "duration": "2h 15m",
    "mode": "driving"
  },
  "routeOptions": [
    {
      "name": "Fastest Route",
      "distance": "67.2km",
      "duration": "2h 15m",
      "tolls": false,
      "highlights": ["Most direct path"]
    },
    {
      "name": "Scenic Route",
      "distance": "72.1km", 
      "duration": "2h 35m",
      "tolls": false,
      "highlights": ["Tea plantations", "Mountain views", "Spice gardens"]
    }
  ],
  "travelTips": [
    "Best time to travel: Early morning (6-8 AM) to avoid traffic",
    "Fuel stops available in Dambulla and Matale",
    "Weather can change quickly in hill country"
  ],
  "pointsOfInterest": [
    {
      "name": "Matale Spice Garden",
      "distance": "15km from route",
      "detour": "20 minutes",
      "worth": "Popular stop for spice tours"
    }
  ]
}
```

#### 18. **Optimize Trip Order**
**`POST /trip/{tripId}/optimize-order`** - Optimize entire trip route

**Response:**
```json
{
  "message": "Trip order optimized successfully",
  "trip": {
    "tripId": "trip_67890abcdef",
    "optimizationResults": {
      "originalTotalDistance": "890km",
      "optimizedTotalDistance": "720km",
      "timeSaved": "4h 30m",
      "fuelSaved": "18L",
      "costSaved": "$45"
    },
    "changes": [
      {
        "day": 2,
        "change": "Moved 'Pinnawala Elephant Orphanage' from day 3 to day 2",
        "reason": "Reduces travel distance by 85km"
      }
    ]
    // ... updated trip with optimized order
  }
}
```

---

### **⚙️ Utility Endpoints**

#### 19. **Get Place Categories**
**`GET /trip/place-categories`** - Get available place types

**Response:**
```json
{
  "categories": [
    {
      "type": "ACCOMMODATION",
      "displayName": "Hotels & Accommodation",
      "description": "Hotels, guesthouses, resorts, homestays",
      "icon": "hotel",
      "googleTypes": ["lodging", "hotel", "guest_house"]
    },
    {
      "type": "ATTRACTION",
      "displayName": "Attractions & Sights",
      "description": "Tourist attractions, landmarks, museums",
      "icon": "attraction",
      "googleTypes": ["tourist_attraction", "museum", "park"]
    },
    {
      "type": "RESTAURANT",
      "displayName": "Restaurants & Dining",
      "description": "Restaurants, cafes, local eateries",  
      "icon": "restaurant",
      "googleTypes": ["restaurant", "meal_takeaway", "cafe"]
    },
    {
      "type": "ACTIVITY",
      "displayName": "Activities & Adventures",
      "description": "Tours, activities, adventure sports",
      "icon": "activity", 
      "googleTypes": ["amusement_park", "zoo", "aquarium"]
    }
  ],
  "total": 4
}
```

#### 20. **Add Place to Trip (General)**
**`POST /trip/{tripId}/add-place`** - Add place without specific day

**Request:**
```json
{
  "placeName": "Galle Fort",
  "city": "Galle",
  "placeType": "ATTRACTION",
  "latitude": 6.0329,
  "longitude": 80.2168
}
```

**Response:**
```json
{
  "message": "Place added successfully",
  "trip": {
    // Updated trip object with auto-assigned day based on optimization
  }
}
```

#### 21. **Get AI Suggestions** 
**`GET /trip/{tripId}/suggestions?day=3`** - Get AI-powered general suggestions

**Response:**
```json
{
  "tripId": "trip_67890abcdef",
  "day": 3,
  "suggestions": {
    "recommended": [
      {
        "placeId": "suggestion_789",
        "name": "Ella Rock Hike",
        "type": "ACTIVITY",
        "confidence": 0.92,
        "reason": "Matches your Adventure preference and location"
      }
    ],
    "nearby": [
      {
        "placeId": "suggestion_012",
        "name": "Nine Arches Bridge", 
        "type": "ATTRACTION",
        "distance": "2km",
        "reason": "Popular photo spot near Ella"
      }
    ],
    "collaborative": [
      {
        "placeId": "suggestion_345",
        "name": "Little Adam's Peak",
        "type": "ACTIVITY", 
        "confidence": 0.87,
        "reason": "Highly rated by users with similar preferences"
      }
    ]
  }
}
```

#### 22. **Health Check**
**`GET /trip/health`** - Service health status

**Response:**
```
Trip Planning Service is running
```

## 📊 **Usage Statistics & Response Examples**

### **Typical Response Times:**
- Location Search: 200-500ms
- Contextual Suggestions: 300-800ms  
- Route Optimization: 1-3 seconds
- Travel Time Calculation: 400-1200ms
- Place Validation: 150-400ms

### **Error Response Format:**
All endpoints return consistent error responses:

```json
{
  "error": "Unauthorized",
  "message": "Session validation failed", 
  "timestamp": "2025-07-01T10:30:00Z",
  "path": "/trip/67890abcdef/suggestions"
}
```

**Common HTTP Status Codes:**
- `200` - Success
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (session invalid)
- `404` - Not Found (trip/place not found)
- `500` - Internal Server Error

### **Rate Limiting:**
- Location Search: 100 requests/minute
- Place Details: 200 requests/minute  
- Travel Info: 50 requests/minute
- Other endpoints: 500 requests/minute

---

### **Data Flow Example:**
```
User creates trip → Preferences stored → 
Day 1 planning → Initial suggestions (country-wide) →
User selects Sigiriya → Context switches to Central Province →
Next suggestions (near Sigiriya) → User adds Kandy temple →
Travel time calculated (Sigiriya→Kandy: 2hrs) →
Suggestions update to Kandy area → 
Collaborative filtering suggests Ella (similar users loved it) →
Route optimization suggests best visiting order →
Final trip saved with complete itinerary
```

### **Core Services & Their Roles:**

1. **TripPlanningService** (Main Orchestrator)
   - Manages trip lifecycle
   - Coordinates all other services
   - Handles day-by-day planning logic

2. **LocationService** (Location Intelligence)
   - Hybrid search (Google + TripAdvisor)
   - Coordinate validation
   - Geographic filtering

3. **ContextualRecommendationService** (Smart Suggestions) 
   - Context-aware recommendations
   - Proximity-based filtering
   - Travel time integration

4. **GooglePlacesService** (External Data)
   - Real-time place data
   - Geocoding/reverse geocoding
   - Place details and photos

5. **TripAdvisorService** (Tourism Data)
   - Tourism-specific information
   - Reviews and ratings
   - Activity suggestions

6. **RecommendationEngine** (AI/ML)
   - Content-based filtering
   - Collaborative filtering
   - User preference learning

7. **RouteOptimizationService** (Route Planning)
   - TSP algorithm for optimal routes
   - Multi-day optimization
   - Time constraint handling

8. **TravelTimeService** (Distance/Time)
   - Travel time estimation
   - Distance calculations
   - Route analysis

9. **DayPlanningService** (Day Management)
   - Inline day management
   - Real-time suggestions
   - TripAdvisor-style UX

10. **PlaceCategoryService** (Category Management)
    - Google-style place categories
    - Type mapping and validation
    - User-friendly categorization

---

## 🛠️ **Complete Service Architecture**
