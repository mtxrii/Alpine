import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { api } from './api'
import type {
  ApiPriority,
  BoardFullResponse,
  BoardMemberResponse,
  BoardResponse,
  ColumnResponse,
  CommentResponse,
  LabelResponse,
  TicketResponse,
  UserResponse,
  UUID,
  WorkspaceResponse,
} from './types'
import './App.css'

type AuthMode = 'login' | 'register'

type TicketForm = {
  title: string
  description: string
  assigneeId: string
  priority: ApiPriority
  columnId: string
}

type TicketDraft = {
  title: string
  description: string
  priority: ApiPriority
  columnId: string
  assigneeIds: string[]
  labelIds: string[]
}

const tokenStorageKey = 'alpine.accessToken'
const boardStorageKey = 'alpine.boardId'
const workspaceStorageKey = 'alpine.workspaceId'

const priorityOptions: ApiPriority[] = ['low', 'medium', 'high', 'urgent']
const roleOptions = ['Member', 'Admin', 'Contributor', 'Viewer']
const starterColumns = [
  { color: '#3273dc', name: 'Backlog' },
  { color: '#3b82f6', name: 'In Progress' },
  { color: '#f59e0b', name: 'In Review' },
  { color: '#23d160', name: 'Done' },
]
const profileColors = [
  '#26c6ae',
  '#3b82f6',
  '#8b5cf6',
  '#ec4899',
  '#f59e0b',
  '#10b981',
  '#06b6d4',
  '#6366f1',
  '#f43f5e',
  '#14b8a6',
  '#7c3aed',
  '#d946ef',
  '#059669',
  '#0891b2',
  '#e11d48',
  '#7c2d12',
  '#4f46e5',
  '#64748b',
  '#6d28d9',
  '#be123c',
]

const emptyTicketForm: TicketForm = {
  assigneeId: '',
  columnId: '',
  description: '',
  priority: 'medium',
  title: '',
}

function getInitials(name: string): string {
  return name
    .split(' ')
    .map((part) => part.charAt(0).toUpperCase())
    .join('')
    .slice(0, 2)
}

function getProfileColor(name: string): string {
  const firstLetter = name.charAt(0).toUpperCase()
  const letterIndex = firstLetter.charCodeAt(0) - 'A'.charCodeAt(0)
  const colorIndex = Math.max(0, letterIndex) % profileColors.length
  return profileColors[colorIndex]
}

function formatPriority(priority?: string): string {
  if (!priority) {
    return 'Medium'
  }

  return `${priority.charAt(0).toUpperCase()}${priority.slice(1)}`
}

function sortColumns(columns: ColumnResponse[]): ColumnResponse[] {
  return [...columns].sort((left, right) => left.position - right.position)
}

function sortTickets(tickets: TicketResponse[]): TicketResponse[] {
  return [...tickets].sort((left, right) => (left.position ?? 0) - (right.position ?? 0))
}

function getTicketColumnId(ticket: TicketResponse): string {
  return ticket.column?.id ?? ''
}

function getFirstAssigneeName(ticket: TicketResponse): string {
  return ticket.assignees?.[0]?.name ?? 'Unassigned'
}

function getDraftFromTicket(ticket: TicketResponse): TicketDraft {
  return {
    assigneeIds: ticket.assignees?.map((assignee) => assignee.id) ?? [],
    columnId: getTicketColumnId(ticket),
    description: ticket.description ?? '',
    labelIds: ticket.labels?.map((label) => label.id) ?? [],
    priority: (ticket.priority as ApiPriority | undefined) ?? 'medium',
    title: ticket.title,
  }
}

function hasId(items: { id: UUID }[] | undefined, id: UUID): boolean {
  return items?.some((item) => item.id === id) ?? false
}

function App() {
  const [accessToken, setAccessToken] = useState(() => localStorage.getItem(tokenStorageKey) ?? '')
  const [authMode, setAuthMode] = useState<AuthMode>('login')
  const [authForm, setAuthForm] = useState({
    email: '',
    name: '',
    password: '',
  })
  const [currentUser, setCurrentUser] = useState<UserResponse | null>(null)
  const [workspaces, setWorkspaces] = useState<WorkspaceResponse[]>([])
  const [boards, setBoards] = useState<BoardResponse[]>([])
  const [selectedWorkspaceId, setSelectedWorkspaceId] = useState(
    () => localStorage.getItem(workspaceStorageKey) ?? '',
  )
  const [selectedBoardId, setSelectedBoardId] = useState(
    () => localStorage.getItem(boardStorageKey) ?? '',
  )
  const [boardFull, setBoardFull] = useState<BoardFullResponse | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [draggedTicketId, setDraggedTicketId] = useState<string | null>(null)
  const [activeDropTarget, setActiveDropTarget] = useState<string | null>(null)
  const [splitByUser, setSplitByUser] = useState(false)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [isDarkMode, setIsDarkMode] = useState(false)
  const [selectedTicket, setSelectedTicket] = useState<TicketResponse | null>(null)
  const [ticketDraft, setTicketDraft] = useState<TicketDraft | null>(null)
  const [isEditingDescription, setIsEditingDescription] = useState(false)
  const [descriptionDraft, setDescriptionDraft] = useState('')
  const [ticketForm, setTicketForm] = useState<TicketForm>(emptyTicketForm)
  const [workspaceName, setWorkspaceName] = useState('')
  const [boardForm, setBoardForm] = useState({ description: '', name: '' })
  const [columnForm, setColumnForm] = useState({ color: starterColumns[0].color, name: '' })
  const [comments, setComments] = useState<CommentResponse[]>([])
  const [commentBody, setCommentBody] = useState('')
  const [workspaceMembers, setWorkspaceMembers] = useState<BoardMemberResponse[]>([])
  const [selectedUserIdToAdd, setSelectedUserIdToAdd] = useState('')
  const [selectedRoleToAdd, setSelectedRoleToAdd] = useState('Member')
  const [allUsers, setAllUsers] = useState<UserResponse[]>([])

  const columns = useMemo(() => sortColumns(boardFull?.columns ?? []), [boardFull])
  const tickets = useMemo(
    () => sortTickets((boardFull?.tickets ?? []).filter((ticket) => !ticket.archived)),
    [boardFull],
  )
  const members = boardFull?.members ?? []
  const labels = boardFull?.labels ?? []
  const visibleBoards = selectedWorkspaceId
    ? boards.filter((board) => board.workspaceId === selectedWorkspaceId)
    : boards

  const currentWorkspace = useMemo(() => {
    return workspaces.find((w) => w.id === selectedWorkspaceId)
  }, [workspaces, selectedWorkspaceId])

  const isWorkspaceOwner = useMemo(() => {
    return !!currentUser && !!currentWorkspace && currentWorkspace.ownerId === currentUser.id
  }, [currentUser, currentWorkspace])

  const allUsersList = useMemo(() => {
    const list = [...allUsers]
    if (currentUser && !list.some((u) => u.id === currentUser.id)) {
      list.unshift(currentUser)
    }
    return list
  }, [allUsers, currentUser])

  const availableUsersToAdd = useMemo(() => {
    const memberIds = workspaceMembers.map((m) => m.id)
    return allUsersList.filter((u) => !memberIds.includes(u.id))
  }, [allUsersList, workspaceMembers])

  useEffect(() => {
    if (!accessToken) {
      return
    }

    localStorage.setItem(tokenStorageKey, accessToken)
    void bootstrap(accessToken)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [accessToken])

  useEffect(() => {
    if (selectedWorkspaceId) {
      localStorage.setItem(workspaceStorageKey, selectedWorkspaceId)
      void loadWorkspaceMembers(selectedWorkspaceId)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedWorkspaceId])

  useEffect(() => {
    if (selectedBoardId) {
      localStorage.setItem(boardStorageKey, selectedBoardId)
      void loadBoard(selectedBoardId)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedBoardId])

  useEffect(() => {
    if (!selectedTicket || !accessToken) {
      return
    }

    void loadComments(selectedTicket.id)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTicket, accessToken])

  async function bootstrap(token: string) {
    setIsLoading(true)
    setError('')

    try {
      const [user, workspaceList, boardList, userList] = await Promise.all([
        api.me(token),
        api.listWorkspaces(token),
        api.listBoards(token),
        api.listUsers(token).catch(() => [] as UserResponse[]),
      ])

      setCurrentUser(user)
      setWorkspaces(workspaceList)
      setBoards(boardList)
      setAllUsers(userList)

      const savedWorkspace = localStorage.getItem(workspaceStorageKey)
      const workspaceId =
        workspaceList.find((workspace) => workspace.id === savedWorkspace)?.id ??
        workspaceList[0]?.id ??
        ''

      setSelectedWorkspaceId(workspaceId)

      const savedBoard = localStorage.getItem(boardStorageKey)
      const firstBoard =
        boardList.find((board) => board.id === savedBoard)?.id ??
        boardList.find((board) => board.workspaceId === workspaceId)?.id ??
        boardList[0]?.id ??
        ''

      setSelectedBoardId(firstBoard)
    } catch (caught) {
      logout()
      setError(caught instanceof Error ? caught.message : 'Could not load your session.')
    } finally {
      setIsLoading(false)
    }
  }

  async function refreshLists() {
    if (!accessToken) {
      return
    }

    const [workspaceList, boardList, userList] = await Promise.all([
      api.listWorkspaces(accessToken),
      api.listBoards(accessToken),
      api.listUsers(accessToken).catch(() => [] as UserResponse[]),
    ])
    setWorkspaces(workspaceList)
    setBoards(boardList)
    setAllUsers(userList)
  }

  async function loadBoard(boardId: string) {
    if (!accessToken || !boardId) {
      return
    }

    setIsLoading(true)
    setError('')

    try {
      const fullBoard = await api.getBoardFull(accessToken, boardId)
      setBoardFull(fullBoard)
      setTicketForm((currentForm) => ({
        ...currentForm,
        assigneeId: currentForm.assigneeId || fullBoard.members?.[0]?.id || '',
        columnId: currentForm.columnId || fullBoard.columns?.[0]?.id || '',
      }))
    } catch (caught) {
      setBoardFull(null)
      setError(caught instanceof Error ? caught.message : 'Could not load the board.')
    } finally {
      setIsLoading(false)
    }
  }

  async function loadComments(ticketId: UUID) {
    if (!accessToken) {
      return
    }

    try {
      setComments(await api.listComments(accessToken, ticketId))
    } catch {
      setComments([])
    }
  }

  async function loadWorkspaceMembers(workspaceId: string) {
    if (!accessToken || !workspaceId) {
      setWorkspaceMembers([])
      return
    }

    try {
      const membersList = await api.listWorkspaceMembers(accessToken, workspaceId)
      setWorkspaceMembers(membersList)
    } catch {
      setWorkspaceMembers([])
    }
  }

  async function addWorkspaceMember(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken || !selectedWorkspaceId || !selectedUserIdToAdd) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      const membersList = await api.addWorkspaceMember(
        accessToken,
        selectedWorkspaceId,
        selectedUserIdToAdd,
        isWorkspaceOwner ? selectedRoleToAdd : undefined,
      )
      setWorkspaceMembers(membersList)
      setSelectedUserIdToAdd('')
      setSelectedRoleToAdd('Member')
      if (selectedBoardId) {
        await loadBoard(selectedBoardId)
      }
      setNotice('Workspace member added.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not add workspace member.')
    } finally {
      setIsSaving(false)
    }
  }

  async function removeWorkspaceMember(userId: string) {
    if (!accessToken || !selectedWorkspaceId) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      await api.removeWorkspaceMember(accessToken, selectedWorkspaceId, userId)
      const membersList = await api.listWorkspaceMembers(accessToken, selectedWorkspaceId)
      setWorkspaceMembers(membersList)
      if (selectedBoardId) {
        await loadBoard(selectedBoardId)
      }
      setNotice('Workspace member removed.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not remove workspace member.')
    } finally {
      setIsSaving(false)
    }
  }

  function logout() {
    localStorage.removeItem(tokenStorageKey)
    localStorage.removeItem(boardStorageKey)
    localStorage.removeItem(workspaceStorageKey)
    setAccessToken('')
    setCurrentUser(null)
    setWorkspaces([])
    setBoards([])
    setSelectedWorkspaceId('')
    setSelectedBoardId('')
    setBoardFull(null)
    setSelectedTicket(null)
    setTicketDraft(null)
    setComments([])
  }

  async function submitAuth(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSaving(true)
    setError('')

    try {
      const authResponse =
        authMode === 'login'
          ? await api.login({ email: authForm.email, password: authForm.password })
          : await api.register(authForm)

      if (!authResponse.accessToken) {
        throw new Error('The backend did not return an access token.')
      }

      setCurrentUser(authResponse.user ?? null)
      setAccessToken(authResponse.accessToken)
      setNotice(authMode === 'login' ? 'Signed in.' : 'Account created.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Authentication failed.')
    } finally {
      setIsSaving(false)
    }
  }

  async function createWorkspace(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken || !currentUser || !workspaceName.trim()) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      const workspace = await api.createWorkspace(accessToken, workspaceName.trim(), currentUser.id)
      setWorkspaceName('')
      await refreshLists()
      setSelectedWorkspaceId(workspace.id)
      setNotice('Workspace saved.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not create workspace.')
    } finally {
      setIsSaving(false)
    }
  }

  async function createBoard(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken || !currentUser || !selectedWorkspaceId || !boardForm.name.trim()) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      const board = await api.createBoard(
        accessToken,
        selectedWorkspaceId,
        boardForm.name.trim(),
        currentUser.id,
        boardForm.description.trim() || undefined,
      )
      setBoardForm({ description: '', name: '' })
      await refreshLists()
      setSelectedBoardId(board.id)
      setNotice('Board saved.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not create board.')
    } finally {
      setIsSaving(false)
    }
  }

  async function createColumn(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken || !selectedBoardId || !columnForm.name.trim()) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      const column = await api.createColumn(accessToken, selectedBoardId, {
        color: columnForm.color,
        name: columnForm.name.trim(),
        position: columns.length + 1,
      })

      setBoardFull((currentBoard) =>
        currentBoard
          ? {
              ...currentBoard,
              columns: sortColumns([...(currentBoard.columns ?? []), column]),
            }
          : currentBoard,
      )
      setTicketForm((currentForm) => ({
        ...currentForm,
        columnId: currentForm.columnId || column.id,
      }))
      setColumnForm({ color: starterColumns[0].color, name: '' })
      setNotice('Column added.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not create column.')
    } finally {
      setIsSaving(false)
    }
  }

  async function createStarterColumns() {
    if (!accessToken || !selectedBoardId) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      const createdColumns: ColumnResponse[] = []

      for (const [index, column] of starterColumns.entries()) {
        createdColumns.push(
          await api.createColumn(accessToken, selectedBoardId, {
            ...column,
            position: columns.length + index + 1,
          }),
        )
      }

      setBoardFull((currentBoard) =>
        currentBoard
          ? {
              ...currentBoard,
              columns: sortColumns([...(currentBoard.columns ?? []), ...createdColumns]),
            }
          : currentBoard,
      )
      setTicketForm((currentForm) => ({
        ...currentForm,
        columnId: currentForm.columnId || createdColumns[0]?.id || '',
      }))
      setNotice('Starter columns added.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not create starter columns.')
      await loadBoard(selectedBoardId)
    } finally {
      setIsSaving(false)
    }
  }

  const ticketLabel = (count: number) => `${count} ${count === 1 ? 'ticket' : 'tickets'}`

  function getTicketsByColumn(boardTickets: TicketResponse[]) {
    return columns.reduce<Record<string, TicketResponse[]>>((groupedTickets, column) => {
      groupedTickets[column.id] = boardTickets.filter((ticket) => getTicketColumnId(ticket) === column.id)
      return groupedTickets
    }, {})
  }

  async function moveTicket(ticketId: string, destinationColumnId: string, assigneeId?: string) {
    if (!accessToken) {
      return
    }

    const ticket = tickets.find((currentTicket) => currentTicket.id === ticketId)

    if (!ticket) {
      return
    }

    const sourceColumnId = getTicketColumnId(ticket)
    const destinationTickets = tickets.filter(
      (currentTicket) => getTicketColumnId(currentTicket) === destinationColumnId,
    )

    setIsSaving(true)
    setError('')

    try {
      let updatedTicket = await api.moveTicket(accessToken, ticketId, {
        destinationColumnId,
        newPosition: destinationTickets.length + 1,
        sourceColumnId,
      })

      if (assigneeId && !hasId(updatedTicket.assignees, assigneeId)) {
        updatedTicket = await api.assignUser(accessToken, ticketId, assigneeId)
      }

      replaceTicket(updatedTicket)
      await loadBoard(selectedBoardId)
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not move ticket.')
      await loadBoard(selectedBoardId)
    } finally {
      setIsSaving(false)
    }
  }

  function replaceTicket(updatedTicket: TicketResponse) {
    setBoardFull((currentBoard) => {
      if (!currentBoard) {
        return currentBoard
      }

      return {
        ...currentBoard,
        tickets: (currentBoard.tickets ?? []).map((ticket) =>
          ticket.id === updatedTicket.id ? updatedTicket : ticket,
        ),
      }
    })
    setSelectedTicket((currentTicket) =>
      currentTicket?.id === updatedTicket.id ? updatedTicket : currentTicket,
    )
  }

  function closeModal() {
    setIsModalOpen(false)
    setTicketForm({
      ...emptyTicketForm,
      assigneeId: members[0]?.id ?? '',
      columnId: columns[0]?.id ?? '',
    })
  }

  function closeTicketDetails() {
    setSelectedTicket(null)
    setTicketDraft(null)
    setIsEditingDescription(false)
    setDescriptionDraft('')
    setCommentBody('')
    setComments([])
  }

  function openTicketDetails(ticket: TicketResponse) {
    setSelectedTicket(ticket)
    setTicketDraft(getDraftFromTicket(ticket))
    setIsEditingDescription(false)
    setDescriptionDraft(ticket.description ?? '')
    setComments([])
  }

  function updateTicketDraft(updates: Partial<TicketDraft>) {
    setTicketDraft((currentDraft) => (currentDraft ? { ...currentDraft, ...updates } : currentDraft))
  }

  function toggleDraftId(field: 'assigneeIds' | 'labelIds', id: string) {
    if (!ticketDraft) {
      return
    }

    const ids = ticketDraft[field]
    updateTicketDraft({
      [field]: ids.includes(id) ? ids.filter((currentId) => currentId !== id) : [...ids, id],
    })
  }

  function saveDescription() {
    if (!ticketDraft) {
      return
    }

    setTicketDraft({ ...ticketDraft, description: descriptionDraft.trim() })
    setIsEditingDescription(false)
  }

  async function saveTicketDetails() {
    if (!accessToken || !selectedTicket || !ticketDraft) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      const originalColumnId = getTicketColumnId(selectedTicket)
      let updatedTicket = selectedTicket

      if (ticketDraft.columnId && ticketDraft.columnId !== originalColumnId) {
        updatedTicket = await api.moveTicket(accessToken, selectedTicket.id, {
          destinationColumnId: ticketDraft.columnId,
          newPosition: tickets.filter((ticket) => getTicketColumnId(ticket) === ticketDraft.columnId).length + 1,
          sourceColumnId: originalColumnId,
        })
      }

      updatedTicket = await api.patchTicket(accessToken, selectedTicket.id, {
        columnId: ticketDraft.columnId,
        description: ticketDraft.description,
        priority: ticketDraft.priority,
        title: ticketDraft.title.trim(),
      })

      const originalAssigneeIds = selectedTicket.assignees?.map((assignee) => assignee.id) ?? []
      const originalLabelIds = selectedTicket.labels?.map((label) => label.id) ?? []

      for (const userId of ticketDraft.assigneeIds.filter((id) => !originalAssigneeIds.includes(id))) {
        updatedTicket = await api.assignUser(accessToken, selectedTicket.id, userId)
      }

      for (const userId of originalAssigneeIds.filter((id) => !ticketDraft.assigneeIds.includes(id))) {
        updatedTicket = await api.unassignUser(accessToken, selectedTicket.id, userId)
      }

      for (const labelId of ticketDraft.labelIds.filter((id) => !originalLabelIds.includes(id))) {
        updatedTicket = await api.addLabel(accessToken, selectedTicket.id, labelId)
      }

      for (const labelId of originalLabelIds.filter((id) => !ticketDraft.labelIds.includes(id))) {
        updatedTicket = await api.removeLabel(accessToken, selectedTicket.id, labelId)
      }

      replaceTicket(updatedTicket)
      await loadBoard(selectedBoardId)
      closeTicketDetails()
      setNotice('Ticket saved.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not save ticket.')
    } finally {
      setIsSaving(false)
    }
  }

  async function createTicket(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken || !currentUser || !selectedBoardId) {
      return
    }

    const title = ticketForm.title.trim()
    const description = ticketForm.description.trim()

    if (!title || !ticketForm.columnId) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      let ticket = await api.createTicket(accessToken, selectedBoardId, {
        columnId: ticketForm.columnId,
        createdBy: currentUser.id,
        description,
        position:
          tickets.filter((currentTicket) => getTicketColumnId(currentTicket) === ticketForm.columnId).length + 1,
        priority: ticketForm.priority,
        title,
      })

      if (ticketForm.assigneeId) {
        ticket = await api.assignUser(accessToken, ticket.id, ticketForm.assigneeId)
      }

      setBoardFull((currentBoard) =>
        currentBoard
          ? { ...currentBoard, tickets: [ticket, ...(currentBoard.tickets ?? [])] }
          : currentBoard,
      )
      await loadBoard(selectedBoardId)
      closeModal()
      setNotice('Ticket created.')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not create ticket.')
    } finally {
      setIsSaving(false)
    }
  }

  async function createComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!accessToken || !currentUser || !selectedTicket || !commentBody.trim()) {
      return
    }

    setIsSaving(true)
    setError('')

    try {
      const comment = await api.createComment(
        accessToken,
        selectedTicket.id,
        currentUser.id,
        commentBody.trim(),
      )
      setComments((currentComments) => [...currentComments, comment])
      setCommentBody('')
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : 'Could not add comment.')
    } finally {
      setIsSaving(false)
    }
  }

  function selectWorkspace(workspaceId: string) {
    setSelectedWorkspaceId(workspaceId)
    const nextBoard = boards.find((board) => board.workspaceId === workspaceId)
    setSelectedBoardId(nextBoard?.id ?? '')
    setBoardFull(null)
  }

  function renderStatusMessage() {
    if (error) {
      return <div className="notification is-danger is-light">{error}</div>
    }

    if (notice) {
      return (
        <div className="notification is-success is-light">
          <button className="delete" onClick={() => setNotice('')} type="button" />
          {notice}
        </div>
      )
    }

    return null
  }

  function renderAuth() {
    return (
      <jelly-theme mode={isDarkMode ? 'dark' : 'light'}>
      <main className={`app-shell ${isDarkMode ? 'dark' : ''}`}>
        <section className="section auth-shell">
          <div className="auth-panel">
            <p className="eyebrow">Alpine</p>
            <h1 className="title is-2">{authMode === 'login' ? 'Sign in' : 'Create account'}</h1>
            <p className="subtitle is-6">Connect to the Alpine backend and load your boards.</p>
            {renderStatusMessage()}
            <form onSubmit={submitAuth}>
              {authMode === 'register' && (
                <div className="field">
                  <label className="label" htmlFor="auth-name">
                    Name
                  </label>
                  <input
                    className="input"
                    id="auth-name"
                    onChange={(event) => setAuthForm((form) => ({ ...form, name: event.target.value }))}
                    required
                    type="text"
                    value={authForm.name}
                  />
                </div>
              )}
              <div className="field">
                <label className="label" htmlFor="auth-email">
                  Email
                </label>
                <input
                  className="input"
                  id="auth-email"
                  onChange={(event) => setAuthForm((form) => ({ ...form, email: event.target.value }))}
                  required
                  type="email"
                  value={authForm.email}
                />
              </div>
              <div className="field">
                <label className="label" htmlFor="auth-password">
                  Password
                </label>
                <input
                  className="input"
                  id="auth-password"
                  minLength={8}
                  onChange={(event) => setAuthForm((form) => ({ ...form, password: event.target.value }))}
                  required
                  type="password"
                  value={authForm.password}
                />
              </div>
              <div className="auth-actions">
                <button className={`button is-primary ${isSaving ? 'is-loading' : ''}`} type="submit">
                  {authMode === 'login' ? 'Sign in' : 'Register'}
                </button>
                <button
                  className="button is-text"
                  onClick={() => setAuthMode(authMode === 'login' ? 'register' : 'login')}
                  type="button"
                >
                  {authMode === 'login' ? 'Need an account?' : 'Have an account?'}
                </button>
              </div>
            </form>
          </div>
        </section>
      </main>
      </jelly-theme>
    )
  }

  function renderBoard(boardTickets: TicketResponse[], boardId: string, assigneeId?: string) {
    const ticketsByColumn = getTicketsByColumn(boardTickets)

    return (
      <div className="kanban-board" style={{ '--column-count': columns.length || 1 } as React.CSSProperties}>
        {columns.map((column) => {
          const columnTickets = ticketsByColumn[column.id] ?? []
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
                  void moveTicket(draggedTicketId, column.id, assigneeId)
                }
                setDraggedTicketId(null)
                setActiveDropTarget(null)
              }}
            >
              <header className="column-header">
                <div>
                  <span className="tag column-tag" style={{ borderColor: column.color, color: column.color }}>
                    {column.name}
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
                      <span className={`tag priority-${ticket.priority ?? 'medium'}`}>
                        {formatPriority(ticket.priority)}
                      </span>
                    </div>
                    {(ticket.labels?.length ?? 0) > 0 && (
                      <div className="ticket-labels">
                        {ticket.labels?.map((label) => (
                          <span className="ticket-label" key={label.id} style={{ backgroundColor: label.color }}>
                            {label.name}
                          </span>
                        ))}
                      </div>
                    )}
                    <footer>
                      <div className="assignee-info">
                        <div
                          className="profile-pic"
                          style={{ backgroundColor: getProfileColor(getFirstAssigneeName(ticket)) }}
                        >
                          {getInitials(getFirstAssigneeName(ticket))}
                        </div>
                        <span>{getFirstAssigneeName(ticket)}</span>
                      </div>
                      <span>#{ticket.id.slice(0, 8)}</span>
                    </footer>
                  </article>
                ))}

                {columnTickets.length === 0 && <div className="empty-column">Drop a ticket here</div>}
              </div>
            </article>
          )
        })}
      </div>
    )
  }

  if (!accessToken) {
    return renderAuth()
  }

  return (
    <jelly-theme mode={isDarkMode ? 'dark' : 'light'}>
    <main className={`app-shell ${isDarkMode ? 'dark' : ''}`}>
      <section className="section board-header">
        <div>
          <p className="eyebrow">Product operations</p>
          <h1 className="title is-2">{boardFull?.board?.name ?? 'Ticket tracker'}</h1>
          <p className="subtitle is-6">
            {boardFull?.board?.description || 'Plan, hand off, and complete work across your board.'}
          </p>
        </div>

        <div className="board-actions">
          <button
            className="button is-outlined"
            onClick={() => setIsDarkMode(!isDarkMode)}
            type="button"
          >
            {isDarkMode ? 'Light' : 'Dark'}
          </button>

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

          <button className="button" onClick={logout} type="button">
            Sign out
          </button>

          <button
            className="button is-primary"
            disabled={!selectedBoardId || columns.length === 0}
            type="button"
            onClick={() => {
              setTicketForm({
                ...emptyTicketForm,
                assigneeId: members[0]?.id ?? '',
                columnId: columns[0]?.id ?? '',
              })
              setIsModalOpen(true)
            }}
          >
            <span aria-hidden="true">+</span>
            <span>New ticket</span>
          </button>
        </div>
      </section>

      <section className="section board-section">
        {renderStatusMessage()}

        <div className="project-controls">
          <section className="project-list" aria-label="Workspaces">
            <div className="project-list-header">
              <h2>Workspaces</h2>
            </div>
            <div className="button-list">
              {workspaces.map((workspace) => (
                <button
                  className={`button project-button ${workspace.id === selectedWorkspaceId ? 'is-primary' : ''}`}
                  key={workspace.id}
                  onClick={() => selectWorkspace(workspace.id)}
                  type="button"
                >
                  {workspace.name}
                </button>
              ))}
            </div>
            <form className="inline-form" onSubmit={createWorkspace}>
              <input
                className="input"
                onChange={(event) => setWorkspaceName(event.target.value)}
                placeholder="New workspace"
                value={workspaceName}
              />
              <button className={`button ${isSaving ? 'is-loading' : ''}`} type="submit">
                Add
              </button>
            </form>
          </section>

          <section className="project-list" aria-label="Boards">
            <div className="project-list-header">
              <h2>Boards</h2>
            </div>
            <div className="button-list">
              {visibleBoards.map((board) => (
                <button
                  className={`button project-button ${board.id === selectedBoardId ? 'is-primary' : ''}`}
                  key={board.id}
                  onClick={() => setSelectedBoardId(board.id)}
                  type="button"
                >
                  {board.name}
                </button>
              ))}
            </div>
            <form className="inline-form board-create-form" onSubmit={createBoard}>
              <input
                className="input"
                onChange={(event) => setBoardForm((form) => ({ ...form, name: event.target.value }))}
                placeholder="New board"
                value={boardForm.name}
              />
              <input
                className="input"
                onChange={(event) => setBoardForm((form) => ({ ...form, description: event.target.value }))}
                placeholder="Description"
                value={boardForm.description}
              />
              <button className={`button ${isSaving ? 'is-loading' : ''}`} disabled={!selectedWorkspaceId} type="submit">
                Add
              </button>
            </form>
          </section>
        </div>

        {selectedBoardId && (
          <section className="column-tools" aria-label="Columns">
            <div>
              <h2>Columns</h2>
              <p>{columns.length === 0 ? 'Create lanes before adding tickets.' : `${columns.length} lanes on this board.`}</p>
            </div>
            <form className="inline-form column-create-form" onSubmit={createColumn}>
              <input
                className="input"
                onChange={(event) => setColumnForm((form) => ({ ...form, name: event.target.value }))}
                placeholder="New column"
                value={columnForm.name}
              />
              <div className="color-control">
                <span className="color-preview" style={{ backgroundColor: columnForm.color }} />
                <input
                  aria-label="Column color"
                  onChange={(event) => setColumnForm((form) => ({ ...form, color: event.target.value }))}
                  type="color"
                  value={columnForm.color}
                />
              </div>
              <button className={`button ${isSaving ? 'is-loading' : ''}`} type="submit">
                Add column
              </button>
              {columns.length === 0 && (
                <button
                  className={`button is-outlined ${isSaving ? 'is-loading' : ''}`}
                  onClick={() => void createStarterColumns()}
                  type="button"
                >
                  Add starter columns
                </button>
              )}
            </form>
          </section>
        )}

        {selectedWorkspaceId && (
          <section className="column-tools workspace-members-tools" aria-label="Workspace Members">
            <div>
              <h2>Workspace Members</h2>
              <p>{workspaceMembers.length === 0 ? 'No members in workspace.' : `${workspaceMembers.length} members in workspace.`}</p>
            </div>
            <div className="workspace-members-list">
              {workspaceMembers.map((member) => (
                <div className="member-pill" key={member.id}>
                  <div className="profile-pic" style={{ backgroundColor: getProfileColor(member.name) }}>
                    {getInitials(member.name)}
                  </div>
                  <div className="member-info">
                    <strong>{member.name}</strong>
                    <span>{member.role || 'Member'}</span>
                  </div>
                  <button
                    className="delete is-small"
                    onClick={() => void removeWorkspaceMember(member.id)}
                    type="button"
                    title="Remove member"
                  />
                </div>
              ))}
            </div>
            <form className={`inline-form member-create-form ${isWorkspaceOwner ? 'has-role-select' : ''}`} onSubmit={addWorkspaceMember}>
              <div className="select is-fullwidth">
                <select
                  value={selectedUserIdToAdd}
                  onChange={(event) => setSelectedUserIdToAdd(event.target.value)}
                  aria-label="Select user to add"
                >
                  <option value="">Select user to add...</option>
                  {availableUsersToAdd.map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.name} ({user.email})
                    </option>
                  ))}
                </select>
              </div>
              {isWorkspaceOwner && (
                <div className="select is-fullwidth">
                  <select
                    value={selectedRoleToAdd}
                    onChange={(event) => setSelectedRoleToAdd(event.target.value)}
                    aria-label="Select member role"
                  >
                    {roleOptions.map((role) => (
                      <option key={role} value={role}>
                        {role}
                      </option>
                    ))}
                  </select>
                </div>
              )}
              <button className={`button ${isSaving ? 'is-loading' : ''}`} disabled={!selectedUserIdToAdd} type="submit">
                Add member
              </button>
            </form>
          </section>
        )}
      </section>

      <section className="section board-section" aria-label="Kanban board">
        {isLoading && <progress className="progress is-small is-primary" max="100" />}
        {!isLoading && selectedBoardId && columns.length === 0 && (
          <div className="empty-board">
            <h2>No columns yet</h2>
            <p>Add a column above, or create a standard Kanban flow in one click.</p>
            <button
              className={`button is-primary ${isSaving ? 'is-loading' : ''}`}
              onClick={() => void createStarterColumns()}
              type="button"
            >
              Add starter columns
            </button>
          </div>
        )}
        {!isLoading && !selectedBoardId && (
          <div className="empty-board">Choose or create a board to load its tickets.</div>
        )}
        {!isLoading &&
          selectedBoardId &&
          columns.length > 0 &&
          (splitByUser ? (
            <div className="user-board-stack">
              {members.map((member) => {
                const userTickets = tickets.filter((ticket) => hasId(ticket.assignees, member.id))

                return (
                  <section className="user-board-section" key={member.id}>
                    <div className="user-divider">
                      <div>
                        <h2>{member.name}</h2>
                        <p>{member.role}</p>
                      </div>
                      <span>{ticketLabel(userTickets.length)}</span>
                    </div>
                    {renderBoard(userTickets, member.id, member.id)}
                  </section>
                )
              })}
            </div>
          ) : (
            renderBoard(tickets, 'all')
          ))}
      </section>

      <div className={`modal ${isModalOpen ? 'is-active' : ''}`}>
        <div className="modal-background" onClick={closeModal} role="presentation" />
        <div className="modal-card">
          <header className="modal-card-head">
            <p className="modal-card-title">Create ticket</p>
            <button aria-label="close" className="delete" onClick={closeModal} type="button" />
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
                    onChange={(event) => setTicketForm((form) => ({ ...form, title: event.target.value }))}
                    required
                    type="text"
                    value={ticketForm.title}
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
                    onChange={(event) => setTicketForm((form) => ({ ...form, description: event.target.value }))}
                    rows={3}
                    value={ticketForm.description}
                  />
                </div>
              </div>

              <div className="form-grid">
                <div className="field">
                  <label className="label" htmlFor="ticket-assignee">
                    Assignee
                  </label>
                  <div className="select is-fullwidth">
                    <select
                      id="ticket-assignee"
                      onChange={(event) => setTicketForm((form) => ({ ...form, assigneeId: event.target.value }))}
                      value={ticketForm.assigneeId}
                    >
                      <option value="">Unassigned</option>
                      {members.map((member) => (
                        <option key={member.id} value={member.id}>
                          {member.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="field">
                  <label className="label" htmlFor="ticket-priority">
                    Priority
                  </label>
                  <div className="select is-fullwidth">
                    <select
                      id="ticket-priority"
                      onChange={(event) =>
                        setTicketForm((form) => ({ ...form, priority: event.target.value as ApiPriority }))
                      }
                      value={ticketForm.priority}
                    >
                      {priorityOptions.map((priority) => (
                        <option key={priority} value={priority}>
                          {formatPriority(priority)}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="field">
                  <label className="label" htmlFor="ticket-column">
                    Column
                  </label>
                  <div className="select is-fullwidth">
                    <select
                      id="ticket-column"
                      onChange={(event) => setTicketForm((form) => ({ ...form, columnId: event.target.value }))}
                      required
                      value={ticketForm.columnId}
                    >
                      {columns.map((column) => (
                        <option key={column.id} value={column.id}>
                          {column.name}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </div>
            </section>

            <footer className="modal-card-foot">
              <button className={`button is-primary ${isSaving ? 'is-loading' : ''}`} type="submit">
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
        <div className="modal-background" onClick={closeTicketDetails} role="presentation" />
        <div className="modal-card ticket-modal">
          <header className="modal-card-head">
            <div>
              <p className="modal-card-title">Ticket details</p>
              {selectedTicket && <p className="modal-card-subtitle">#{selectedTicket.id.slice(0, 8)}</p>}
            </div>
            <button aria-label="close" className="delete" onClick={closeTicketDetails} type="button" />
          </header>

          {selectedTicket && ticketDraft && (
            <>
              <section className="modal-card-body">
                <div className="field">
                  <label className="label" htmlFor="detail-title">
                    Title
                  </label>
                  <input
                    className="input"
                    id="detail-title"
                    onChange={(event) => updateTicketDraft({ title: event.target.value })}
                    value={ticketDraft.title}
                  />
                </div>

                <div className="ticket-detail-section">
                  <p className="detail-label">Description</p>
                  {isEditingDescription ? (
                    <div className="description-editor">
                      <textarea
                        aria-label="Ticket description"
                        className="textarea"
                        onChange={(event) => setDescriptionDraft(event.target.value)}
                        rows={4}
                        value={descriptionDraft}
                      />
                      <div className="description-actions">
                        <button className="button is-primary" onClick={saveDescription} type="button">
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
                      onClick={() => {
                        setDescriptionDraft(ticketDraft.description)
                        setIsEditingDescription(true)
                      }}
                      type="button"
                    >
                      {ticketDraft.description || 'Add a description'}
                    </button>
                  )}
                </div>

                <div className="detail-grid">
                  <div>
                    <p className="detail-label">Priority</p>
                    <div className="select is-fullwidth detail-select">
                      <select
                        aria-label="Ticket priority"
                        onChange={(event) => updateTicketDraft({ priority: event.target.value as ApiPriority })}
                        value={ticketDraft.priority}
                      >
                        {priorityOptions.map((priority) => (
                          <option key={priority} value={priority}>
                            {formatPriority(priority)}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                  <div>
                    <p className="detail-label">Column</p>
                    <div className="select is-fullwidth detail-select">
                      <select
                        aria-label="Ticket column"
                        onChange={(event) => updateTicketDraft({ columnId: event.target.value })}
                        value={ticketDraft.columnId}
                      >
                        {columns.map((column) => (
                          <option key={column.id} value={column.id}>
                            {column.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>
                </div>

                <div className="ticket-detail-section">
                  <p className="detail-label">Assignees</p>
                  <div className="choice-grid">
                    {members.map((member) => (
                      <label className="choice-pill" key={member.id}>
                        <input
                          checked={ticketDraft.assigneeIds.includes(member.id)}
                          onChange={() => toggleDraftId('assigneeIds', member.id)}
                          type="checkbox"
                        />
                        <span>{member.name}</span>
                      </label>
                    ))}
                  </div>
                </div>

                <div className="ticket-detail-section">
                  <p className="detail-label">Labels</p>
                  <div className="choice-grid">
                    {labels.map((label: LabelResponse) => (
                      <label className="choice-pill" key={label.id}>
                        <input
                          checked={ticketDraft.labelIds.includes(label.id)}
                          onChange={() => toggleDraftId('labelIds', label.id)}
                          type="checkbox"
                        />
                        <span className="label-swatch" style={{ backgroundColor: label.color }} />
                        <span>{label.name}</span>
                      </label>
                    ))}
                    {labels.length === 0 && <p className="muted-copy">No labels are available for this board.</p>}
                  </div>
                </div>

                <div className="ticket-detail-section">
                  <p className="detail-label">Comments</p>
                  <div className="comment-list">
                    {comments.map((comment) => (
                      <article className="comment-item" key={comment.id}>
                        <strong>{comment.authorName}</strong>
                        <p>{comment.body}</p>
                      </article>
                    ))}
                    {comments.length === 0 && <p className="muted-copy">No comments yet.</p>}
                  </div>
                  <form className="comment-form" onSubmit={createComment}>
                    <textarea
                      className="textarea"
                      onChange={(event) => setCommentBody(event.target.value)}
                      placeholder="Add a comment"
                      rows={2}
                      value={commentBody}
                    />
                    <button className={`button ${isSaving ? 'is-loading' : ''}`} type="submit">
                      Comment
                    </button>
                  </form>
                </div>
              </section>

              <footer className="modal-card-foot">
                <button className={`button is-primary ${isSaving ? 'is-loading' : ''}`} onClick={saveTicketDetails} type="button">
                  Save
                </button>
                <button className="button" onClick={closeTicketDetails} type="button">
                  Cancel
                </button>
              </footer>
            </>
          )}
        </div>
      </div>
    </main>
    </jelly-theme>
  )
}

export default App
