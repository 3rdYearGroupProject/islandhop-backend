@echo off
echo Building Trip Planning Service...

echo.
echo 1. Cleaning previous builds...
call mvn clean

echo.
echo 2. Compiling and packaging...
call mvn package -DskipTests

echo.
echo 3. Build completed!
echo JAR file: target/trip-planning-service-0.0.1-SNAPSHOT.jar

echo.
echo To run the service:
echo   java -jar target/trip-planning-service-0.0.1-SNAPSHOT.jar
echo.
echo Or use Maven:
echo   mvn spring-boot:run
echo.

pause
