# Trip Planning Service

A sophisticated AI-powered trip planning service for the IslandHop tourism platform, featuring hybrid recommendation algorithms and intelligent route optimization.

## 📚 Documentation

- **[Complete API Documentation](API_DOCUMENTATION.md)** - All endpoints with examples
- **[Frontend Integration Guide](FRONTEND_INTEGRATION_GUIDE.md)** - Detailed integration examples

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
- MongoDB 7.0+
- Maven 3.6+
- Docker (optional)

### Running with Docker
```bash
docker-compose up
```

### Running Locally
1. **Start MongoDB**:
   ```bash
   mongod --dbpath /path/to/your/db
   ```

2. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

3. **Health Check**:
   ```bash
   curl http://localhost:8083/api/v1/trip/health
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

## 🔗 Key Endpoints

### Quick Reference
- **Health Check**: `GET /api/v1/trip/health`
- **Search Cities**: `GET /api/v1/trip/search-locations?query=Colombo`
- **Create Trip**: `POST /api/v1/trip/create-basic`
- **Add Cities**: `POST /api/v1/trip/{tripId}/cities`

For complete endpoint documentation with examples, see **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)**

## 🏗️ Project Structure

```
trip-planning-service/
├── src/main/java/
│   └── com/islandhop/tripplanning/
│       ├── controller/     # REST controllers
│       ├── service/        # Business logic
│       ├── model/          # Data models
│       ├── dto/            # Data transfer objects
│       └── config/         # Configuration classes
├── API_DOCUMENTATION.md    # Complete API reference
├── FRONTEND_INTEGRATION_GUIDE.md  # Frontend examples
├── docker-compose.yml      # Docker setup
└── README.md              # This file
```

## 🔧 Architecture

### Service Structure
```
trip-planning-service/
├── controller/          # REST endpoints
├── service/            # Business logic
│   ├── recommendation/ # AI recommendation engine
│   └── external/      # External API integrations
├── model/             # Data models
├── repository/        # MongoDB repositories
├── dto/              # Data transfer objects
└── config/           # Configuration classes
```

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
  statistics: TripStatistics
}
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Manual Testing
```bash
# Health check
curl http://localhost:8083/api/v1/trip/health

# Search for cities
curl "http://localhost:8083/api/v1/trip/search-locations?query=Colombo"
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

## 🤝 Integration with Other Services

### User Services
- Session validation via `/tourist/session/validate`
- User authentication and authorization

### Frontend Integration
- CORS enabled for `http://localhost:5173`
- RESTful JSON API
- Map data optimized for visualization libraries

## 📊 Monitoring

### Health Check
```http
GET /api/v1/trip/health
```

### Actuator Endpoints
- `/actuator/health` - System health
- `/actuator/info` - Application info

## 🐛 Troubleshooting

### Common Issues

1. **MongoDB Connection Issues**:
   - Check MongoDB is running
   - Verify connection string in application.properties

2. **API Key Errors**:
   - Ensure API keys are properly set
   - Check API quotas and limits

3. **Session Validation Failures**:
   - Verify user-services is running on correct port
   - Check CORS configuration

## 📄 License

This project is part of the IslandHop tourism platform.

---

For detailed API documentation and frontend integration examples, see the linked documentation files above.
