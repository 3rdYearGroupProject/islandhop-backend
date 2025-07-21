# Trip Planning Service
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'Trip Planning Service'; cd d:\groupProject\islandhop-backend\trip-planning-service; mvn spring-boot:run"

# User Services
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'User Services'; cd d:\groupProject\islandhop-backend\user-services; mvn spring-boot:run"

# Pooling Service
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'Pooling Service'; cd d:\groupProject\islandhop-backend\pooling-service; mvn spring-boot:run"


Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'Firebase User Microservice'; cd d:\groupProject\islandhop-backend\firebase-user-microservice; mvn spring-boot:run"