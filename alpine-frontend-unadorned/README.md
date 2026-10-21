# Alpine frontend

Alpine is a React and TypeScript frontend for managing workspaces, kanban boards, and tickets. It connects to the Alpine backend through a JSON API and provides:

- User registration and login
- Workspace and board selection/creation
- Kanban columns with drag-and-drop ticket movement
- Ticket creation and editing
- Ticket priorities, assignees, labels, and comments
- Workspace member management
- Board activity access
- Light/dark mode and an optional view split by assignee

This repository contains the frontend only. A running Alpine API is required for authentication and application data.

## Requirements

- Node.js 18 or newer
- npm
- The Alpine backend running locally or at a reachable URL

## Getting started

1. Install dependencies:

   ```bash
   npm install
   ```

2. Configure the backend URL if it is not running at the default address:

   ```bash
   printf 'VITE_API_URL=http://localhost:8080\n' > .env.local
   ```

   `VITE_API_URL` is optional. When omitted, the frontend uses `http://localhost:8080`.

3. Start the development server:

   ```bash
   npm run dev
   ```

   Vite will print the local URL, normally `http://localhost:5173`.

4. Open the URL in a browser, then register or sign in with an account provided by the backend.

The backend must allow requests from the frontend origin. If the browser reports CORS errors, update the backend's CORS configuration to allow the Vite development URL.

## Available scripts

| Command | Description |
| --- | --- |
| `npm run dev` | Start Vite with hot module replacement |
| `npm run build` | Type-check the project and create a production build in `dist/` |
| `npm run lint` | Run ESLint |
| `npm run preview` | Serve the production build locally after `npm run build` |

For a production-style local check:

```bash
npm run build
npm run preview
```

## Configuration

The frontend reads Vite environment variables at build time. The supported variable is:

| Variable | Default | Description |
| --- | --- | --- |
| `VITE_API_URL` | `http://localhost:8080` | Base URL for the Alpine API |

Use a `.env.local` file for local values. Do not commit credentials or other secrets; values prefixed with `VITE_` are exposed to browser code.

## Project structure

```text
src/
  App.tsx       Main application UI and client-side state
  api.ts        Typed fetch wrapper and backend endpoint calls
  types.ts      API request/response and domain types
  App.css       Application component styles
  index.css     Global styles and Bulma setup
  main.tsx      React entry point
  mock/         Sample data retained for UI prototyping
public/         Static assets served as-is
vite.config.ts  Vite configuration
```

The API client currently calls authentication, user, workspace, board, column, ticket, assignee, label, comment, activity, and workspace-member endpoints. See [`src/api.ts`](src/api.ts) for the complete list.

## Authentication and browser state

After login or registration, the access token is stored in `localStorage` under `alpine.accessToken`. The selected workspace and board are also remembered under `alpine.workspaceId` and `alpine.boardId`. Sign out clears the stored session so the next visit starts at the authentication screen.

## Troubleshooting

- **The app loads but data requests fail:** confirm the backend is running and `VITE_API_URL` points to the correct base URL.
- **The browser shows a CORS error:** allow the frontend origin in the backend's CORS settings.
- **A stale board or session appears:** sign out, or clear the `alpine.accessToken`, `alpine.workspaceId`, and `alpine.boardId` local-storage entries.
- **Changes to `.env.local` are ignored:** restart the Vite dev server; environment variables are read when Vite starts/builds.
