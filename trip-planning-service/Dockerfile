FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the Maven build output
COPY target/trip-planning-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8083

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8083/api/trip/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
