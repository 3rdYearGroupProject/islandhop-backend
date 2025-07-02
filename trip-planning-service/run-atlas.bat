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
set TRIPADVISOR_API_KEY=83917CC8B63044EB954118EC2FD0F1AC
set GOOGLE_PLACES_API_KEY=AIzaSyA5osDKPs3jttq1AuEIOGTOBvrx8tfTcPY
set GOOGLE_MAPS_API_KEY=AIzaSyCtvdlyAbtALV62EAWT1qIFy2OLxuMljmk


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
echo Using MongoDB Atlas: %MONGODB_URI%
echo Server will start on port: %SERVER_PORT%
echo.

REM Build and run the application with MongoDB Atlas
mvn clean spring-boot:run

echo.
echo Service startup completed.
pause
