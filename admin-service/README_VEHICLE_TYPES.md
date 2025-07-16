# Admin Functions Microservice

A complete Java 17 Spring Boot microservice that provides CRUD operations for vehicle types management in the IslandHop travel system.

## Features

- **Full CRUD Operations**: Create, Read, Update, Delete vehicle types
- **Advanced Querying**: Search by fuel type, price range, availability, and capacity
- **Input Validation**: Comprehensive validation using Jakarta Validation
- **Exception Handling**: Global exception handling with standardized API responses
- **Debug Logging**: Detailed logging with SLF4J and Logback
- **PostgreSQL Integration**: Uses Neon-hosted PostgreSQL database

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.3**
- **Spring Data JPA**
- **PostgreSQL** (Neon cloud)
- **Maven**
- **Lombok**
- **Jakarta Validation**
- **SLF4J + Logback**

## Database Schema

The service manages the `vehicle_types` table with the following structure:

```sql
CREATE TABLE vehicle_types (
  id BIGSERIAL PRIMARY KEY,
  capacity INTEGER,
  description VARCHAR(255),
  fuel_type VARCHAR(255),
  is_available BOOLEAN NOT NULL,
  price_per_km DOUBLE PRECISION NOT NULL,
  type_name VARCHAR(255) UNIQUE NOT NULL
);
```

## API Endpoints

All endpoints are prefixed with `/api/v1/admin/vehicle-types`

### Basic CRUD Operations

#### Create Vehicle Type

- **POST** `/admin/vehicle-types`
- **Request Body:**

```json
{
  "capacity": 4,
  "description": "Compact sedan for city travel",
  "fuelType": "Petrol",
  "isAvailable": true,
  "pricePerKm": 50.0,
  "typeName": "Economy Car"
}
```

- **Response:** `201 Created`

```json
{
  "status": "success",
  "message": "Vehicle type created successfully",
  "data": {
    "id": 1,
    "capacity": 4,
    "description": "Compact sedan for city travel",
    "fuelType": "Petrol",
    "isAvailable": true,
    "pricePerKm": 50.0,
    "typeName": "Economy Car"
  }
}
```

#### Get All Vehicle Types

- **GET** `/admin/vehicle-types`
- **Response:** `200 OK`

```json
{
  "status": "success",
  "message": "Vehicle types retrieved successfully",
  "data": [
    {
      "id": 1,
      "capacity": 4,
      "description": "Compact sedan for city travel",
      "fuelType": "Petrol",
      "isAvailable": true,
      "pricePerKm": 50.0,
      "typeName": "Economy Car"
    }
  ]
}
```

#### Get Vehicle Type by ID

- **GET** `/admin/vehicle-types/{id}`
- **Response:** `200 OK` or `404 Not Found`

#### Update Vehicle Type

- **PUT** `/admin/vehicle-types/{id}`
- **Request Body:** Same as create request
- **Response:** `200 OK` or `404 Not Found`

#### Delete Vehicle Type

- **DELETE** `/admin/vehicle-types/{id}`
- **Response:** `200 OK` or `404 Not Found`

### Advanced Queries

#### Get Available Vehicle Types

- **GET** `/admin/vehicle-types/available`
- Returns only vehicle types where `isAvailable = true`

#### Get Vehicle Types by Fuel Type

- **GET** `/admin/vehicle-types/fuel-type/{fuelType}`
- Example: `/admin/vehicle-types/fuel-type/Petrol`

#### Get Vehicle Types by Price Range

- **GET** `/admin/vehicle-types/price-range?minPrice=20&maxPrice=100`
- Query parameters:
  - `minPrice`: Minimum price per km (required)
  - `maxPrice`: Maximum price per km (required)

## Validation Rules

- **typeName**: Required, cannot be blank, must be unique
- **isAvailable**: Required boolean
- **pricePerKm**: Required, must be zero or positive
- **capacity**: Optional, must be zero or positive if provided
- **description**: Optional
- **fuelType**: Optional

## Error Handling

The service uses a global exception handler that provides standardized error responses:

### Validation Errors (400 Bad Request)

```json
{
  "status": "error",
  "message": "Validation failed",
  "data": {
    "typeName": "Type name is required and cannot be blank",
    "pricePerKm": "Price per km is required"
  }
}
```

### Business Logic Errors (400 Bad Request)

```json
{
  "status": "error",
  "message": "Vehicle type with name 'Economy Car' already exists",
  "data": null
}
```

### Not Found Errors (404 Not Found)

```json
{
  "status": "error",
  "message": "Vehicle type with ID 999 not found",
  "data": null
}
```

### Server Errors (500 Internal Server Error)

```json
{
  "status": "error",
  "message": "An unexpected error occurred",
  "data": null
}
```

## Configuration

The service is configured via `application.properties`:

```properties
# Server Configuration
server.port=8091
server.servlet.context-path=/api/v1

# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://ep-empty-base-a194u9qt-pooler.ap-southeast-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
spring.datasource.username=neondb_owner
spring.datasource.password=npg_Ig3thklS7cZm
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Logging Configuration (Debug Level)
logging.level.com.islandhop.adminservice=DEBUG
logging.level.org.springframework.data.jpa=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

## Running the Service

### Prerequisites

- Java 17
- Maven 3.6+
- PostgreSQL database (Neon cloud)

### Build and Run

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run

# Or run the JAR file
mvn package
java -jar target/admin-service-0.0.1-SNAPSHOT.jar
```

The service will start on port 8091 and be available at:
`http://localhost:8091/api/v1/admin/vehicle-types`

## Testing

### Example cURL Commands

#### Create a vehicle type:

```bash
curl -X POST http://localhost:8091/api/v1/admin/vehicle-types \
  -H "Content-Type: application/json" \
  -d '{
    "capacity": 4,
    "description": "Compact sedan for city travel",
    "fuelType": "Petrol",
    "isAvailable": true,
    "pricePerKm": 50.0,
    "typeName": "Economy Car"
  }'
```

#### Get all vehicle types:

```bash
curl -X GET http://localhost:8091/api/v1/admin/vehicle-types
```

#### Get vehicle type by ID:

```bash
curl -X GET http://localhost:8091/api/v1/admin/vehicle-types/1
```

#### Update vehicle type:

```bash
curl -X PUT http://localhost:8091/api/v1/admin/vehicle-types/1 \
  -H "Content-Type: application/json" \
  -d '{
    "capacity": 4,
    "description": "Updated description",
    "fuelType": "Petrol",
    "isAvailable": true,
    "pricePerKm": 55.0,
    "typeName": "Premium Economy Car"
  }'
```

#### Delete vehicle type:

```bash
curl -X DELETE http://localhost:8091/api/v1/admin/vehicle-types/1
```

## Logging

The service provides detailed logging at DEBUG level for:

- Service method entries and exits
- Database operations
- SQL queries and parameters
- Error conditions
- Performance metrics

Log format: `%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

## Project Structure

```
src/main/java/com/islandhop/adminservice/
├── AdminServiceApplication.java          # Main application class
├── config/
│   └── JpaConfig.java                    # JPA configuration
├── controller/
│   └── VehicleTypeController.java        # REST controller
├── dto/
│   ├── ApiResponse.java                  # Standard API response wrapper
│   ├── CreateVehicleTypeRequest.java     # Create request DTO
│   ├── UpdateVehicleTypeRequest.java     # Update request DTO
│   └── VehicleTypeResponse.java          # Response DTO
├── exception/
│   └── GlobalExceptionHandler.java       # Global exception handling
├── model/
│   └── VehicleType.java                  # JPA entity
├── repository/
│   └── VehicleTypeRepository.java        # JPA repository interface
└── service/
    └── VehicleTypeService.java           # Business logic service
```

## Dependencies

Key dependencies in `pom.xml`:

- `spring-boot-starter-web`: Web layer
- `spring-boot-starter-data-jpa`: JPA data access
- `postgresql`: PostgreSQL driver
- `lombok`: Reduce boilerplate code
- `spring-boot-starter-validation`: Input validation
- `spring-boot-starter-test`: Testing framework

## Security

- No authentication/authorization implemented as per requirements
- CORS enabled for all origins
- Input validation to prevent malicious data
- SQL injection protection through JPA/Hibernate

## Future Enhancements

- Add authentication and authorization (JWT/Spring Security)
- Implement caching with Redis
- Add unit and integration tests
- Implement API versioning
- Add health check endpoints
- Add metrics and monitoring
- Implement soft delete functionality
- Add audit logging (created_at, updated_at, created_by, updated_by)
