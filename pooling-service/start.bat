@echo off
echo Starting IslandHop Pooling Service...
echo.

rem Set default cloud environment variables if not already set
if "%MONGODB_URI%"=="" set MONGODB_URI=mongodb+srv://2022cs056:dH4aTFn3IOerWlVZ@cluster0.9ccambx.mongodb.net/islandhop_pooling?retryWrites=true^&w=majority
if "%KAFKA_BOOTSTRAP_SERVERS%"=="" set KAFKA_BOOTSTRAP_SERVERS=pkc-12576z.us-west2.gcp.confluent.cloud:9092
if "%ITINERARY_SERVICE_URL%"=="" set ITINERARY_SERVICE_URL=https://your-itinerary-service.herokuapp.com
if "%PORT%"=="" set PORT=8081

echo Configuration:
echo - MongoDB: %MONGODB_URI%
echo - Kafka: %KAFKA_BOOTSTRAP_SERVERS%
echo - Itinerary Service: %ITINERARY_SERVICE_URL%
echo - Port: %PORT%
echo.

echo Checking if Maven is installed...
mvn --version
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

echo.
echo Building the project...
mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Starting the application...
echo The service will be available at: http://localhost:%PORT%
echo Swagger UI will be available at: http://localhost:%PORT%/swagger-ui.html
echo Health check available at: http://localhost:%PORT%/actuator/health
echo.
echo Press Ctrl+C to stop the service
echo.

mvn spring-boot:run
