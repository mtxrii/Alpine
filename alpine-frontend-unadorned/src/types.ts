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
