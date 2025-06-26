# MongoDB Sample Data for Trip Planning Service

## Sample Trips Collection

```json
[
  {
    "_id": "trip_001",
    "tripId": "trip_001",
    "userId": "user@example.com",
    "tripName": "Cultural Heritage Tour of Sri Lanka",
    "startDate": "2025-08-01",
    "endDate": "2025-08-05",
    "arrivalTime": "14:30",
    "baseCity": "Colombo",
    "multiCity": true,
    "categories": ["Culture", "Nature"],
    "pacing": "NORMAL",
    "status": "PLANNING",
    "places": [
      {
        "placeId": "A001",
        "name": "Gangaramaya Temple",
        "city": "Colombo",
        "latitude": 6.9162,
        "longitude": 79.8562,
        "description": "Famous Buddhist temple in heart of Colombo",
        "categories": ["Culture", "Religion"],
        "type": "ATTRACTION",
        "estimatedVisitDurationMinutes": 90,
        "dayNumber": 1,
        "orderInDay": 1,
        "rating": 4.3,
        "reviewCount": 1250,
        "priceLevel": "FREE",
        "userAdded": false,
        "confirmed": true
      },
      {
        "placeId": "A002",
        "name": "National Museum of Colombo",
        "city": "Colombo",
        "latitude": 6.9094,
        "longitude": 79.8606,
        "description": "Premier museum showcasing Sri Lankan history",
        "categories": ["Culture", "History"],
        "type": "ATTRACTION",
        "estimatedVisitDurationMinutes": 120,
        "dayNumber": 1,
        "orderInDay": 2,
        "rating": 4.1,
        "reviewCount": 890,
        "priceLevel": "BUDGET",
        "userAdded": false,
        "confirmed": true
      },
      {
        "placeId": "A004",
        "name": "Temple of the Sacred Tooth Relic",
        "city": "Kandy",
        "latitude": 7.2906,
        "longitude": 80.6337,
        "description": "Sacred Buddhist temple housing tooth relic",
        "categories": ["Culture", "Religion"],
        "type": "ATTRACTION",
        "estimatedVisitDurationMinutes": 120,
        "dayNumber": 2,
        "orderInDay": 1,
        "rating": 4.6,
        "reviewCount": 2100,
        "priceLevel": "BUDGET",
        "userAdded": true,
        "confirmed": true
      }
    ],
    "dayPlans": [
      {
        "dayNumber": 1,
        "date": "2025-08-01",
        "baseCity": "Colombo",
        "startTime": "15:00",
        "endTime": "18:30",
        "totalTravelTimeMinutes": 30,
        "totalVisitTimeMinutes": 210,
        "totalActivities": 2,
        "pacingAssessment": "Perfect",
        "activities": [
          {
            "activityId": "act_001",
            "startTime": "15:00",
            "endTime": "16:30",
            "durationMinutes": 90,
            "type": "VISIT"
          }
        ],
        "travelSegments": [
          {
            "fromPlaceId": "A001",
            "toPlaceId": "A002",
            "fromPlaceName": "Gangaramaya Temple",
            "toPlaceName": "National Museum of Colombo",
            "distance": 2.5,
            "durationMinutes": 15,
            "travelMode": "TAXI"
          }
        ],
        "dayTips": [
          "Both attractions are in Colombo, making it easy to cover in an afternoon",
          "Consider visiting Gangaramaya during sunset for better photography"
        ],
        "warnings": []
      }
    ],
    "statistics": {
      "totalDays": 5,
      "totalPlaces": 3,
      "totalDistanceKm": 120.5,
      "totalTravelTimeMinutes": 180,
      "totalVisitTimeMinutes": 330,
      "predominantCategory": "Culture",
      "citiesVisited": 2,
      "userSatisfactionScore": 0.85,
      "totalSuggestions": 12,
      "acceptedSuggestions": 8
    },
    "preferences": {
      "maxDailyTravelHours": 8,
      "bufferTimeMinutes": 30,
      "maxAttractionsPerDay": 4
    },
    "excludedAttractions": [],
    "createdAt": "2025-06-25T10:30:00Z",
    "updatedAt": "2025-06-26T14:45:00Z"
  },
  {
    "_id": "trip_002",
    "tripId": "trip_002",
    "userId": "traveler@example.com",
    "tripName": "Nature and Adventure in Sri Lanka",
    "startDate": "2025-09-15",
    "endDate": "2025-09-20",
    "arrivalTime": "09:00",
    "baseCity": "Nuwara Eliya",
    "multiCity": false,
    "categories": ["Nature", "Adventure"],
    "pacing": "ACTIVE",
    "status": "PLANNING",
    "places": [
      {
        "placeId": "A005",
        "name": "Royal Botanical Gardens",
        "city": "Kandy",
        "latitude": 7.2684,
        "longitude": 80.5979,
        "description": "Beautiful botanical gardens with diverse flora",
        "categories": ["Nature", "Garden"],
        "type": "ATTRACTION",
        "estimatedVisitDurationMinutes": 180,
        "dayNumber": 1,
        "orderInDay": 1,
        "rating": 4.5,
        "reviewCount": 1890,
        "priceLevel": "BUDGET",
        "userAdded": false,
        "confirmed": false
      }
    ],
    "dayPlans": [],
    "statistics": null,
    "preferences": {
      "maxDailyTravelHours": 10,
      "bufferTimeMinutes": 15,
      "maxAttractionsPerDay": 5
    },
    "excludedAttractions": [],
    "createdAt": "2025-06-26T08:15:00Z",
    "updatedAt": "2025-06-26T08:15:00Z"
  }
]
```

## Sample User Preferences Collection

```json
[
  {
    "_id": "user@example.com",
    "userId": "user@example.com",
    "categoryPreferences": {
      "Culture": 8,
      "Nature": 6,
      "Adventure": 3,
      "Leisure": 5
    },
    "locationPreferences": {
      "Colombo": 0.9,
      "Kandy": 0.85,
      "Galle": 0.7,
      "Anuradhapura": 0.8
    },
    "visitedAttractions": {
      "A001": 2,
      "A002": 1,
      "A004": 1
    },
    "averageDailyBudget": 75.0,
    "averageAttractionsPerDay": 3,
    "preferredTravelModes": ["TAXI", "BUS", "WALKING"],
    "preferredPacing": "NORMAL",
    "similarUsers": ["traveler@example.com", "explorer@example.com"],
    "attractionRatings": {
      "A001": 0.85,
      "A002": 0.75,
      "A004": 0.95
    },
    "lastUpdated": "2025-06-26T14:45:00Z"
  },
  {
    "_id": "traveler@example.com",
    "userId": "traveler@example.com",
    "categoryPreferences": {
      "Nature": 9,
      "Adventure": 8,
      "Culture": 4,
      "Leisure": 3
    },
    "locationPreferences": {
      "Nuwara Eliya": 0.95,
      "Ella": 0.9,
      "Kandy": 0.7
    },
    "visitedAttractions": {
      "A005": 1
    },
    "averageDailyBudget": 100.0,
    "averageAttractionsPerDay": 4,
    "preferredTravelModes": ["DRIVING", "HIKING"],
    "preferredPacing": "ACTIVE",
    "similarUsers": ["user@example.com"],
    "attractionRatings": {
      "A005": 0.9
    },
    "lastUpdated": "2025-06-26T08:15:00Z"
  }
]
```

## Sample Place Data (TripAdvisor Mock Responses)

```json
{
  "colombo_attractions": [
    {
      "placeId": "A001",
      "name": "Gangaramaya Temple",
      "latitude": 6.9162,
      "longitude": 79.8562,
      "rating": 4.3,
      "reviewCount": 1250,
      "categories": ["Culture", "Religion"],
      "priceLevel": "FREE",
      "estimatedVisitDurationMinutes": 90,
      "description": "One of the most important temples in Colombo, featuring diverse architectural styles"
    },
    {
      "placeId": "A002",
      "name": "National Museum of Colombo",
      "latitude": 6.9094,
      "longitude": 79.8606,
      "rating": 4.1,
      "reviewCount": 890,
      "categories": ["Culture", "History"],
      "priceLevel": "BUDGET",
      "estimatedVisitDurationMinutes": 120,
      "description": "Sri Lanka's premier museum showcasing the country's rich history and culture"
    },
    {
      "placeId": "A003",
      "name": "Galle Face Green",
      "latitude": 6.9248,
      "longitude": 79.8434,
      "rating": 4.0,
      "reviewCount": 2100,
      "categories": ["Nature", "Leisure"],
      "priceLevel": "FREE",
      "estimatedVisitDurationMinutes": 60,
      "description": "Popular ocean-side urban park perfect for evening strolls and local food"
    }
  ],
  "kandy_attractions": [
    {
      "placeId": "A004",
      "name": "Temple of the Sacred Tooth Relic",
      "latitude": 7.2906,
      "longitude": 80.6337,
      "rating": 4.6,
      "reviewCount": 2100,
      "categories": ["Culture", "Religion"],
      "priceLevel": "BUDGET",
      "estimatedVisitDurationMinutes": 120,
      "description": "Sacred Buddhist temple housing the tooth relic of Lord Buddha"
    },
    {
      "placeId": "A005",
      "name": "Royal Botanical Gardens",
      "latitude": 7.2684,
      "longitude": 80.5979,
      "rating": 4.5,
      "reviewCount": 1890,
      "categories": ["Nature", "Garden"],
      "priceLevel": "BUDGET",
      "estimatedVisitDurationMinutes": 180,
      "description": "Beautiful botanical gardens spanning 147 acres with diverse flora"
    }
  ]
}
```

## Database Setup Commands

```javascript
// MongoDB commands to set up the database

// 1. Create database
use islandhop_trips

// 2. Create collections with indexes
db.trips.createIndex({ "userId": 1 })
db.trips.createIndex({ "tripId": 1, "userId": 1 })
db.trips.createIndex({ "categories": 1 })
db.trips.createIndex({ "pacing": 1 })
db.trips.createIndex({ "startDate": 1 })

db.user_preferences.createIndex({ "userId": 1 })
db.user_preferences.createIndex({ "similarUsers": 1 })

// 3. Insert sample data (copy the JSON arrays above)
db.trips.insertMany([/* trips data */])
db.user_preferences.insertMany([/* user preferences data */])
```

## Environment Setup

1. **Install MongoDB locally or use MongoDB Atlas**
2. **Update application.properties with your MongoDB connection**:
   ```properties
   spring.data.mongodb.host=localhost
   spring.data.mongodb.port=27017
   spring.data.mongodb.database=islandhop_trips
   ```
3. **Insert the sample data using MongoDB Compass or mongosh**
4. **Update API keys in api-keys.properties when you get them**

This sample data provides a good foundation for testing the recommendation algorithms and API endpoints.
