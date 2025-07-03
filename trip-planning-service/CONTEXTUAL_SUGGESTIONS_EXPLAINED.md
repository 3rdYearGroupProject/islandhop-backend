# Contextual Suggestions System - How It Works

## Overview
The contextual suggestions system in your trip planning application provides intelligent, context-aware recommendations based on the user's current trip state, location, preferences, and planning progress. It adapts its suggestions dynamically based on multiple factors.

## Types of Suggestions

### 1. **General Suggestions** (`GET /trip/{tripId}/suggestions`)
Basic AI-powered suggestions for the entire trip without specific context.

**When Used:**
- Initial trip overview
- General exploration of trip possibilities
- When user wants broad recommendations

**What It Provides:**
- Attractions based on trip categories
- Hotels and accommodations
- Restaurants in the base city
- General insights about the destination

### 2. **Contextual Day Suggestions** (`GET /trip/{tripId}/day/{dayNumber}/contextual-suggestions`)
Smart suggestions based on the specific day and current planning state.

**When Used:**
- Planning a specific day
- After adding places to a day
- When user needs targeted recommendations

**Context Types:**
- `initial`: First time planning this day
- `next_place`: After adding a place, suggesting what's next

### 3. **Nearby Suggestions** (`GET /trip/{tripId}/nearby-suggestions`)
Location-specific suggestions based on a selected place.

**When Used:**
- User selects a specific place
- Looking for activities around a location
- Building a cluster of activities

## How Contextual Suggestions Work

### Context Analysis Engine

The system analyzes multiple context factors:

```java
// Context factors analyzed:
1. Current trip state (places added, day being planned)
2. Last added place (location, type, time)
3. Travel context (province, city, remaining time)
4. User preferences (categories, pacing)
5. Day constraints (time left, places count)
6. Geographic context (current area, province)
```

### 1. **Initial Day Suggestions** (`contextType: "initial"`)

**Circumstances:**
- User starts planning a new day
- No places added to this day yet
- Fresh planning session

**Algorithm Process:**
```java
// Step 1: Check accommodation needs
if (!hasAccommodationForDay(trip, dayNumber)) {
    // Suggest hotels in base city or current area
    getSuggestionsForCity(baseCity, HOTEL, categories, 5);
}

// Step 2: Suggest attractions based on preferences
getSuggestionsForCity(baseCity, ATTRACTION, trip.getCategories(), 10);

// Step 3: Add restaurant suggestions
getSuggestionsForCity(baseCity, RESTAURANT, categories, 5);
```

**Example Response:**
```json
{
  "dayNumber": 2,
  "contextType": "initial",
  "accommodations": [
    {
      "name": "Hotel Suisse Kandy",
      "city": "Kandy",
      "placeType": "HOTEL",
      "relevanceScore": 0.9,
      "reasonForSuggestion": "Highly rated hotel in Kandy matching your preferences"
    }
  ],
  "attractions": [
    {
      "name": "Temple of the Tooth",
      "category": ["cultural", "religious"],
      "relevanceScore": 0.95,
      "reasonForSuggestion": "Matches your cultural interests"
    }
  ],
  "insights": [
    "Start your day by selecting accommodation if you haven't already",
    "Based on your preferences, we recommend exploring cultural attractions"
  ]
}
```

### 2. **Next Place Suggestions** (`contextType: "next_place"`)

**Circumstances:**
- User just added a place to the day
- Looking for what to do next
- Building a logical sequence

**Algorithm Process:**
```java
// Step 1: Get the last added place
PlannedPlace lastAddedPlace = getLastAddedPlaceForDay(trip, dayNumber);

// Step 2: Find nearby attractions (within reasonable distance)
getSuggestionsNearPlace(lastAddedPlace, categories, ATTRACTION, 8);

// Step 3: Find nearby restaurants
getSuggestionsNearPlace(lastAddedPlace, categories, RESTAURANT, 5);

// Step 4: Calculate travel times and distances
for each suggestion {
    calculateDistance(lastPlace.coords, suggestion.coords);
    estimateTravelTime(lastPlace, suggestion);
}
```

**Example Scenario:**
```
User adds "Temple of the Tooth" in Kandy
↓
System analyzes: Cultural site, Kandy location, morning visit
↓
Suggests nearby:
- Royal Botanical Gardens Peradeniya (20 min drive)
- Kandy Lake (5 min walk)  
- Cultural show venue (15 min walk)
- Traditional restaurant (10 min walk)
```

**Smart Context Analysis:**
```java
// Travel Context Building
TravelContext context = {
    currentCity: "Kandy",
    currentProvince: "Central",
    currentLatitude: 7.2906,
    currentLongitude: 80.6337,
    totalPlacesToday: 2,
    remainingTimeToday: 360, // 6 hours left
    suggestedNextArea: "Peradeniya"
}
```

### 3. **Nearby Suggestions** (Location-Based)

**Circumstances:**
- User clicks on a specific place
- Wants to see what's around that location
- Planning clusters of activities

**Algorithm Process:**
```java
// Step 1: Define search radius based on area type
double radius = determineSearchRadius(referencePlace);
// Urban area: 5km, Rural: 20km, Tourist zone: 10km

// Step 2: Search by place type
List<PlannedPlace> places = searchPlacesByType(
    lat, lng, placeType, radius);

// Step 3: Filter and rank by relevance
filterByCategories(places, trip.getCategories());
rankByDistance(places, referencePlace);
rankByRating(places);
rankByPopularity(places);
```

**Example Response:**
```json
{
  "suggestions": [
    {
      "name": "Royal Botanical Gardens",
      "distanceFromLastPlaceKm": 5.2,
      "travelTimeFromLastPlaceMinutes": 20,
      "travelMode": "driving",
      "relevanceScore": 0.92,
      "reasonForSuggestion": "Popular nature attraction near Temple of the Tooth",
      "bestTimeToVisit": "morning",
      "estimatedVisitDurationMinutes": 120
    }
  ]
}
```

## Smart Context Factors

### 1. **Time-Based Context**
```java
// Morning (6-11 AM): Museums, temples, gardens
// Afternoon (11-4 PM): Outdoor activities, sightseeing
// Evening (4-8 PM): Markets, beaches, cultural shows
// Night (8+ PM): Restaurants, entertainment, accommodation

if (isAfternoon() && isBeachCity(currentCity)) {
    prioritizeBeachActivities();
} else if (isMorning() && hasTemples(currentCity)) {
    prioritizeTempleVisits();
}
```

### 2. **Geographic Context**
```java
// Province-specific suggestions
if (currentProvince.equals("Central")) {
    suggestTeaPlantations();
    suggestMountainViews();
    suggestCulturalSites();
} else if (currentProvince.equals("Southern")) {
    suggestBeaches();
    suggestForts();
    suggestWildlife();
}
```

### 3. **Trip Pacing Context**
```java
// Adjust suggestions based on pacing preference
if (trip.getPacing() == RELAXED) {
    maxPlacesPerDay = 3;
    bufferTimeBetweenPlaces = 60; // 1 hour
} else if (trip.getPacing() == ACTIVE) {
    maxPlacesPerDay = 6;
    bufferTimeBetweenPlaces = 15; // 15 minutes
}
```

### 4. **Category Preferences Context**
```java
// Weight suggestions based on user interests
Map<String, Double> categoryWeights = {
    "culture": 0.9,  // User loves cultural sites
    "nature": 0.7,   // Moderate interest
    "adventure": 0.3 // Low interest
}

// Apply weights to suggestion ranking
suggestion.relevanceScore *= categoryWeights.get(suggestion.category);
```

## Smart Insights and Warnings

### Dynamic Insights Generation
```java
// Context-aware insights
if (context.getTotalPlacesToday() == 0) {
    insights.add("Start your day by selecting accommodation");
    insights.add("Based on your preferences, explore " + 
                 trip.getCategories().get(0) + " attractions");
}

if (context.getRemainingTimeToday() < 120) {
    insights.add("Limited time remaining. Consider quick visits or nearby restaurants");
}

if (isRainySeasonAndOutdoorPlanned()) {
    insights.add("Check weather forecast - consider indoor alternatives");
}
```

### Smart Warnings
```java
// Over-planning detection
if (context.getTotalPlacesToday() > 5) {
    warnings.add("You might be planning too many places for one day");
}

// Travel time warnings
if (totalTravelTimeToday > maxDailyTravelTime) {
    warnings.add("Consider reducing travel between distant locations");
}

// Timing conflicts
if (hasTimingConflicts(dayPlan)) {
    warnings.add("Some attractions may be closed during your planned visit times");
}
```

## Multi-Context Suggestion Scenarios

### Scenario 1: First Day in Colombo
```json
{
  "context": "New trip, cultural interests, morning start",
  "suggestions": [
    {
      "name": "Gangaramaya Temple",
      "reasonForSuggestion": "Perfect cultural start, open early, central location"
    },
    {
      "name": "National Museum",
      "reasonForSuggestion": "Nearby cultural site, complements temple visit"
    }
  ],
  "insights": ["Start with cultural sites as they open early and are less crowded"]
}
```

### Scenario 2: After Adding Temple of the Tooth, Kandy
```json
{
  "context": "Just added major cultural site, have 4 hours left, cultural interests",
  "suggestions": [
    {
      "name": "Royal Botanical Gardens Peradeniya",
      "distanceFromLastPlaceKm": 5.2,
      "travelTimeFromLastPlaceMinutes": 20,
      "reasonForSuggestion": "Perfect nature complement to cultural morning, manageable distance"
    }
  ],
  "insights": ["Gardens are perfect for afternoon after morning temple visit"]
}
```

### Scenario 3: Multi-City Trip Day 5 in Galle
```json
{
  "context": "Southern province, beach area, afternoon, adventure interests",
  "suggestions": [
    {
      "name": "Unawatuna Beach",
      "reasonForSuggestion": "Perfect beach time, water sports available for adventure lovers"
    },
    {
      "name": "Snake Island",
      "reasonForSuggestion": "Short boat trip, adventure activity, unique experience"
    }
  ],
  "insights": ["Afternoon is perfect for beach activities in Southern province"]
}
```

## Technical Implementation Flow

### 1. Request Processing
```java
// Controller receives request
@GetMapping("/{tripId}/day/{dayNumber}/contextual-suggestions")
public ResponseEntity<?> getContextualSuggestions(
    @PathVariable String tripId,
    @PathVariable Integer dayNumber,
    @RequestParam String contextType) {
    
    // Validate session and get user
    String userId = sessionValidationService.validateSessionAndGetUserId(session);
    
    // Get contextual suggestions
    ContextualSuggestionsResponse suggestions = 
        tripPlanningService.getContextualSuggestions(tripId, dayNumber, contextType, userId);
    
    return ResponseEntity.ok(suggestions);
}
```

### 2. Context Analysis
```java
// Service analyzes context
public ContextualSuggestionsResponse getContextualSuggestions(
    String tripId, Integer dayNumber, String contextType, String userId) {
    
    // Get trip data
    Trip trip = getTripByIdAndUserId(tripId, userId);
    
    // Analyze current context
    PlannedPlace lastAddedPlace = getLastAddedPlaceForDay(trip, dayNumber);
    TravelContext travelContext = buildTravelContext(trip, dayNumber, lastAddedPlace);
    
    // Generate appropriate suggestions
    return contextualRecommendationService.generateContextualSuggestions(
        trip, dayNumber, contextType);
}
```

### 3. Suggestion Generation
```java
// Generate context-specific suggestions
if ("initial".equals(contextType)) {
    generateInitialDaySuggestions(response, trip, dayNumber);
} else if ("next_place".equals(contextType)) {
    generateNextPlaceSuggestions(response, trip, dayNumber, lastAddedPlace);
}

// Add intelligent insights
response.setInsights(generateInsights(trip, dayNumber, travelContext));
response.setWarnings(generateWarnings(trip, dayNumber, travelContext));
```

## Benefits of Contextual Suggestions

1. **Personalized Experience**: Adapts to user's specific interests and trip state
2. **Location Intelligence**: Suggests relevant nearby places based on current position
3. **Time Optimization**: Considers travel time and remaining day time
4. **Smart Sequencing**: Builds logical flow of activities
5. **Category Matching**: Aligns with user's stated preferences
6. **Dynamic Adaptation**: Changes based on trip progress and choices
7. **Multi-Province Support**: Understands Sri Lankan geography and attractions
8. **Practical Insights**: Provides actionable tips and warnings

This contextual system makes trip planning intelligent and adaptive, providing users with relevant suggestions that make sense in their current context! 🎯🇱🇰
