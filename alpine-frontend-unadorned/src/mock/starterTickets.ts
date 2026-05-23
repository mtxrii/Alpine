import type { Ticket } from '../types'
import { mockUsers } from './users'

export const starterTickets: Ticket[] = [
  {
    id: 'ticket-101',
    title: 'Map ticket model',
    description: 'Define fields the backend will eventually persist.',
    assignee: mockUsers[0].name,
    priority: 'High',
    status: 'backlog',
  },
  {
    id: 'ticket-102',
    title: 'Draft empty states',
    description: 'Show useful guidance when a column has no tickets.',
    assignee: mockUsers[1].name,
    priority: 'Medium',
    status: 'backlog',
  },
  {
    id: 'ticket-103',
    title: 'Review swimlane labels',
    description: 'Make sure the team language matches the workflow.',
    assignee: mockUsers[2].name,
    priority: 'Low',
    status: 'ready',
  },
  {
    id: 'ticket-104',
    title: 'Build drag interactions',
    description: 'Move cards across lanes and update local state.',
    assignee: mockUsers[3].name,
    priority: 'High',
    status: 'progress',
  },
  {
    id: 'ticket-105',
    title: 'Create seed tickets',
    description: 'Add realistic sample work for first-run demos.',
    assignee: mockUsers[4].name,
    priority: 'Medium',
    status: 'done',
  },
]
