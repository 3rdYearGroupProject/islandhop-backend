# Review Service

## Overview
The Review Service is a microservice designed to handle reviews for drivers and guides in the tourism platform. It evaluates reviews using a machine learning model and manages the review lifecycle, including submission, approval, and status updates.

## Features
- **Review Submission**: Tourists can submit reviews for drivers and guides.
- **Machine Learning Evaluation**: Reviews are evaluated on a scale of 0-10, with ratings from 0 (inappropriate) to 10 (good).
- **Review Management**: The service maintains separate tables for driver reviews and guide reviews, each with fields for review ID, email, review text, rating, and status.
- **Pending Reviews**: Reviews that cannot be automatically classified are stored in a separate table for manual review.
- **Endpoints**: The service provides endpoints to submit reviews, retrieve reviews by email, view pending reviews, and change review statuses.

## Project Structure
```
review-service
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── islandhop
│   │   │           └── reviewservice
│   │   │               ├── ReviewServiceApplication.java
│   │   │               ├── config
│   │   │               │   ├── DatabaseConfig.java
│   │   │               │   └── MLModelConfig.java
│   │   │               ├── controller
│   │   │               │   ├── DriverReviewController.java
│   │   │               │   ├── GuideReviewController.java
│   │   │               │   └── PendingReviewController.java
│   │   │               ├── dto
│   │   │               │   ├── ReviewRequest.java
│   │   │               │   ├── ReviewResponse.java
│   │   │               │   └── MLConfigRequest.java
│   │   │               ├── entity
│   │   │               │   ├── DriverReview.java
│   │   │               │   ├── GuideReview.java
│   │   │               │   └── PendingReview.java
│   │   │               ├── enums
│   │   │               │   └── ReviewStatus.java
│   │   │               ├── repository
│   │   │               │   ├── DriverReviewRepository.java
│   │   │               │   ├── GuideReviewRepository.java
│   │   │               │   └── PendingReviewRepository.java
│   │   │               ├── service
│   │   │               │   ├── MLModelService.java
│   │   │               │   ├── DriverReviewService.java
│   │   │               │   ├── GuideReviewService.java
│   │   │               │   └── PendingReviewService.java
│   │   │               └── util
│   │   │                   └── ReviewValidator.java
│   │   └── resources
│   │       ├── application.yml
│   │       └── db
│   │           └── migration
│   │               ├── V1__Create_driver_review_table.sql
│   │               ├── V2__Create_guide_review_table.sql
│   │               └── V3__Create_pending_review_table.sql
│   └── test
│       └── java
│           └── com
│               └── islandhop
│                   └── reviewservice
│                       └── ReviewServiceApplicationTests.java
├── .env
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Getting Started
1. **Clone the Repository**: Clone this repository to your local machine.
2. **Configure Database**: Update the `application.yml` file with your PostgreSQL database connection details.
3. **Run Migrations**: Execute the SQL migration scripts located in `src/main/resources/db/migration` to create the necessary tables in your database.
4. **Build the Project**: Use Maven to build the project by running `mvn clean install`.
5. **Run the Application**: Start the application using `mvn spring-boot:run`.

## API Endpoints
- **Submit Review**: `POST /reviews/driver` or `POST /reviews/guide`
- **Get Reviews by Email**: `GET /reviews/driver?email={email}` or `GET /reviews/guide?email={email}`
- **View Pending Reviews**: `GET /reviews/pending`
- **Change Review Status**: `PATCH /reviews/pending/{reviewId}`

## License
This project is licensed under the MIT License. See the LICENSE file for details.