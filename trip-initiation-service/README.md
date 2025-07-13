# Trip Initiation Service

## Overview
The Trip Initiation Service is a Spring Boot microservice designed to facilitate the initiation of travel trips. It allows users to create trip plans, calculate optimal routes, and estimate costs associated with the trip, including vehicle and guide fees.

## Technologies Used
- Java 17
- Spring Boot 3.x
- MongoDB for storing trip plans and initiated trips
- PostgreSQL for vehicle types and guide fees
- Google Maps API for route calculations

## Project Structure
```
trip-initiation-service
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── islandhop
│   │   │           └── tripinitiation
│   │   │               ├── TripInitiationServiceApplication.java
│   │   │               ├── controller
│   │   │               │   └── TripInitiationController.java
│   │   │               ├── service
│   │   │               │   ├── TripInitiationService.java
│   │   │               │   ├── RouteCalculationService.java
│   │   │               │   └── CostCalculationService.java
│   │   │               ├── repository
│   │   │               │   ├── TripPlanRepository.java
│   │   │               │   ├── InitiatedTripRepository.java
│   │   │               │   ├── VehicleTypeRepository.java
│   │   │               │   └── GuideFeeRepository.java
│   │   │               ├── model
│   │   │               │   ├── mongo
│   │   │               │   │   ├── TripPlan.java
│   │   │               │   │   ├── InitiatedTrip.java
│   │   │               │   │   ├── DailyPlan.java
│   │   │               │   │   ├── Place.java
│   │   │               │   │   └── Location.java
│   │   │               │   └── postgres
│   │   │               │       ├── VehicleType.java
│   │   │               │       └── GuideFee.java
│   │   │               ├── dto
│   │   │               │   ├── TripInitiationRequest.java
│   │   │               │   ├── TripInitiationResponse.java
│   │   │               │   └── RoutePoint.java
│   │   │               ├── exception
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   ├── TripNotFoundException.java
│   │   │               │   ├── VehicleTypeNotFoundException.java
│   │   │               │   └── RouteCalculationException.java
│   │   │               └── config
│   │   │                   ├── MongoConfig.java
│   │   │                   ├── PostgresConfig.java
│   │   │                   └── GoogleMapsConfig.java
│   │   └── resources
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   └── test
│       └── java
│           └── com
│               └── islandhop
│                   └── tripinitiation
│                       ├── TripInitiationServiceApplicationTests.java
│                       ├── controller
│                       │   └── TripInitiationControllerTest.java
│                       └── service
│                           ├── TripInitiationServiceTest.java
│                           ├── RouteCalculationServiceTest.java
│                           └── CostCalculationServiceTest.java
├── pom.xml
└── README.md
```

## Setup Instructions
1. **Clone the Repository**
   ```
   git clone <repository-url>
   cd trip-initiation-service
   ```

2. **Configure Application Properties**
   - Update `src/main/resources/application.yml` with your MongoDB and PostgreSQL connection details, as well as your Google Maps API key.

3. **Build the Project**
   ```
   mvn clean install
   ```

4. **Run the Application**
   ```
   mvn spring-boot:run
   ```

5. **Access the API**
   - The main endpoint for initiating trips is available at:
     ```
     POST /api/trips/initiate
     ```

## API Documentation
Refer to the API documentation for details on request and response formats for the `/api/trips/initiate` endpoint.

## Logging
Logs are configured to be saved in a text file. Ensure that the logging configuration in `src/main/resources/logback-spring.xml` is set up correctly to capture the necessary log levels.

## Testing
Unit tests are included for the service and controller layers. To run the tests, use:
```
mvn test
```

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License. See the LICENSE file for more details.