# Trip Initiation Microservice

## Overview
The Trip Initiation Microservice is a Spring Boot application designed to facilitate the initiation of travel trips. It allows users to create trip plans, calculate costs based on vehicle types and guide fees, and save initiated trips to a MongoDB database.

## Project Structure
The project is organized as follows:

```
trip-initiation-microservice
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── islandhop
│   │   │           └── tripinit
│   │   │               ├── TripInitiationApplication.java
│   │   │               ├── controller
│   │   │               │   └── TripInitiationController.java
│   │   │               ├── service
│   │   │               │   ├── TripInitiationService.java
│   │   │               │   └── GoogleMapsService.java
│   │   │               ├── model
│   │   │               │   ├── mongodb
│   │   │               │   │   ├── TripPlan.java
│   │   │               │   │   ├── DailyPlan.java
│   │   │               │   │   ├── Place.java
│   │   │               │   │   ├── Location.java
│   │   │               │   │   └── InitiatedTrip.java
│   │   │               │   └── postgresql
│   │   │               │       ├── VehicleType.java
│   │   │               │       └── GuideFee.java
│   │   │               ├── repository
│   │   │               │   ├── mongodb
│   │   │               │   │   ├── TripPlanRepository.java
│   │   │               │   │   └── InitiatedTripRepository.java
│   │   │               │   └── postgresql
│   │   │               │       ├── VehicleTypeRepository.java
│   │   │               │       └── GuideFeeRepository.java
│   │   │               ├── dto
│   │   │               │   ├── TripInitiationRequest.java
│   │   │               │   ├── TripInitiationResponse.java
│   │   │               │   └── RouteSummary.java
│   │   │               ├── config
│   │   │               │   └── GoogleMapsConfig.java
│   │   │               └── exception
│   │   │                   ├── TripNotFoundException.java
│   │   │                   ├── VehicleTypeNotFoundException.java
│   │   │                   └── GlobalExceptionHandler.java
│   │   └── resources
│   │       ├── application.yml
│   │       └── application-dev.yml
├── pom.xml
└── README.md
```

## Setup Instructions

### Prerequisites
- Java 17
- Maven
- MongoDB
- PostgreSQL
- Google Maps API key

### Installation
1. Clone the repository:
   ```
   git clone <repository-url>
   cd trip-initiation-microservice
   ```

2. Update the `application.yml` and `application-dev.yml` files with your MongoDB and PostgreSQL connection details, as well as your Google Maps API key.

3. Build the project using Maven:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   mvn spring-boot:run
   ```

### API Endpoints
- **POST /api/trips/initiate**
  - Initiates a trip based on the provided request body.
  - Request body should include:
    - `userId`: The ID of the user initiating the trip.
    - `tripId`: The ID of the trip plan.
    - `setDriver`: Indicates if a driver is needed (1 for yes, 0 for no).
    - `setGuide`: Indicates if a guide is needed (1 for yes, 0 for no).
    - `preferredVehicleTypeId`: The ID of the preferred vehicle type.

### Error Handling
The application includes global exception handling to manage various error scenarios, such as:
- Trip not found
- Vehicle type not found
- Google Maps API failures
- Database connection issues

### Logging
Key operations are logged for monitoring and debugging purposes.

## Conclusion
This microservice provides a robust solution for initiating travel trips, integrating with MongoDB for data storage and PostgreSQL for pricing information, while leveraging the Google Maps API for route calculations.