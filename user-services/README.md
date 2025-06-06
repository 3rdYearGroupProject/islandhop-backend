# User Services for Tourism Platform

This project is a RESTful microservice called `user-services` designed for managing tourist accounts on a tourism platform. It is built using Spring Boot and integrates with Firebase for authentication, PostgreSQL for data storage, and Redis for caching.

## Technologies Used

- **Java**: The programming language used for development.
- **Spring Boot**: Framework for building the RESTful microservice.
- **Firebase Authentication**: Used for user authentication and token verification.
- **PostgreSQL**: Relational database for storing user data.
- **Redis**: In-memory data structure store used for caching and OTP management.
- **AWS SNS**: For sending verification emails (or a simple SMTP stub if AWS libraries are not available).
- **Docker**: For containerization and deployment on AWS ECS.

## Features

- **User Registration**: Tourists can register with their details.
- **User Login**: Authentication via Firebase, providing user profile information.
- **Account Management**: Update account details, deactivate, and delete accounts.
- **OTP Verification**: Send and verify OTPs for account actions.
- **Error Handling**: Centralized exception handling for better error management.

## Endpoints

1. **Register a Tourist**
   - `POST /register`
   - Auth required: Yes

2. **Log In**
   - `GET /me`
   - Auth required: Yes

3. **Update Account Details**
   - `PATCH /update`
   - Auth required: Yes

4. **Account Deactivation**
   - `POST /deactivate`
   - Auth required: Yes

5. **Account Deletion**
   - `DELETE /delete`
   - Auth required: Yes

6. **Send Verification Code (OTP)**
   - `POST /verify/send`
   - Auth required: Yes

7. **Verify OTP**
   - `POST /verify/check`
   - Auth required: Yes

## Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd user-services
   ```

2. **Configure Database and Redis**
   Update the `application.yml` file with your PostgreSQL and Redis configurations.

3. **Build the Application**
   Use Gradle to build the application:
   ```bash
   ./gradlew build
   ```

4. **Run the Application**
   You can run the application locally using:
   ```bash
   ./gradlew bootRun
   ```

5. **Dockerize the Application**
   Build the Docker image:
   ```bash
   docker build -t user-services .
   ```

6. **Deploy to AWS**
   Follow AWS documentation to deploy the Docker container to ECS, and set up RDS for PostgreSQL and ElastiCache for Redis.

## Health Check

The application exposes a health check endpoint at `/health` to monitor its status.

## License

This project is licensed under the MIT License. See the LICENSE file for more details.