@echo off
setlocal enabledelayedexpansion

echo Starting all IslandHop backend microservices...
echo.

REM Ask user for preference
echo Choose an option:
echo 1. Install dependencies and run all services (mvn clean install ^&^& mvn spring-boot:run)
echo 2. Just run all services (mvn spring-boot:run)
echo.
set /p choice="Enter your choice (1 or 2): "

if "%choice%"=="1" (
    set "COMMAND=mvn clean install && mvn spring-boot:run"
    set "ACTION=Installing dependencies and starting"
) else if "%choice%"=="2" (
    set "COMMAND=mvn spring-boot:run"
    set "ACTION=Starting"
) else (
    echo Invalid choice. Defaulting to option 1 (install and run)
    set "COMMAND=mvn clean install && mvn spring-boot:run"
    set "ACTION=Installing dependencies and starting"
)

echo.
echo Selected: !ACTION! all services...
echo.

REM List of Java microservice folders
set SERVICES=admin-service api-usage-service booking-service chat-notification-service emergency-services firebase-user-microservice payhere-payment-service pooling-service review-service trip-initiation-microservice trip-planning-service user-services

for %%S in (%SERVICES%) do (
    if exist %%S (
        cd %%S
        if exist pom.xml (
            echo !ACTION! %%S...
            start "%%S - !ACTION!" cmd /k "!COMMAND!"
        ) else (
            echo Skipping %%S: No pom.xml found
        )
        cd ..
    ) else (
        echo Skipping %%S: Folder does not exist
    )
)

echo.
echo All Java microservices have been started in separate windows!
echo.
echo Press any key to exit...
pause >nul
endlocal
