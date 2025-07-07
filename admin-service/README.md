# Admin Service

## Overview

The Admin Service is a microservice responsible for monitoring the health and status of all external dependencies used by the IslandHop platform. It provides a centralized endpoint for checking the connectivity and status of Redis, Firebase, and MongoDB services.

## Features

- **System Status Monitoring**: Real-time health checks for external services
- **RESTful API**: JSON responses ready for frontend consumption
- **Configurable**: All connection parameters read from application properties
- **Fault Tolerant**: Graceful handling of service failures

## API Endpoints

### GET /api/v1/admin/status

Returns the status of all external services.

**Response:**

```json
{
  "redis": "UP",
  "firebase": "UP",
  "mongodb": "DOWN"
}
```

**Status Values:**

- `UP`: Service is accessible and responding
- `DOWN`: Service is not accessible or not responding

## Configuration

### Application Properties

Configure the following properties in `application.yml`:

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
    mongodb:
      uri: ${MONGODB_URI:mongodb://localhost:27017/islandhop}
      database: ${MONGODB_DATABASE:islandhop}

firebase:
  service-account-key: ${FIREBASE_SERVICE_ACCOUNT_KEY:classpath:serviceAccountKey.json}
  project-id: ${FIREBASE_PROJECT_ID:islandhop-project}

server:
  port: ${SERVER_PORT:8090}
```

### Environment Variables

You can override the default values using environment variables:

- `REDIS_HOST`: Redis server hostname
- `REDIS_PORT`: Redis server port (default: 6379)
- `REDIS_PASSWORD`: Redis password (optional)
- `MONGODB_URI`: MongoDB connection URI
- `MONGODB_DATABASE`: MongoDB database name
- `FIREBASE_SERVICE_ACCOUNT_KEY`: Path to Firebase service account key
- `FIREBASE_PROJECT_ID`: Firebase project ID
- `SERVER_PORT`: Application port (default: 8090)

## Running the Service

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis server
- MongoDB server
- Firebase project with service account key

### Local Development

1. **Start dependencies:**

   ```bash
   # Start Redis (using Docker)
   docker run -d -p 6379:6379 redis:latest

   # Start MongoDB (using Docker)
   docker run -d -p 27017:27017 mongo:latest
   ```

2. **Configure Firebase:**

   - Replace `src/main/resources/serviceAccountKey.json` with your actual Firebase service account key

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

### Using Docker

```bash
# Build the image
docker build -t admin-service .

# Run the container
docker run -p 8090:8090 \
  -e REDIS_HOST=localhost \
  -e MONGODB_URI=mongodb://localhost:27017/islandhop \
  admin-service
```

## Project Structure

```
admin-service/
├── src/main/java/com/islandhop/adminservice/
│   ├── AdminServiceApplication.java     # Main application class
│   ├── controller/
│   │   └── AdminController.java         # REST endpoints
│   ├── service/
│   │   ├── SystemStatusService.java     # Service interface
│   │   └── impl/
│   │       └── SystemStatusServiceImpl.java # Service implementation
│   ├── model/
│   │   └── SystemStatusResponse.java    # Response model
│   └── config/
│       ├── RedisConfig.java            # Redis configuration
│       ├── FirebaseConfig.java         # Firebase configuration
│       └── MongoConfig.java            # MongoDB configuration
├── src/main/resources/
│   ├── application.yml                 # Application configuration
│   └── serviceAccountKey.json         # Firebase service account key
├── Dockerfile                         # Docker configuration
└── pom.xml                           # Maven dependencies
```

## Dependencies

- Spring Boot 3.2.3
- Spring Boot Starter Web
- Spring Boot Starter Data Redis
- Spring Boot Starter Data MongoDB
- Spring Boot Starter Actuator
- Firebase Admin SDK
- Lombok

## Health Checks

The service includes Spring Boot Actuator endpoints:

- `/actuator/health`: Application health status
- `/actuator/info`: Application information

## Development Notes

- Follows the same package structure and naming conventions as other IslandHop services
- Uses Lombok for reducing boilerplate code
- Implements proper error handling and logging
- Designed for easy integration with React frontend
- All external connections are configurable via environment variables
