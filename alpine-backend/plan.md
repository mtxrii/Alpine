# Kanban Backend Implementation Plan (Java + Spring Boot)

## Goal

Build a production-ready Kanban backend API using:

- Java 21
- Spring Boot 3.x
- PostgreSQL
- Flyway
- Spring Security + JWT
- JPA / Hibernate
- Maven
- Docker
- Testcontainers

The backend should support:

- Authentication
- Workspaces
- Boards
- Columns
- Tickets
- Drag-and-drop movement
- Assignments
- Labels
- Comments
- Activity logs

This document is designed to be consumed by AI coding agents (Codex/Cursor/etc.) and implemented incrementally.

---

# High-Level Architecture

```text
Client (React/Vue/etc.)
        |
        v
Spring Boot REST API
        |
        +-- Spring Security JWT
        +-- Service Layer
        +-- JPA/Hibernate
        +-- PostgreSQL
        +-- Flyway Migrations
        +-- WebSocket (optional later)
```

---

# Recommended Versions

| Component | Version |
|---|---|
| Java | 21 |
| Spring Boot | 3.3+ |
| Maven | Latest |
| PostgreSQL | 16 |
| Flyway | Latest |
| JWT | Spring Security OAuth2 Resource Server |
| Docker | Latest |

Spring Boot 3.x + Java 21 is the recommended modern stack. :contentReference[oaicite:0]{index=0}

---

# Step 1 — Initialize the Project

## Create Spring Boot App

Use Spring Initializr.

Dependencies:

- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- PostgreSQL Driver
- Flyway Migration
- Lombok
- OAuth2 Resource Server
- Actuator
- Testcontainers
- Spring Boot Test

---

# Step 2 — Create Project Structure

Use a modular monolith structure.

```text
src/main/java/com/example/kanban
|
+-- auth
+-- user
+-- workspace
+-- board
+-- column
+-- ticket
+-- comment
+-- label
+-- activity
+-- common
+-- config
+-- security
```

Each module should contain:

```text
controller/
service/
repository/
entity/
dto/
mapper/
```

---

# Step 3 — Configure application.yml

Create:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/alpine
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate

    properties:
      hibernate:
        format_sql: true

  flyway:
    enabled: true

server:
  port: 8080
```

Important:

NEVER use:

```yaml
ddl-auto: update
```

Use Flyway migrations instead. :contentReference[oaicite:1]{index=1}

---

# Step 4 — Setup Docker Infrastructure

Create:

```text
docker-compose.yml
```

Include:

- PostgreSQL
- pgAdmin (optional)

Example:

```yaml
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: alpine
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres

    ports:
      - "5432:5432"
```

---

# Step 5 — Configure Flyway

Create migration folder:

```text
src/main/resources/db/migration
```

Create initial migration:

```text
V1__initial_schema.sql
```

All schema changes MUST use versioned migrations. :contentReference[oaicite:2]{index=2}

---

# Step 6 — Create Base Entity

Create abstract base class:

```java
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
```

All entities should extend this class.

---

# Step 7 — Implement Core Entities

Implementation order:

1. User
2. Workspace
3. WorkspaceMember
4. Board
5. Column
6. Ticket
7. TicketAssignee
8. Label
9. TicketLabel
10. Comment
11. ActivityLog

---

# Step 8 — Create JPA Relationships

Use:

- `@ManyToOne`
- `@OneToMany`
- `@ManyToMany`

Prefer explicit join entities instead of implicit many-to-many tables.

Good:

```java
TicketAssignee
```

Avoid hidden JPA many-to-many tables.

---

# Step 9 — Implement Repositories

Create Spring Data repositories.

Example:

```java
public interface TicketRepository
    extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByBoardId(UUID boardId);
}
```

Keep repositories thin.

Business logic belongs in services.

---

# Step 10 — Implement DTOs

Never expose entities directly.

Create:

## Request DTOs

```text
CreateTicketRequest
UpdateTicketRequest
MoveTicketRequest
```

## Response DTOs

```text
TicketResponse
BoardResponse
CommentResponse
```

---

# Step 11 — Add Validation

Use Jakarta Validation.

Example:

```java
@NotBlank
private String title;
```

Validate:

- title lengths
- required fields
- enum values

---

# Step 12 — Implement Authentication

Use JWT authentication.

Recommended approach:

- Spring Security 6
- OAuth2 Resource Server
- Stateless JWT auth

Avoid legacy custom JWT filters if possible. :contentReference[oaicite:3]{index=3}

---

# Step 13 — Create User Registration/Login

Endpoints:

```text
POST /auth/register
POST /auth/login
GET  /auth/me
```

Store passwords using:

```java
BCryptPasswordEncoder
```

---

# Step 14 — Configure Spring Security

Create:

```text
SecurityConfig.java
```

Requirements:

- Stateless sessions
- JWT validation
- Protected API routes
- Public auth endpoints

Example:

```java
http
    .csrf(AbstractHttpConfigurer::disable)
    .sessionManagement(
        s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );
```

Stateless JWT auth is the recommended modern approach. :contentReference[oaicite:4]{index=4}

---

# Step 15 — Implement Board APIs

Endpoints:

```text
GET    /boards
POST   /boards
GET    /boards/{id}
PATCH  /boards/{id}
DELETE /boards/{id}
```

---

# Step 16 — Implement Column APIs

Endpoints:

```text
GET    /boards/{id}/columns
POST   /boards/{id}/columns
PATCH  /columns/{id}
DELETE /columns/{id}
```

Columns require:

```text
position
```

for ordering.

---

# Step 17 — Implement Ticket APIs

Endpoints:

```text
GET    /boards/{id}/tickets
POST   /boards/{id}/tickets
GET    /tickets/{id}
PATCH  /tickets/{id}
DELETE /tickets/{id}
```

---

# Step 18 — Implement Drag-and-Drop Movement

Critical feature.

Endpoint:

```text
POST /tickets/{id}/move
```

Request:

```json
{
  "sourceColumnId": "...",
  "destinationColumnId": "...",
  "newPosition": 2500
}
```

Update:

- column_id
- position

ONLY.

---

# Step 19 — Implement Sparse Positioning

Do NOT use sequential ordering.

Bad:

```text
1
2
3
```

Good:

```text
1000
2000
3000
```

Insertions can occur between values:

```text
1500
```

This minimizes database writes.

---

# Step 20 — Implement Ticket Assignees

Endpoints:

```text
POST   /tickets/{id}/assignees
DELETE /tickets/{id}/assignees/{userId}
```

---

# Step 21 — Implement Labels

Endpoints:

```text
POST   /boards/{id}/labels
PATCH  /labels/{id}
DELETE /labels/{id}
```

---

# Step 22 — Implement Ticket Labels

Endpoints:

```text
POST   /tickets/{id}/labels
DELETE /tickets/{id}/labels/{labelId}
```

---

# Step 23 — Implement Comments

Endpoints:

```text
GET    /tickets/{id}/comments
POST   /tickets/{id}/comments
PATCH  /comments/{id}
DELETE /comments/{id}
```

---

# Step 24 — Implement Activity Logs

Track:

- ticket created
- ticket moved
- ticket assigned
- ticket updated
- comment added

Store metadata as JSONB.

---

# Step 25 — Implement Global Error Handling

Create:

```text
GlobalExceptionHandler.java
```

Use:

```java
@RestControllerAdvice
```

Return consistent responses.

Example:

```json
{
  "timestamp": "...",
  "status": 404,
  "message": "Ticket not found"
}
```

---

# Step 26 — Add Pagination

Never return massive lists.

Use:

```java
Pageable pageable
```

Paginate:

- comments
- activity logs
- tickets

---

# Step 27 — Add Database Indexes

Important indexes:

```sql
CREATE INDEX idx_ticket_board
ON tickets(board_id);

CREATE INDEX idx_ticket_column
ON tickets(column_id);

CREATE INDEX idx_ticket_position
ON tickets(position);
```

---

# Step 28 — Add Integration Tests

Use:

- Testcontainers
- PostgreSQL container
- Real Flyway migrations

Avoid H2 for integration tests. PostgreSQL behavior differs significantly. :contentReference[oaicite:5]{index=5}

---

# Step 29 — Add Unit Tests

Test:

- services
- validators
- permission logic
- move ticket logic

---

# Step 30 — Add OpenAPI / Swagger

Add:

```xml
springdoc-openapi-starter-webmvc-ui
```

Expose:

```text
/swagger-ui.html
```

---

# Step 31 — Add Logging

Use structured logging.

Include:

- request id
- user id
- trace id

Avoid logging JWT tokens or passwords.

---

# Step 32 — Add Permissions

Minimum roles:

```text
OWNER
ADMIN
MEMBER
```

Permissions:

| Action | Required |
|---|---|
| Delete board | OWNER |
| Edit board | ADMIN |
| Create ticket | MEMBER |

---

# Step 33 — Optimize Board Fetch

Create:

```text
GET /boards/{id}/full
```

Return:

```json
{
  "board": {},
  "columns": [],
  "tickets": [],
  "members": [],
  "labels": []
}
```

Avoid frontend waterfall requests.

---

# Step 34 — Add WebSocket Realtime (Optional)

Later enhancement.

Events:

```text
ticket.created
ticket.updated
ticket.moved
comment.created
```

Use:

- Spring WebSocket
- STOMP
- SockJS

Optional for MVP.

---

# Step 35 — Add CI/CD

Setup:

- GitHub Actions
- Build
- Test
- Docker image
- Deploy

---

# Step 36 — Production Hardening

## Required

- HTTPS only
- Rate limiting
- Secure JWT secrets
- Environment variables
- Database backups
- Health checks

---

# Step 37 — Suggested Future Enhancements

Possible future modules:

- Attachments
- Checklists
- Subtasks
- Notifications
- Mentions
- AI summaries
- Time tracking
- Sprint support
- Audit trails
- Search

---

# Recommended Implementation Order

## Phase 1 — Foundation

- Project setup
- Docker
- PostgreSQL
- Flyway
- Security
- Authentication

## Phase 2 — Core Kanban

- Boards
- Columns
- Tickets
- Drag-and-drop

## Phase 3 — Collaboration

- Assignees
- Labels
- Comments

## Phase 4 — Production Readiness

- Activity logs
- Pagination
- Testing
- Swagger
- Logging

## Phase 5 — Advanced Features

- Realtime
- Notifications
- Attachments
- AI

---

# Recommended Coding Standards

## General

- Constructor injection only
- No field injection
- DTOs everywhere
- Thin controllers
- Business logic in services
- Transactions in service layer

## Naming

```text
TicketController
TicketService
TicketRepository
TicketMapper
```

## API

Use REST conventions consistently.

---

# Definition of Done

The backend is complete when:

- Authentication works
- CRUD APIs work
- Drag-and-drop works
- Permissions work
- Flyway migrations work
- Tests pass
- Swagger docs exist
- Docker compose runs successfully
- PostgreSQL persists data
- Full board fetch endpoint works

---

# Recommended Next Steps After Backend

1. Build React frontend
2. Add drag-and-drop UI
3. Add realtime collaboration
4. Add notifications
5. Add deployment pipeline
6. Add monitoring
7. Add analytics
