import { useState } from 'react'
import type { FormEvent } from 'react'
import { starterTickets } from './mock/starterTickets'
import { mockUsers } from './mock/users'
import type { Ticket, TicketPriority, TicketStatus } from './types'
import './App.css'

type Column = {
  id: TicketStatus
  title: string
  accent: string
}

const columns: Column[] = [
  { id: 'backlog', title: 'Backlog', accent: 'is-info' },
  { id: 'ready', title: 'In Progress', accent: 'is-link' },
  { id: 'progress', title: 'In Review', accent: 'is-warning' },
  { id: 'done', title: 'Done', accent: 'is-success' },
]

const emptyForm = {
  title: '',
  description: '',
  assignee: mockUsers[0]?.name ?? '',
  priority: 'Medium' as TicketPriority,
  status: 'backlog' as TicketStatus,
}

const priorityOptions: TicketPriority[] = ['Low', 'Medium', 'High']

function getInitials(name: string): string {
  return name
    .split(' ')
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
    .slice(0, 2)
}

function App() {
  const [tickets, setTickets] = useState<Ticket[]>(starterTickets)
  const [nextTicketNumber, setNextTicketNumber] = useState(106)
  const [draggedTicketId, setDraggedTicketId] = useState<string | null>(null)
  const [activeDropTarget, setActiveDropTarget] = useState<string | null>(null)
  const [splitByUser, setSplitByUser] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [selectedTicket, setSelectedTicket] = useState<Ticket | null>(null)
  const [ticketDraft, setTicketDraft] = useState<Ticket | null>(null)
  const [isEditingDescription, setIsEditingDescription] = useState(false)
  const [descriptionDraft, setDescriptionDraft] = useState('')
  const [form, setForm] = useState(emptyForm)

  const ticketLabel = (count: number) => `${count} ${count === 1 ? 'ticket' : 'tickets'}`

  function getTicketsByColumn(boardTickets: Ticket[]) {
    return columns.reduce<Record<TicketStatus, Ticket[]>>(
      (groupedTickets, column) => ({
        ...groupedTickets,
        [column.id]: boardTickets.filter(
          (ticket) => ticket.status === column.id,
        ),
      }),
      {
        backlog: [],
        ready: [],
        progress: [],
        done: [],
      },
    )
  }

  function moveTicket(
    ticketId: string,
    status: TicketStatus,
    assignee?: string,
  ) {
    setTickets((currentTickets) =>
      currentTickets.map((ticket) =>
        ticket.id === ticketId
          ? { ...ticket, status, assignee: assignee ?? ticket.assignee }
          : ticket,
      ),
    )
  }

  function closeModal() {
    setIsModalOpen(false)
    setForm(emptyForm)
  }

  function closeTicketDetails() {
    setSelectedTicket(null)
    setTicketDraft(null)
    setIsEditingDescription(false)
    setDescriptionDraft('')
  }

  function openTicketDetails(ticket: Ticket) {
    setSelectedTicket(ticket)
    setTicketDraft(ticket)
    setIsEditingDescription(false)
    setDescriptionDraft(ticket.description)
  }

  function startEditingDescription() {
    if (!ticketDraft) {
      return
    }

    setDescriptionDraft(ticketDraft.description)
    setIsEditingDescription(true)
  }

  function saveDescription() {
    if (!ticketDraft) {
      return
    }

    const description = descriptionDraft.trim()

    if (!description) {
      return
    }

    setTicketDraft({ ...ticketDraft, description })
    setIsEditingDescription(false)
  }

  function updateTicketDraft(updates: Partial<Ticket>) {
    if (!ticketDraft) {
      return
    }

    setTicketDraft({ ...ticketDraft, ...updates })
  }

  function saveTicketDetails() {
    if (!selectedTicket || !ticketDraft) {
      return
    }

    setTickets((currentTickets) =>
      currentTickets.map((ticket) =>
        ticket.id === selectedTicket.id ? ticketDraft : ticket,
      ),
    )
    closeTicketDetails()
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

  function renderBoard(
    boardTickets: Ticket[],
    boardId: string,
    assignee?: string,
  ) {
    const ticketsByColumn = getTicketsByColumn(boardTickets)

    return (
      <div className="kanban-board">
        {columns.map((column) => {
          const columnTickets = ticketsByColumn[column.id]
          const dropTargetId = `${boardId}-${column.id}`
          const isDropTarget = activeDropTarget === dropTargetId

          return (
            <article
              className={`kanban-column ${isDropTarget ? 'is-drop-target' : ''}`}
              key={column.id}
              onDragOver={(event) => {
                event.preventDefault()
                setActiveDropTarget(dropTargetId)
              }}
              onDragLeave={() => setActiveDropTarget(null)}
              onDrop={(event) => {
                event.preventDefault()
                if (draggedTicketId) {
                  moveTicket(draggedTicketId, column.id, assignee)
                }
                setDraggedTicketId(null)
                setActiveDropTarget(null)
              }}
            >
              <header className="column-header">
                <div>
                  <span className={`tag ${column.accent} is-light`}>
                    {column.title}
                  </span>
                  <p>{ticketLabel(columnTickets.length)}</p>
                </div>
              </header>

              <div className="ticket-list">
                {columnTickets.map((ticket) => (
                  <article
                    className={`ticket-card ${draggedTicketId === ticket.id ? 'is-dragging' : ''}`}
                    draggable
                    key={ticket.id}
                    onClick={() => openTicketDetails(ticket)}
                    onDragStart={(event) => {
                      event.dataTransfer.effectAllowed = 'move'
                      event.dataTransfer.setData('text/plain', ticket.id)
                      setDraggedTicketId(ticket.id)
                    }}
                    onDragEnd={() => {
                      setDraggedTicketId(null)
                      setActiveDropTarget(null)
                    }}
                    onKeyDown={(event) => {
                      if (event.key === 'Enter' || event.key === ' ') {
                        event.preventDefault()
                        openTicketDetails(ticket)
                      }
                    }}
                    role="button"
                    tabIndex={0}
                  >
                    <div className="ticket-card-header">
                      <h2 className="title is-6">{ticket.title}</h2>
                      <span
                        className={`tag priority-${ticket.priority.toLowerCase()}`}
                      >
                        {ticket.priority}
                      </span>
                    </div>
                    <footer>
                      <div className="assignee-info">
                        <div className="profile-pic">{getInitials(ticket.assignee)}</div>
                        <span>{ticket.assignee}</span>
                      </div>
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
    )
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

        <div className="board-actions">
          <label className="split-toggle">
            <span>Split by user</span>
            <input
              checked={splitByUser}
              onChange={(event) => setSplitByUser(event.target.checked)}
              type="checkbox"
            />
            <span className="switch-track" aria-hidden="true">
              <span className="switch-thumb" />
            </span>
          </label>

          <button
            className="button is-primary"
            type="button"
            onClick={() => setIsModalOpen(true)}
          >
            <span aria-hidden="true">+</span>
            <span>New ticket</span>
          </button>
        </div>
      </section>

      <section className="section board-section" aria-label="Kanban board">
        {splitByUser ? (
          <div className="user-board-stack">
            {mockUsers.map((user) => {
              const userTickets = tickets.filter(
                (ticket) => ticket.assignee === user.name,
              )

              return (
                <section className="user-board-section" key={user.id}>
                  <div className="user-divider">
                    <div>
                      <h2>{user.name}</h2>
                      <p>{user.role}</p>
                    </div>
                    <span>{ticketLabel(userTickets.length)}</span>
                  </div>
                  {renderBoard(userTickets, user.id, user.name)}
                </section>
              )
            })}
          </div>
        ) : (
          renderBoard(tickets, 'all')
        )}
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
                    <div className="select is-fullwidth">
                      <select
                        id="ticket-assignee"
                        onChange={(event) =>
                          setForm((currentForm) => ({
                            ...currentForm,
                            assignee: event.target.value,
                          }))
                        }
                        required
                        value={form.assignee}
                      >
                        {mockUsers.map((user) => (
                          <option key={user.id} value={user.name}>
                            {user.name}
                          </option>
                        ))}
                      </select>
                    </div>
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

      <div className={`modal ${selectedTicket ? 'is-active' : ''}`}>
        <div
          className="modal-background"
          onClick={closeTicketDetails}
          role="presentation"
        />
        <div className="modal-card">
          <header className="modal-card-head">
            <div>
              <p className="modal-card-title">Ticket details</p>
              {selectedTicket && (
                <p className="modal-card-subtitle">
                  {selectedTicket.id.replace('ticket-', '#')}
                </p>
              )}
            </div>
            <button
              aria-label="close"
              className="delete"
              onClick={closeTicketDetails}
              type="button"
            />
          </header>

          {selectedTicket && ticketDraft && (
            <>
              <section className="modal-card-body">
                <div className="ticket-detail-header">
                  <h2 className="title is-4">{ticketDraft.title}</h2>
                  <span
                    className={`tag priority-${ticketDraft.priority.toLowerCase()}`}
                  >
                    {ticketDraft.priority}
                  </span>
                </div>

                <div className="ticket-detail-section">
                  <p className="detail-label">Description</p>
                  {isEditingDescription ? (
                    <div className="description-editor">
                      <textarea
                        aria-label="Ticket description"
                        className="textarea"
                        onChange={(event) =>
                          setDescriptionDraft(event.target.value)
                        }
                        rows={4}
                        value={descriptionDraft}
                      />
                      <div className="description-actions">
                        <button
                          className="button is-primary"
                          onClick={saveDescription}
                          type="button"
                        >
                          Save
                        </button>
	                        <button
	                          className="button"
	                          onClick={() => {
	                            setDescriptionDraft(ticketDraft.description)
	                            setIsEditingDescription(false)
	                          }}
                          type="button"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <button
                      className="ticket-detail-description"
                      onClick={startEditingDescription}
                      type="button"
	                    >
	                      {ticketDraft.description}
	                    </button>
                  )}
                </div>

                <div className="detail-grid">
                  <div>
                    <p className="detail-label">Assignee</p>
                    <div className="select is-fullwidth detail-select">
                      <select
	                        aria-label="Ticket assignee"
	                        onChange={(event) =>
	                          updateTicketDraft({ assignee: event.target.value })
	                        }
	                        value={ticketDraft.assignee}
                      >
                        {mockUsers.map((user) => (
                          <option key={user.id} value={user.name}>
                            {user.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div>
                    <p className="detail-label">Priority</p>
                    <div className="select is-fullwidth detail-select">
                      <select
	                        aria-label="Ticket priority"
	                        onChange={(event) =>
	                          updateTicketDraft({
	                            priority: event.target.value as TicketPriority,
	                          })
	                        }
	                        value={ticketDraft.priority}
                      >
                        {priorityOptions.map((priority) => (
                          <option key={priority}>{priority}</option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div>
                    <p className="detail-label">Column</p>
                    <div className="select is-fullwidth detail-select">
                      <select
	                        aria-label="Ticket column"
	                        onChange={(event) =>
	                          updateTicketDraft({
	                            status: event.target.value as TicketStatus,
	                          })
	                        }
	                        value={ticketDraft.status}
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
              </section>

              <footer className="modal-card-foot">
                <button
	                  className="button is-primary"
	                  onClick={saveTicketDetails}
	                  type="button"
                >
                  Done
                </button>
              </footer>
            </>
          )}
        </div>
      </div>
    </main>
  )
}

export default App
