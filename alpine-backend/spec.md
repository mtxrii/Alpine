# Kanban Board Backend API Specification

## Overview

This document defines the backend architecture, database schema, and REST API design for a Kanban-style project management application.

The system supports:

- Users
- Workspaces
- Boards
- Columns
- Tickets
- Ticket assignments
- Drag-and-drop ticket movement
- Labels
- Comments
- Activity tracking

Recommended stack:

- PostgreSQL
- Node.js
- Prisma or Drizzle ORM
- REST API
- Optional WebSocket realtime updates

---

# Architecture Overview

## Entity Relationships

```text
Workspace
 └── Boards
      ├── Columns
      │     └── Tickets
      │           ├── Assignees
      │           ├── Labels
      │           ├── Comments
      │           └── Activity Logs
      └── Labels
```

---

# Database Schema

---

# Users

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email TEXT UNIQUE NOT NULL,
    name TEXT NOT NULL,
    avatar_url TEXT,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

# Workspaces

```sql
CREATE TABLE workspaces (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name TEXT NOT NULL,

    owner_id UUID NOT NULL REFERENCES users(id),

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

# Workspace Members

```sql
CREATE TABLE workspace_members (
    workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    role TEXT NOT NULL CHECK (
        role IN ('owner', 'admin', 'member')
    ),

    PRIMARY KEY (workspace_id, user_id)
);
```

---

# Boards

```sql
CREATE TABLE boards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,

    name TEXT NOT NULL,
    description TEXT,

    created_by UUID NOT NULL REFERENCES users(id),

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

# Columns

```sql
CREATE TABLE columns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    board_id UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,

    name TEXT NOT NULL,

    position INTEGER NOT NULL,

    color TEXT,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

## Notes

- `position` controls left-to-right ordering
- Use sparse ordering values:
    - 1000
    - 2000
    - 3000

This enables efficient drag-and-drop reordering.

---

# Tickets

```sql
CREATE TABLE tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    board_id UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,
    column_id UUID NOT NULL REFERENCES columns(id) ON DELETE CASCADE,

    title TEXT NOT NULL,
    description TEXT,

    priority TEXT CHECK (
        priority IN ('low', 'medium', 'high', 'urgent')
    ),

    status TEXT,

    position INTEGER NOT NULL,

    created_by UUID NOT NULL REFERENCES users(id),

    due_date TIMESTAMPTZ,

    archived BOOLEAN DEFAULT FALSE,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

# Ticket Assignees

```sql
CREATE TABLE ticket_assignees (
    ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,

    PRIMARY KEY(ticket_id, user_id)
);
```

---

# Labels

```sql
CREATE TABLE labels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    board_id UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,

    name TEXT NOT NULL,
    color TEXT NOT NULL
);
```

---

# Ticket Labels

```sql
CREATE TABLE ticket_labels (
    ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,
    label_id UUID REFERENCES labels(id) ON DELETE CASCADE,

    PRIMARY KEY(ticket_id, label_id)
);
```

---

# Comments

```sql
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    ticket_id UUID NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,

    author_id UUID NOT NULL REFERENCES users(id),

    body TEXT NOT NULL,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
```

---

# Activity Logs

```sql
CREATE TABLE activity_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    board_id UUID NOT NULL REFERENCES boards(id) ON DELETE CASCADE,

    ticket_id UUID REFERENCES tickets(id) ON DELETE CASCADE,

    user_id UUID NOT NULL REFERENCES users(id),

    action TEXT NOT NULL,

    metadata JSONB,

    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

## Example Activity Metadata

```json
{
  "action": "ticket.moved",
  "metadata": {
    "fromColumnId": "todo",
    "toColumnId": "inprogress"
  }
}
```

---

# REST API Specification

---

# Authentication

## Register

```http
POST /auth/register
```

## Login

```http
POST /auth/login
```

## Logout

```http
POST /auth/logout
```

## Current User

```http
GET /auth/me
```

---

# Workspaces

## Get Workspaces

```http
GET /workspaces
```

## Create Workspace

```http
POST /workspaces
```

## Get Workspace

```http
GET /workspaces/:workspaceId
```

## Update Workspace

```http
PATCH /workspaces/:workspaceId
```

## Delete Workspace

```http
DELETE /workspaces/:workspaceId
```

---

# Boards

## Get Boards

```http
GET /boards
```

## Create Board

```http
POST /boards
```

## Get Board

```http
GET /boards/:boardId
```

## Update Board

```http
PATCH /boards/:boardId
```

## Delete Board

```http
DELETE /boards/:boardId
```

---

# Columns

## Get Columns

```http
GET /boards/:boardId/columns
```

## Create Column

```http
POST /boards/:boardId/columns
```

## Update Column

```http
PATCH /columns/:columnId
```

## Delete Column

```http
DELETE /columns/:columnId
```

---

# Reorder Columns

```http
POST /boards/:boardId/columns/reorder
```

## Request Body

```json
{
  "orderedColumnIds": [
    "col_1",
    "col_2",
    "col_3"
  ]
}
```

---

# Tickets

## Get Tickets

```http
GET /boards/:boardId/tickets
```

## Create Ticket

```http
POST /boards/:boardId/tickets
```

## Get Ticket

```http
GET /tickets/:ticketId
```

## Update Ticket

```http
PATCH /tickets/:ticketId
```

## Delete Ticket

```http
DELETE /tickets/:ticketId
```

---

# Move Ticket (Drag and Drop)

```http
POST /tickets/:ticketId/move
```

## Request Body

```json
{
  "sourceColumnId": "todo",
  "destinationColumnId": "inprogress",
  "newPosition": 2500
}
```

## Backend Logic

Update:

- `column_id`
- `position`

Sparse positioning avoids rewriting many rows.

---

# Ticket Assignees

## Assign User

```http
POST /tickets/:ticketId/assignees
```

## Remove Assignee

```http
DELETE /tickets/:ticketId/assignees/:userId
```

## Request Body

```json
{
  "userId": "user_123"
}
```

---

# Labels

## Create Label

```http
POST /boards/:boardId/labels
```

## Update Label

```http
PATCH /labels/:labelId
```

## Delete Label

```http
DELETE /labels/:labelId
```

---

# Ticket Labels

## Add Label

```http
POST /tickets/:ticketId/labels
```

## Remove Label

```http
DELETE /tickets/:ticketId/labels/:labelId
```

---

# Comments

## Get Comments

```http
GET /tickets/:ticketId/comments
```

## Create Comment

```http
POST /tickets/:ticketId/comments
```

## Update Comment

```http
PATCH /comments/:commentId
```

## Delete Comment

```http
DELETE /comments/:commentId
```

---

# Activity Feed

## Board Activity

```http
GET /boards/:boardId/activity
```

## Ticket Activity

```http
GET /tickets/:ticketId/activity
```

---

# Ticket Response Shape

```json
{
  "id": "ticket_123",
  "title": "Fix login bug",
  "description": "OAuth callback failing",

  "priority": "high",

  "column": {
    "id": "col_inprogress",
    "name": "In Progress"
  },

  "position": 2400,

  "assignees": [
    {
      "id": "user_1",
      "name": "Alice"
    }
  ],

  "labels": [
    {
      "id": "label_bug",
      "name": "Bug",
      "color": "#ff0000"
    }
  ],

  "createdBy": {
    "id": "user_2",
    "name": "Bob"
  },

  "dueDate": "2026-05-30T00:00:00Z",

  "createdAt": "2026-05-20T00:00:00Z",
  "updatedAt": "2026-05-22T00:00:00Z"
}
```

---

# Recommended Board Fetch Endpoint

## Full Board Payload

```http
GET /boards/:boardId/full
```

## Example Response

```json
{
  "board": {},
  "columns": [],
  "tickets": [],
  "members": [],
  "labels": []
}
```

This minimizes frontend waterfall requests.

---

# Realtime Events

Optional but recommended.

## Example Events

```text
ticket.created
ticket.updated
ticket.deleted
ticket.moved

column.created
column.updated

comment.created
```

## Example Payload

```json
{
  "event": "ticket.moved",
  "payload": {
    "ticketId": "ticket_123",
    "columnId": "done",
    "position": 5000
  }
}
```

---

# Recommended Tech Stack

## Backend Frameworks

- Fastify
- NestJS
- Express

## ORM

- Prisma
- Drizzle

## Database

- PostgreSQL

## Realtime

- Socket.IO
- Ably
- Pusher
- Supabase Realtime

---

# Scaling Considerations

---

# Sparse Positioning

Do not use sequential indexes.

Avoid:

```text
1
2
3
```

Use:

```text
1000
2000
3000
```

Insertions can then occur between positions.

Example:

```text
1500
```

Rebalance occasionally if needed.

---

# Pagination

Avoid loading:

- all comments
- all activity logs
- all users

in a single request.

Paginate aggressively.

---

# Permissions

Minimum roles:

```text
workspace.owner
workspace.admin
workspace.member
```

---

# Suggested Future Features

- Subtasks
- Attachments
- Checklists
- Ticket relationships
- Time tracking
- Sprint support
- Automations
- Templates
- Custom fields
- Mentions
- Notifications
- AI summaries

---

# Recommended MVP Scope

## Include

- Users
- Boards
- Columns
- Tickets
- Drag-and-drop
- Assignees

## Skip Initially

- Activity logs
- Labels
- Comments
- Notifications
- Attachments

These can be added later without major schema changes.