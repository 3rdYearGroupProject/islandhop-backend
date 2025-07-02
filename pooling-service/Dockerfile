# Multi-stage build for production deployment
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml for dependency caching
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Create non-root user for security
RUN addgroup --system pooling && adduser --system --group pooling

# Copy only the built JAR from builder stage
COPY --from=builder /app/target/pooling-service-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown pooling:pooling app.jar

USER pooling

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8086/api/health || exit 1

# Run application
EXPOSE 8086
CMD ["java", "-jar", "app.jar"]