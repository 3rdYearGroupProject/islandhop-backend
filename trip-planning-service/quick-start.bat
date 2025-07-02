@echo off
echo ========================================
echo   Quick Start - Trip Planning Service
echo ========================================
echo.
echo Starting with MongoDB Atlas...
echo.

REM Set environment variables
set MONGODB_URI=mongodb+srv://2022cs056:dH4aTFn3IOerWlVZ@cluster0.9ccambx.mongodb.net/islandhop_trips?retryWrites=true^&w=majority
set SPRING_PROFILES_ACTIVE=development
set SERVER_PORT=8083
set TRIPADVISOR_API_KEY=83917CC8B63044EB954118EC2FD0F1AC
set GOOGLE_PLACES_API_KEY=AIzaSyA5osDKPs3jttq1AuEIOGTOBvrx8tfTcPY
set GOOGLE_MAPS_API_KEY=AIzaSyCtvdlyAbtALV62EAWT1qIFy2OLxuMljmk

echo Running Spring Boot application...
echo.
echo Service will start on: http://localhost:8083/api
echo MongoDB Atlas: Connected
echo.

mvn spring-boot:run

pause
