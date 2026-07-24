import type {
  ActivityResponse,
  AuthResponse,
  BoardFullResponse,
  BoardMemberResponse,
  BoardResponse,
  ColumnRequest,
  ColumnResponse,
  CommentResponse,
  LoginRequest,
  MoveTicketRequest,
  RegisterRequest,
  TicketPatchRequest,
  TicketRequest,
  TicketResponse,
  UserResponse,
  UUID,
  WorkspaceResponse,
} from './types'

const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

type ApiOptions = {
  body?: unknown
  method?: 'GET' | 'POST' | 'PATCH' | 'DELETE'
  token?: string | null
}

async function request<T>(path: string, options: ApiOptions = {}): Promise<T> {
  const headers = new Headers()

  if (options.body !== undefined) {
    headers.set('Content-Type', 'application/json')
  }

  if (options.token) {
    headers.set('Authorization', `Bearer ${options.token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
    headers,
    method: options.method ?? 'GET',
  })

  if (!response.ok) {
    let message = `${response.status} ${response.statusText}`

    try {
      const errorBody = (await response.json()) as { message?: string }
      message = errorBody.message ?? message
    } catch {
      const text = await response.text()
      message = text || message
    }

    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return (await response.json()) as T
}

export const api = {
  login(body: LoginRequest) {
    return request<AuthResponse>('/auth/login', { body, method: 'POST' })
  },
  register(body: RegisterRequest) {
    return request<AuthResponse>('/auth/register', { body, method: 'POST' })
  },
  me(token: string) {
    return request<UserResponse>('/auth/me', { token })
  },
  listUsers(token: string) {
    return request<UserResponse[]>('/users', { token })
  },
  getUser(token: string, userId: UUID) {
    return request<UserResponse>(`/users/${userId}`, { token })
  },
  listWorkspaces(token: string) {
    return request<WorkspaceResponse[]>('/workspaces', { token })
  },
  createWorkspace(token: string, name: string, ownerId: UUID) {
    return request<WorkspaceResponse>('/workspaces', {
      body: { name, ownerId },
      method: 'POST',
      token,
    })
  },
  listBoards(token: string) {
    return request<BoardResponse[]>('/boards', { token })
  },
  createBoard(
    token: string,
    workspaceId: UUID,
    name: string,
    createdBy: UUID,
    description?: string,
  ) {
    return request<BoardResponse>('/boards', {
      body: { createdBy, description, name, workspaceId },
      method: 'POST',
      token,
    })
  },
  getBoardFull(token: string, boardId: UUID) {
    return request<BoardFullResponse>(`/boards/${boardId}/full`, { token })
  },
  createColumn(token: string, boardId: UUID, body: ColumnRequest) {
    return request<ColumnResponse>(`/boards/${boardId}/columns`, {
      body,
      method: 'POST',
      token,
    })
  },
  createTicket(token: string, boardId: UUID, body: TicketRequest) {
    return request<TicketResponse>(`/boards/${boardId}/tickets`, {
      body,
      method: 'POST',
      token,
    })
  },
  patchTicket(token: string, ticketId: UUID, body: TicketPatchRequest) {
    return request<TicketResponse>(`/tickets/${ticketId}`, {
      body,
      method: 'PATCH',
      token,
    })
  },
  moveTicket(token: string, ticketId: UUID, body: MoveTicketRequest) {
    return request<TicketResponse>(`/tickets/${ticketId}/move`, {
      body,
      method: 'POST',
      token,
    })
  },
  assignUser(token: string, ticketId: UUID, userId: UUID) {
    return request<TicketResponse>(`/tickets/${ticketId}/assignees`, {
      body: { userId },
      method: 'POST',
      token,
    })
  },
  unassignUser(token: string, ticketId: UUID, userId: UUID) {
    return request<TicketResponse>(`/tickets/${ticketId}/assignees/${userId}`, {
      method: 'DELETE',
      token,
    })
  },
  addLabel(token: string, ticketId: UUID, labelId: UUID) {
    return request<TicketResponse>(`/tickets/${ticketId}/labels`, {
      body: { labelId },
      method: 'POST',
      token,
    })
  },
  removeLabel(token: string, ticketId: UUID, labelId: UUID) {
    return request<TicketResponse>(`/tickets/${ticketId}/labels/${labelId}`, {
      method: 'DELETE',
      token,
    })
  },
  listComments(token: string, ticketId: UUID) {
    return request<CommentResponse[]>(`/tickets/${ticketId}/comments`, { token })
  },
  createComment(token: string, ticketId: UUID, authorId: UUID, body: string) {
    return request<CommentResponse>(`/tickets/${ticketId}/comments`, {
      body: { authorId, body },
      method: 'POST',
      token,
    })
  },
  listBoardActivity(token: string, boardId: UUID) {
    return request<ActivityResponse[]>(`/boards/${boardId}/activity`, { token })
  },
  listWorkspaceMembers(token: string, workspaceId: UUID) {
    return request<BoardMemberResponse[]>(`/workspaces/${workspaceId}/members`, { token })
  },
  addWorkspaceMember(token: string, workspaceId: UUID, userId: UUID, role?: string) {
    return request<BoardMemberResponse[]>(`/workspaces/${workspaceId}/members`, {
      body: { userId, role },
      method: 'POST',
      token,
    })
  },
  removeWorkspaceMember(token: string, workspaceId: UUID, userId: UUID) {
    return request<void>(`/workspaces/${workspaceId}/members/${userId}`, {
      method: 'DELETE',
      token,
    })
  },
}
