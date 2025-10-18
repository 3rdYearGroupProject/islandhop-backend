@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo   Starting all IslandHop backend microservices (JAR mode)
echo ======================================================
echo.

REM Ask user for preference
echo Choose an option:
echo 1. Build all services and then run JARs (mvn clean package -DskipTests)
echo 2. Just run existing JARs
echo.
set /p choice="Enter your choice (1 or 2): "

if "%choice%"=="1" (
    set "COMMAND=BUILD_AND_RUN"
    set "ACTION=Building and starting"
) else if "%choice%"=="2" (
    set "COMMAND=RUN_ONLY"
    set "ACTION=Starting"
) else (
    echo Invalid choice. Defaulting to option 1 (build and run)
    set "COMMAND=BUILD_AND_RUN"
    set "ACTION=Building and starting"
)

echo.
echo Selected: !ACTION! all services...
echo.

REM List of microservice folders
set SERVICES=admin-service api-usage-service booking-service chat-notification-service emergency-services firebase-user-microservice payhere-payment-service pooling-service review-service trip-initiation-microservice trip-planning-service user-services

for %%S in (%SERVICES%) do (
    if exist %%S (
        cd %%S
        if exist pom.xml (
            echo --------------------------------------------
            echo Processing %%S...
            
            if "!COMMAND!"=="BUILD_AND_RUN" (
                echo Building %%S...
                call mvn clean package -DskipTests >NUL
            )

            REM Locate the built JAR file (in target folder)
            for /f "delims=" %%J in ('dir /b /a:-d "target\*.jar" ^| findstr /v "original"') do (
                set "JAR_FILE=%%J"
            )

            if defined JAR_FILE (
                echo Starting %%S with !JAR_FILE!...
                start "%%S - !ACTION!" cmd /k "java -jar target\!JAR_FILE!"
            ) else (
                echo Skipping %%S: No JAR file found in target folder.
            )

            set "JAR_FILE="
        ) else (
            echo Skipping %%S: No pom.xml found
        )
        cd ..
    ) else (
        echo Skipping %%S: Folder does not exist
    )
)

echo.
echo ======================================================
echo All Java microservices have been started in separate windows!
echo ======================================================
echo.
echo Press any key to exit...
pause >nul
endlocal
