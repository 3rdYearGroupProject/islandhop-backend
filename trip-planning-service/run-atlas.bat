@echo off
echo ========================================
echo   Trip Planning Service - Atlas Mode
echo ========================================
echo.
echo Starting Trip Planning Service with MongoDB Atlas...
echo.

REM Set environment variables for MongoDB Atlas
set MONGODB_URI=mongodb+srv://2022cs056:dH4aTFn3IOerWlVZ@cluster0.9ccambx.mongodb.net/islandhop_trips?retryWrites=true^&w=majority
set SPRING_PROFILES_ACTIVE=development
set SERVER_PORT=8083


echo Checking prerequisites...
echo.

REM Check Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java not found. Please install Java 17 or higher.
    pause
    exit /b 1
)
echo ✓ Java found

REM Check Maven
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven not found. Please install Maven.
    pause
    exit /b 1
)
echo ✓ Maven found

echo.
echo Building and starting the application...
echo.

REM Build and run the application with MongoDB Atlas
mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=development"

echo.
echo Service startup completed.
pause
