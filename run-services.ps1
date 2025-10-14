# Trip Planning Service
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'Trip Planning Service'; cd d:\groupProject\islandhop-backend\trip-planning-service; mvn spring-boot:run"

# User Services
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'User Services'; cd d:\groupProject\islandhop-backend\user-services; mvn spring-boot:run"

# Pooling Service
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'Pooling Service'; cd d:\groupProject\islandhop-backend\pooling-service; mvn spring-boot:run"

# Trip Initiation Service
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'Trip Initiation Service'; cd d:\groupProject\islandhop-backend\trip-initiation-microservice; mvn spring-boot:run"

# Admin Service
Start-Process powershell -ArgumentList "-NoExit -Command `$host.ui.RawUI.WindowTitle = 'Admin Service'; cd d:\groupProject\islandhop-backend\admin-service; mvn spring-boot:run"
