# IslandHop Emergency Services Microservice

This microservice handles emergency alerts and notifications for the IslandHop platform. It manages panic button triggers, alert delivery, and emergency response coordination.

## Features

- Panic button trigger handling
- Real-time alert delivery to involved parties
- Geolocation tracking
- Multi-channel notifications (in-app, email, SMS)
- Alert status tracking and management

## Prerequisites

- Java 17
- Maven
- PostgreSQL
- Redis
- Firebase Admin SDK credentials
- AWS SNS configuration
- Twilio account (for SMS notifications)

## Setup

1. Clone the repository
2. Configure the following in `application.properties`:
   - Database credentials
   - Redis connection
   - Firebase credentials path
   - AWS SNS topic ARN
   - Twilio credentials

3. Place your Firebase credentials JSON file in `src/main/resources/firebase-credentials.json`

4. Build the project:
```bash
mvn clean install
```

5. Run the application:
```bash
mvn spring-boot:run
```

## API Endpoints

### Emergency Alerts

- `POST /emergency/trigger` - Trigger a new emergency alert
- `GET /emergency/status/{alertId}` - Get alert status
- `PUT /emergency/resolve/{alertId}` - Mark alert as resolved

## Security

- All endpoints require authentication
- Emergency triggers are only available during active bookings
- Support/admin roles required for alert resolution

## Dependencies

- Spring Boot 3.2.3
- Spring Data JPA
- Spring Data Redis
- Firebase Admin SDK
- AWS SNS
- Twilio SDK
- PostgreSQL
- Lombok

## Configuration

The service uses the following external services:
- PostgreSQL for data persistence
- Redis for caching
- Firebase for push notifications
- AWS SNS for message delivery
- Twilio for SMS notifications

## Development

To run tests:
```bash
mvn test
```

## License

This project is proprietary and confidential. 