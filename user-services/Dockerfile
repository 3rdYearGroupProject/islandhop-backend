FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the build artifacts from the host to the container
COPY build/libs/user-services-*.jar app.jar

# Expose the application port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]