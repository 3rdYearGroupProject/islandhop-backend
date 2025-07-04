@echo off
echo ========================================
echo   Trip Planning Service Setup Script
echo ========================================

echo.
echo This script will help you set up the Trip Planning Service
echo.

echo 1. Checking prerequisites...

echo Checking Java 17...
java -version 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: Java not found! Please install Java 17
    pause
    exit /b 1
)

echo Checking Maven...
call mvn -version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven not found! Please install Maven
    pause
    exit /b 1
)

echo Checking Docker...
docker --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo WARNING: Docker not found! You'll need to install MongoDB manually
) else (
    echo Docker found - you can use Docker Compose
)

echo.
echo 2. Setting up environment file...
if not exist ".env" (
    if exist ".env.template" (
        copy ".env.template" ".env"
        echo Created .env file from template
        echo IMPORTANT: Please edit .env file and add your API keys!
    ) else (
        echo WARNING: .env.template not found!
    )
) else (
    echo .env file already exists
)

echo.
echo 3. Building the application...
call build.bat

echo.
echo ========================================
echo   SETUP COMPLETE!
echo ========================================
echo.

echo Next steps:
echo   1. Edit .env file and add your API keys:
echo      - TRIPADVISOR_API_KEY
echo      - GOOGLE_MAPS_API_KEY
echo      - OPENWEATHER_API_KEY (optional)
echo.
echo   2. Start the services:
echo      Option A - With Docker:
echo        docker-compose up -d
echo.
echo      Option B - Manual:
echo        - Start MongoDB on port 27017
echo        - Run: java -jar target/trip-planning-service-0.0.1-SNAPSHOT.jar
echo.
echo   3. Verify the service:
echo      - Health check: http://localhost:8083/api/trip/health
echo      - MongoDB UI: http://localhost:8084 (if using Docker)
echo.
echo   4. Test integration with user-services on port 8081
echo.

pause
