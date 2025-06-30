@echo off
echo Building and running Pooling Service...

echo.
echo 1. Cleaning and building...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo 2. Starting Pooling Service on port 8086...
echo Make sure MongoDB is running on localhost:27017
echo Make sure Trip Planning Service is running on port 8082
echo Make sure User Services is running on port 8081
echo.

java -jar target/pooling-service-0.0.1-SNAPSHOT.jar

pause
