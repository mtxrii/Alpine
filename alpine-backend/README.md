# Alpine Backend

Alpine Backend is the Spring Boot API for a WikiForge/Kanban board system. It provides authenticated REST endpoints for users, workspaces, boards, columns, tickets, labels, comments, and activity logs.

The service uses PostgreSQL for persistence, Flyway for schema migrations, and stateless JWT bearer tokens for authentication. The complete API contract is available in [`openapi.yaml`](openapi.yaml).

## Requirements

- Java 26 (the Gradle toolchain is configured for Java 26)
- Docker and Docker Compose, for the local PostgreSQL database

The repository includes the Gradle wrapper, so a separate Gradle installation is not required.

## Run locally

1. Start PostgreSQL:

   ```bash
   docker compose up -d postgres
   ```

2. Start the API:

   ```bash
   ./gradlew bootRun
   ```

   On Windows, use `gradlew.bat bootRun`.

The API listens on <http://localhost:8080> by default. Flyway runs the SQL migrations in `src/main/resources/db/migration` automatically when the application starts.

To stop the database container:

```bash
docker compose down
```

The database is stored in the `postgres_data` Docker volume. To remove the database and its data as well, run `docker compose down -v`.

## Configuration

The default local configuration connects to the PostgreSQL instance created by `docker-compose.yml`:

| Variable | Default | Description |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/alpine` | JDBC connection URL |
| `DATABASE_USERNAME` | `postgres` | Database user |
| `DATABASE_PASSWORD` | `postgres` | Database password |
| `PORT` | `8080` | HTTP port |
| `JWT_ISSUER` | `alpine-backend` | JWT issuer claim |
| `JWT_SECRET` | development placeholder | HMAC signing secret; must be at least 32 bytes |
| `JWT_ACCESS_TOKEN_TTL` | `PT2H` | Access-token lifetime, using ISO-8601 duration syntax |

For anything other than local development, set a strong `JWT_SECRET` and provide database credentials through the environment rather than using the defaults.

Example:

```bash
JWT_SECRET='replace-with-a-secret-at-least-32-bytes-long' ./gradlew bootRun
```

## Authentication and API usage

Registration and login are public:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/logout`

All other application endpoints require an access token in the request header:

```text
Authorization: Bearer <access-token>
```

See [`openapi.yaml`](openapi.yaml) for request bodies, response schemas, and the full endpoint list.

## Tests

Run the test suite with:

```bash
./gradlew test
```

The integration test uses Testcontainers and starts a PostgreSQL 16 container, so Docker must be available when running the full suite.

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
