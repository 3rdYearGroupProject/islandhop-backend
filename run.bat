@echo off
setlocal enabledelayedexpansion

echo Starting all IslandHop backend microservices...
echo.

REM List of Java microservice folders
set SERVICES=admin-service api-usage-service booking-service chat-notification-service emergency-services firebase-user-microservice payhere-payment-service pooling-service review-service trip-initiation-microservice trip-planning-service user-services

for %%S in (%SERVICES%) do (
    if exist %%S (
        cd %%S
        if exist pom.xml (
            echo Installing dependencies and starting %%S...
            start "%%S - Install & Run" cmd /k "mvn clean install && mvn spring-boot:run"
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
