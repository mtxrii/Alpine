export type UUID = string

export type ApiPriority = 'low' | 'medium' | 'high' | 'urgent'

export type RegisterRequest = {
  email: string
  name: string
  password: string
  avatarUrl?: string
}

export type LoginRequest = {
  email: string
  password: string
}

export type AuthResponse = {
  user?: UserResponse
  accessToken?: string
  tokenType?: string
  expiresIn?: number
}

export type UserResponse = {
  id: UUID
  email: string
  name: string
  avatarUrl?: string
}

export type WorkspaceResponse = {
  id: UUID
  name: string
  ownerId: UUID
  createdAt?: string
  updatedAt?: string
}

export type BoardResponse = {
  id: UUID
  workspaceId: UUID
  name: string
  description?: string
  createdBy: UUID
  createdAt?: string
  updatedAt?: string
}

export type BoardMemberResponse = {
  id: UUID
  name: string
  email: string
  role: string
}

export type ColumnResponse = {
  id: UUID
  boardId: UUID
  name: string
  position: number
  color?: string
  createdAt?: string
  updatedAt?: string
}

export type LabelResponse = {
  id: UUID
  boardId: UUID
  name: string
  color: string
}

export type UserSummary = {
  id: UUID
  name: string
}

export type ColumnSummary = {
  id: UUID
  name: string
}

export type TicketResponse = {
  id: UUID
  boardId: UUID
  title: string
  description?: string
  priority?: ApiPriority | string
  status?: string
  column?: ColumnSummary
  position?: number
  assignees?: UserSummary[]
  labels?: LabelResponse[]
  createdBy?: UserSummary
  dueDate?: string
  archived?: boolean
  createdAt?: string
  updatedAt?: string
}

export type BoardFullResponse = {
  board?: BoardResponse
  columns?: ColumnResponse[]
  tickets?: TicketResponse[]
  members?: BoardMemberResponse[]
  labels?: LabelResponse[]
}

export type TicketRequest = {
  columnId: UUID
  title: string
  description?: string
  priority?: ApiPriority
  status?: string
  position?: number
  createdBy: UUID
  dueDate?: string
}

export type TicketPatchRequest = Partial<{
  columnId: UUID
  title: string
  description: string
  priority: ApiPriority
  status: string
  position: number
  archived: boolean
  dueDate: string
}>

export type MoveTicketRequest = {
  sourceColumnId: UUID
  destinationColumnId: UUID
  newPosition: number
}

export type CommentResponse = {
  id: UUID
  ticketId: UUID
  authorId: UUID
  authorName: string
  body: string
  createdAt?: string
  updatedAt?: string
}

export type ActivityResponse = {
  id: UUID
  boardId: UUID
  ticketId?: UUID
  userId?: UUID
  userName?: string
  action: string
  metadata?: string
  createdAt?: string
}

export type TicketStatus = 'backlog' | 'ready' | 'progress' | 'done'
export type TicketPriority = 'Low' | 'Medium' | 'High'

export type Ticket = {
  id: string
  title: string
  description: string
  assignee: string
  priority: TicketPriority
  status: TicketStatus
}

export type User = {
  id: string
  name: string
  role: string
}
