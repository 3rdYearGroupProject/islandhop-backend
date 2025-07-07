# Environment Setup for Admin Service

## Configuration

This service uses environment variables for sensitive configuration. Follow these steps to set up your environment:

### 1. Create Environment File

Copy the example environment file and fill in your actual values:

```bash
cp .env.example .env
```

### 2. Update .env File

Edit the `.env` file with your actual credentials:

- **Redis (Upstash)**: Update with your Upstash Redis credentials
- **MongoDB (Atlas)**: Update with your MongoDB Atlas connection string
- **Firebase**: Update with your Firebase project ID

### 3. Environment Variables

The following environment variables are supported:

| Variable              | Description            | Default                         |
| --------------------- | ---------------------- | ------------------------------- |
| `REDIS_HOST`          | Redis server hostname  | localhost                       |
| `REDIS_PORT`          | Redis server port      | 6379                            |
| `REDIS_PASSWORD`      | Redis password         | (empty)                         |
| `REDIS_SSL_ENABLED`   | Enable SSL for Redis   | false                           |
| `MONGODB_URI`         | MongoDB connection URI | mongodb://localhost:27017/admin |
| `MONGODB_DATABASE`    | MongoDB database name  | admin                           |
| `FIREBASE_PROJECT_ID` | Firebase project ID    | default-project                 |
| `SERVER_PORT`         | Application port       | 8090                            |

### 4. Security Notes

- ⚠️ **NEVER** commit the `.env` file to version control
- The `.env` file is already added to `.gitignore`
- Use `.env.example` as a template for new developers
- Keep your credentials secure and rotate them regularly

### 5. Running the Service

The service will automatically load environment variables from the `.env` file when running locally:

```bash
mvn spring-boot:run
```

For production deployment, set these environment variables in your deployment platform (Docker, Kubernetes, cloud providers, etc.).
