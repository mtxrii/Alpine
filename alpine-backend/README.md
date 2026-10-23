# Alpine Backend

Alpine Backend is the Spring Boot API for a WikiForge/Kanban board system. It provides authenticated REST endpoints for users, workspaces, boards, columns, tickets, labels, comments, and activity logs.

The service uses PostgreSQL for persistence, Flyway for schema migrations, and stateless JWT bearer tokens for authentication. The complete API contract is available in [`openapi.yaml`](openapi.yaml).

## Table of Contents

- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Authentication and API usage](#authentication-and-api-usage)
- [Tests](#tests)
- [Project layout](#project-layout)

## Requirements

- Java 26 (the Gradle toolchain is configured for Java 26)
- Docker and Docker Compose, for the local PostgreSQL database

The repository includes the Gradle wrapper, so a separate Gradle installation is not required.

## Getting Started

### 1. Start the Database

Ensure Docker is running, then start the PostgreSQL instance:

```bash
docker compose up -d postgres
```

### 2. Start the API

Run the application using the Gradle wrapper:

```bash
# macOS/Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

The API will be available at [http://localhost:8080](http://localhost:8080).

### Managing the Database

- **Stop the database:**
  ```bash
  docker compose down
  ```
- **Remove database and data:**
  ```bash
  docker compose down -v
  ```

## Configuration

The default local configuration connects to the PostgreSQL instance created by `docker-compose.yml`:

| Variable | Default | Description |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/alpine` | JDBC connection URL |
| `DATABASE_USERNAME` | `postgres` | Database user |
| `DATABASE_PASSWORD` | `postgres` | Database password |
| `PORT` | `8080` | HTTP port |
| `JWT_ISSUER` | `alpine-backend` | JWT issuer claim |
| `JWT_SECRET` | development placeholder | HMAC signing secret (must be ≥ 32 bytes) |
| `JWT_ACCESS_TOKEN_TTL` | `PT2H` | Access-token lifetime (ISO-8601) |

For non-development environments, always provide a strong `JWT_SECRET` and secure database credentials.

Example with custom secret:
```bash
JWT_SECRET='replace-with-a-secret-at-least-32-bytes-long' ./gradlew bootRun
```

## Authentication and API usage

Registration and login are public:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/logout`

All other endpoints require an access token in the request header:

```text
Authorization: Bearer <access-token>
```

See [`openapi.yaml`](openapi.yaml) for full API details.

## Tests

Run the test suite:

```bash
./gradlew test
```

*Note: Integration tests use Testcontainers and require Docker.*

## Project layout

```text
src/main/java/.../auth       Authentication and JWT handling
src/main/java/.../board      Board endpoints and services
src/main/java/.../column     Board-column endpoints and services
src/main/java/.../ticket     Ticket endpoints, labels, and assignees
src/main/java/.../workspace  Workspace and membership endpoints
src/main/java/.../comment    Ticket comments
src/main/java/.../activity   Board and ticket activity logs
src/main/resources/db        Flyway database migrations
openapi.yaml                 OpenAPI 3 API specification
docker-compose.yml           Local PostgreSQL service
```
