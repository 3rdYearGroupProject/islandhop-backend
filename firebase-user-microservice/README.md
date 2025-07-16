# Firebase User Microservice

This project is a Spring Boot microservice that interacts with Firebase Authentication to manage user-related operations. It provides RESTful endpoints for various user management functionalities.

## Features

- Initialize Firebase Admin SDK using a service account JSON file.
- Get user display name by UID.
- Delete a user account by UID.
- Deactivate (disable) a user account.
- Reactivate (enable) a user account.
- Get full user info from UID (email, displayName, disabled, phoneNumber, etc.).

## Tech Stack

- Java 17+
- Spring Boot (Web + Validation)
- Firebase Admin SDK
- SLF4J for logging

## Project Structure

```
firebase-user-microservice
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── firebaseuser
│   │   │               ├── controller
│   │   │               │   └── FirebaseUserController.java
│   │   │               ├── service
│   │   │               │   └── FirebaseUserService.java
│   │   │               ├── config
│   │   │               │   └── FirebaseConfig.java
│   │   │               └── model
│   │   │                   └── UserInfoDTO.java
│   │   └── resources
│   │       └── application.properties
├── pom.xml
└── README.md
```

## Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd firebase-user-microservice
   ```

2. **Add Firebase Service Account Key:**
   - Obtain your Firebase service account key JSON file from the Firebase Console.
   - Place the JSON file in the `src/main/resources` directory.
   - Update the `application.properties` file with the path to your service account key.

3. **Build the project:**
   ```bash
   mvn clean install
   ```

4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

## Usage

- **Get User Display Name:**
  - `GET /api/firebase/user/display-name/{uid}`

- **Delete User Account:**
  - `DELETE /api/firebase/user/{uid}`

- **Deactivate User Account:**
  - `PUT /api/firebase/user/deactivate/{uid}`

- **Reactivate User Account:**
  - `PUT /api/firebase/user/activate/{uid}`

- **Get Full User Info:**
  - `GET /api/firebase/user/info/{uid}`

## Logging

The application uses SLF4J for logging. Logs are generated at INFO and DEBUG levels for all methods.

## Error Handling

The microservice includes proper error handling, returning appropriate HTTP status codes and custom error messages for various scenarios.

## License

This project is licensed under the MIT License.