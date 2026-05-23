import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import './App.css'

type TicketStatus = 'backlog' | 'ready' | 'progress' | 'done'
type TicketPriority = 'Low' | 'Medium' | 'High'

type Ticket = {
  id: string
  title: string
  description: string
  assignee: string
  priority: TicketPriority
  status: TicketStatus
}

type Column = {
  id: TicketStatus
  title: string
  accent: string
}

const columns: Column[] = [
  { id: 'backlog', title: 'Backlog', accent: 'is-info' },
  { id: 'ready', title: 'Ready', accent: 'is-link' },
  { id: 'progress', title: 'In Progress', accent: 'is-warning' },
  { id: 'done', title: 'Done', accent: 'is-success' },
]

const starterTickets: Ticket[] = [
  {
    id: 'ticket-101',
    title: 'Map ticket model',
    description: 'Define fields the backend will eventually persist.',
    assignee: 'Maya',
    priority: 'High',
    status: 'backlog',
  },
  {
    id: 'ticket-102',
    title: 'Draft empty states',
    description: 'Show useful guidance when a column has no tickets.',
    assignee: 'Leo',
    priority: 'Medium',
    status: 'backlog',
  },
  {
    id: 'ticket-103',
    title: 'Review swimlane labels',
    description: 'Make sure the team language matches the workflow.',
    assignee: 'Priya',
    priority: 'Low',
    status: 'ready',
  },
  {
    id: 'ticket-104',
    title: 'Build drag interactions',
    description: 'Move cards across lanes and update local state.',
    assignee: 'Ethan',
    priority: 'High',
    status: 'progress',
  },
  {
    id: 'ticket-105',
    title: 'Create seed tickets',
    description: 'Add realistic sample work for first-run demos.',
    assignee: 'Ari',
    priority: 'Medium',
    status: 'done',
  },
]

const emptyForm = {
  title: '',
  description: '',
  assignee: '',
  priority: 'Medium' as TicketPriority,
  status: 'backlog' as TicketStatus,
}

function App() {
  const [tickets, setTickets] = useState<Ticket[]>(starterTickets)
  const [nextTicketNumber, setNextTicketNumber] = useState(106)
  const [draggedTicketId, setDraggedTicketId] = useState<string | null>(null)
  const [activeDropColumn, setActiveDropColumn] = useState<TicketStatus | null>(
    null,
  )
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)

  const ticketsByColumn = useMemo(
    () =>
      columns.reduce<Record<TicketStatus, Ticket[]>>(
        (groupedTickets, column) => ({
          ...groupedTickets,
          [column.id]: tickets.filter((ticket) => ticket.status === column.id),
        }),
        {
          backlog: [],
          ready: [],
          progress: [],
          done: [],
        },
      ),
    [tickets],
  )

  function moveTicket(ticketId: string, status: TicketStatus) {
    setTickets((currentTickets) =>
      currentTickets.map((ticket) =>
        ticket.id === ticketId ? { ...ticket, status } : ticket,
      ),
    )
  }

  function closeModal() {
    setIsModalOpen(false)
    setForm(emptyForm)
  }

  function createTicket(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const title = form.title.trim()
    const description = form.description.trim()
    const assignee = form.assignee.trim()

    if (!title || !description || !assignee) {
      return
    }

    const ticketNumber = nextTicketNumber

    setTickets((currentTickets) => [
      {
        id: `ticket-${ticketNumber}`,
        title,
        description,
        assignee,
        priority: form.priority,
        status: form.status,
      },
      ...currentTickets,
    ])
    setNextTicketNumber((currentNumber) => currentNumber + 1)
    closeModal()
  }

  return (
    <main className="app-shell">
      <section className="section board-header">
        <div>
          <p className="eyebrow">Product operations</p>
          <h1 className="title is-2">Ticket tracker</h1>
          <p className="subtitle is-6">
            Plan, hand off, and complete work across a simple four-step board.
          </p>
        </div>

        <button
          className="button is-primary"
          type="button"
          onClick={() => setIsModalOpen(true)}
        >
          <span aria-hidden="true">+</span>
          <span>New ticket</span>
        </button>
      </section>

      <section className="section board-section" aria-label="Kanban board">
        <div className="kanban-board">
          {columns.map((column) => {
            const columnTickets = ticketsByColumn[column.id]
            const isDropTarget = activeDropColumn === column.id

            return (
              <article
                className={`kanban-column ${isDropTarget ? 'is-drop-target' : ''}`}
                key={column.id}
                onDragOver={(event) => {
                  event.preventDefault()
                  setActiveDropColumn(column.id)
                }}
                onDragLeave={() => setActiveDropColumn(null)}
                onDrop={(event) => {
                  event.preventDefault()
                  if (draggedTicketId) {
                    moveTicket(draggedTicketId, column.id)
                  }
                  setDraggedTicketId(null)
                  setActiveDropColumn(null)
                }}
              >
                <header className="column-header">
                  <div>
                    <span className={`tag ${column.accent} is-light`}>
                      {column.title}
                    </span>
                    <p>{columnTickets.length} tickets</p>
                  </div>
                </header>

                <div className="ticket-list">
                  {columnTickets.map((ticket) => (
                    <article
                      className={`ticket-card ${draggedTicketId === ticket.id ? 'is-dragging' : ''}`}
                      draggable
                      key={ticket.id}
                      onDragStart={(event) => {
                        event.dataTransfer.effectAllowed = 'move'
                        event.dataTransfer.setData('text/plain', ticket.id)
                        setDraggedTicketId(ticket.id)
                      }}
                      onDragEnd={() => {
                        setDraggedTicketId(null)
                        setActiveDropColumn(null)
                      }}
                    >
                      <div className="ticket-card-header">
                        <h2 className="title is-6">{ticket.title}</h2>
                        <span
                          className={`tag priority-${ticket.priority.toLowerCase()}`}
                        >
                          {ticket.priority}
                        </span>
                      </div>
                      <p>{ticket.description}</p>
                      <footer>
                        <span>{ticket.assignee}</span>
                        <span>{ticket.id.replace('ticket-', '#')}</span>
                      </footer>
                    </article>
                  ))}

                  {columnTickets.length === 0 && (
                    <div className="empty-column">Drop a ticket here</div>
                  )}
                </div>
              </article>
            )
          })}
        </div>
      </section>

      <div className={`modal ${isModalOpen ? 'is-active' : ''}`}>
        <div
          className="modal-background"
          onClick={closeModal}
          role="presentation"
        />
        <div className="modal-card">
          <header className="modal-card-head">
            <p className="modal-card-title">Create ticket</p>
            <button
              aria-label="close"
              className="delete"
              onClick={closeModal}
              type="button"
            />
          </header>

          <form onSubmit={createTicket}>
            <section className="modal-card-body">
              <div className="field">
                <label className="label" htmlFor="ticket-title">
                  Title
                </label>
                <div className="control">
                  <input
                    className="input"
                    id="ticket-title"
                    onChange={(event) =>
                      setForm((currentForm) => ({
                        ...currentForm,
                        title: event.target.value,
                      }))
                    }
                    required
                    type="text"
                    value={form.title}
                  />
                </div>
              </div>

              <div className="field">
                <label className="label" htmlFor="ticket-description">
                  Description
                </label>
                <div className="control">
                  <textarea
                    className="textarea"
                    id="ticket-description"
                    onChange={(event) =>
                      setForm((currentForm) => ({
                        ...currentForm,
                        description: event.target.value,
                      }))
                    }
                    required
                    rows={3}
                    value={form.description}
                  />
                </div>
              </div>

              <div className="form-grid">
                <div className="field">
                  <label className="label" htmlFor="ticket-assignee">
                    Assignee
                  </label>
                  <div className="control">
                    <input
                      className="input"
                      id="ticket-assignee"
                      onChange={(event) =>
                        setForm((currentForm) => ({
                          ...currentForm,
                          assignee: event.target.value,
                        }))
                      }
                      required
                      type="text"
                      value={form.assignee}
                    />
                  </div>
                </div>

                <div className="field">
                  <label className="label" htmlFor="ticket-priority">
                    Priority
                  </label>
                  <div className="control">
                    <div className="select is-fullwidth">
                      <select
                        id="ticket-priority"
                        onChange={(event) =>
                          setForm((currentForm) => ({
                            ...currentForm,
                            priority: event.target.value as TicketPriority,
                          }))
                        }
                        value={form.priority}
                      >
                        <option>Low</option>
                        <option>Medium</option>
                        <option>High</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div className="field">
                  <label className="label" htmlFor="ticket-status">
                    Column
                  </label>
                  <div className="control">
                    <div className="select is-fullwidth">
                      <select
                        id="ticket-status"
                        onChange={(event) =>
                          setForm((currentForm) => ({
                            ...currentForm,
                            status: event.target.value as TicketStatus,
                          }))
                        }
                        value={form.status}
                      >
                        {columns.map((column) => (
                          <option key={column.id} value={column.id}>
                            {column.title}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>
              </div>
            </section>

            <footer className="modal-card-foot">
              <button className="button is-primary" type="submit">
                Create ticket
              </button>
              <button className="button" onClick={closeModal} type="button">
                Cancel
              </button>
            </footer>
          </form>
        </div>
      </div>
    </main>
  )
}

export default App
