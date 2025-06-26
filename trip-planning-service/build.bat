@echo off
echo ========================================
echo   Trip Planning Service Build Script
echo ========================================

echo.
echo Checking Java version...
java -version
echo.

echo Checking Maven version...
call mvn -version
echo.

echo 1. Cleaning previous builds...
call mvn clean
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven clean failed!
    pause
    exit /b 1
)

echo.
echo 2. Compiling and running tests...
call mvn compile test
if %ERRORLEVEL% neq 0 (
    echo ERROR: Compilation or tests failed!
    pause
    exit /b 1
)

echo.
echo 3. Packaging application...
call mvn package -DskipTests
if %ERRORLEVEL% neq 0 (
    echo ERROR: Packaging failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   BUILD SUCCESSFUL!
echo ========================================
echo JAR file: target/trip-planning-service-0.0.1-SNAPSHOT.jar
echo.

echo Available commands:
echo   Run with Java:     java -jar target/trip-planning-service-0.0.1-SNAPSHOT.jar
echo   Run with Maven:    mvn spring-boot:run
echo   Run with Docker:   docker-compose up --build
echo   Development mode:  docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
echo.

echo Note: Make sure to:
echo   1. Copy .env.template to .env and add your API keys
echo   2. Start MongoDB or use Docker Compose
echo   3. Ensure user-services is running on port 8081
echo.

pause
