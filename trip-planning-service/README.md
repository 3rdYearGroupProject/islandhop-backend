# Trip Planning Assistant Microservice

A sophisticated AI-powered trip planning service for the IslandHop tourism platform, featuring hybrid recommendation algorithms and intelligent route optimization.

## 🎯 Features

### Core Functionality
- **Smart Trip Creation**: Initialize trips with user preferences and constraints
- **Manual Place Addition**: Allow tourists to add specific destinations
- **AI-Powered Suggestions**: Hybrid recommendation engine combining content-based and collaborative filtering
- **Route Optimization**: Intelligent ordering using distance optimization algorithms
- **Day-by-Day Planning**: Detailed breakdowns with timing and travel segments
- **Map Integration**: GPS coordinates and route data for visualization

### Recommendation Engine
- **Hybrid Algorithm**: Combines content-based filtering (preferences, location) with collaborative filtering (similar users)
- **Multi-Factor Scoring**: 
  - User preference matching
  - Geographic proximity
  - Popularity and ratings
  - Time constraints
  - Diversity encouragement
- **Real-time Adaptation**: Learns from user choices and feedback

### External Integrations
- **TripAdvisor Content API**: Attractions, hotels, restaurants
- **Google Distance Matrix API**: Travel time calculations
- **User Services Integration**: Session validation and authentication

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- Access to external APIs (TripAdvisor, Google Maps)

### Installation

1. **Clone and navigate to the service**:
   ```bash
   cd trip-planning-service
   ```

2. **Set up MongoDB**:
   ```bash
   # Start MongoDB locally
   mongod --dbpath /path/to/data/db
   
   # Or use MongoDB Atlas (update connection string in application.properties)
   ```

3. **Configure API keys**:
   ```bash
   # Edit src/main/resources/api-keys.properties
   TRIPADVISOR_API_KEY=your-tripadvisor-key
   GOOGLE_MAPS_API_KEY=your-google-maps-key
   OPENWEATHER_API_KEY=your-weather-key  # optional
   ```

4. **Install dependencies and run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Load sample data** (optional):
   ```bash
   # Import sample data from mongodb-sample-data.md
   mongoimport --db islandhop_trips --collection trips --file sample-trips.json
   ```

### Configuration

Key configuration options in `application.properties`:

```properties
# Service Configuration
server.port=8083
server.servlet.context-path=/api

# MongoDB
spring.data.mongodb.database=islandhop_trips

# User Services Integration
user.service.base-url=http://localhost:8081/api
user.service.validate-session-endpoint=/tourist/session/validate

# Recommendation Tuning
recommendation.max-daily-travel-hours=8
recommendation.max-attractions-per-day=4
recommendation.similarity-threshold=0.7
```

## 📚 API Documentation

### Core Endpoints

#### Create Trip
```http
POST /api/trip/initiate
Content-Type: application/json

{
  "tripName": "Cultural Tour of Sri Lanka",
  "startDate": "2025-08-01",
  "endDate": "2025-08-05",
  "arrivalTime": "14:30",
  "baseCity": "Colombo",
  "multiCity": true,
  "categories": ["Culture", "Nature"],
  "pacing": "NORMAL"
}
```

#### Add Place
```http
POST /api/trip/{tripId}/add-place
Content-Type: application/json

{
  "placeName": "Temple of the Tooth",
  "city": "Kandy",
  "latitude": 7.2906,
  "longitude": 80.6337,
  "preferredDay": 2
}
```

#### Get Suggestions
```http
GET /api/trip/{tripId}/suggestions?day=2
```

Response:
```json
{
  "tripId": "trip_001",
  "attractions": [
    {
      "recommendationId": "rec_001",
      "suggestedPlace": {
        "placeId": "A001",
        "name": "Gangaramaya Temple",
        "rating": 4.3,
        "categories": ["Culture", "Religion"]
      },
      "score": 0.85,
      "reasons": [
        "Matches your interests in Culture",
        "Highly rated attraction with excellent reviews"
      ],
      "type": "ATTRACTION"
    }
  ],
  "hotels": [...],
  "restaurants": [...],
  "insights": [
    "Found 5 attractions matching your interests in Culture, Nature"
  ],
  "warnings": [],
  "message": "Found 5 recommendations for Day 2 in Kandy"
}
```

#### Optimize Route
```http
POST /api/trip/{tripId}/optimize-order
```

#### Get Day Plan
```http
GET /api/trip/{tripId}/day/2
```

#### Get Map Data
```http
GET /api/trip/{tripId}/map-data
```

### Authentication
All endpoints require valid session authentication. The service validates sessions with the user-services microservice.

## 🔧 Architecture

### Service Structure
```
trip-planning-service/
├── src/main/java/com/islandhop/tripplanning/
│   ├── controller/          # REST endpoints
│   ├── service/            # Business logic
│   │   ├── recommendation/ # AI recommendation engine
│   │   └── external/      # External API integrations
│   ├── model/             # Data models
│   ├── repository/        # MongoDB repositories
│   ├── dto/              # Data transfer objects
│   └── config/           # Configuration classes
├── src/main/resources/
│   ├── application.properties
│   └── api-keys.properties
└── mongodb-sample-data.md  # Sample data for testing
```

### Recommendation Algorithm Flow

1. **Content-Based Analysis**:
   - Analyze user trip preferences
   - Find attractions matching categories
   - Score based on location proximity
   - Consider time constraints and pacing

2. **Collaborative Filtering**:
   - Find users with similar trip patterns
   - Analyze popular attractions among similar users
   - Weight by user satisfaction scores

3. **Hybrid Combination**:
   - Combine scores with 70% content-based, 30% collaborative weights
   - Apply business rules and constraints
   - Rank and return top recommendations

### Database Schema

#### Trips Collection
```javascript
{
  _id: ObjectId,
  tripId: String,
  userId: String,
  tripName: String,
  startDate: Date,
  endDate: Date,
  places: [PlannedPlace],
  dayPlans: [DayPlan],
  statistics: TripStatistics,
  // ... other fields
}
```

#### User Preferences Collection
```javascript
{
  _id: String (userId),
  categoryPreferences: Map<String, Integer>,
  locationPreferences: Map<String, Double>,
  visitedAttractions: Map<String, Integer>,
  similarUsers: [String],
  // ... other fields
}
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test with Sample Data
1. Import sample data from `mongodb-sample-data.md`
2. Start the service
3. Use the provided sample trip data to test endpoints

### Manual Testing
```bash
# Create a trip
curl -X POST http://localhost:8083/api/trip/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2025-08-01",
    "endDate": "2025-08-05",
    "baseCity": "Colombo",
    "categories": ["Culture"],
    "pacing": "NORMAL"
  }'

# Get suggestions
curl http://localhost:8083/api/trip/{tripId}/suggestions
```

## 🔧 Configuration & Deployment

### Environment Variables
```bash
export TRIPADVISOR_API_KEY="your-key"
export GOOGLE_MAPS_API_KEY="your-key"
export MONGODB_URI="mongodb://localhost:27017/islandhop_trips"
```

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/trip-planning-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Production Considerations
- Configure proper MongoDB connection pooling
- Set up external API rate limiting
- Implement caching for recommendation results
- Add monitoring and logging
- Configure proper CORS settings

## 🤝 Integration with Other Services

### User Services
- Session validation via `/tourist/session/validate`
- User authentication and authorization

### Frontend Integration
- CORS enabled for `http://localhost:5173`
- RESTful JSON API
- Map data optimized for visualization libraries

## 📊 Monitoring & Analytics

### Health Check
```http
GET /api/trip/health
```

### Metrics
The service exposes actuator endpoints for monitoring:
- `/actuator/health`
- `/actuator/info`

### Recommendation Performance
Track recommendation acceptance rates and user satisfaction through the statistics stored in trip documents.

## 🔮 Future Enhancements

- **Real-time Weather Integration**: Weather-based recommendations
- **Machine Learning Improvements**: Advanced ML models for better predictions
- **Social Features**: Trip sharing and collaborative planning
- **Budget Optimization**: Cost-aware recommendations
- **Crowd Prediction**: Real-time crowd level warnings
- **Multi-language Support**: Internationalization

## 🐛 Troubleshooting

### Common Issues

1. **MongoDB Connection Issues**:
   - Check MongoDB is running
   - Verify connection string in application.properties

2. **API Key Errors**:
   - Ensure API keys are properly set in api-keys.properties
   - Check API quotas and limits

3. **Session Validation Failures**:
   - Verify user-services is running on correct port
   - Check CORS configuration

4. **No Recommendations Returned**:
   - Check sample data is loaded
   - Verify TripAdvisor service is returning mock data

### Logs
Check application logs for detailed error information:
```bash
tail -f logs/trip-planning-service.log
```

## 📄 License

This project is part of the IslandHop tourism platform.

---

For questions and support, please refer to the main IslandHop documentation or contact the development team.
