import type { AuthResponse, Note, ApiError } from '~/types/api'

export function useApi() {
  const config = useRuntimeConfig()
  const baseURL = config.public.apiBase as string

  function getToken(): string | null {
    if (!import.meta.client) return null
    return localStorage.getItem('notesync_token')
  }

  async function request<T>(path: string, options: Parameters<typeof $fetch>[1] = {}): Promise<T> {
    const token = getToken()

    try {
      return await $fetch<T>(path, {
        baseURL,
        ...options,
        headers: {
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
          ...(options.headers ?? {})
        }
      })
    } catch (err: unknown) {
      const fetchError = err as { statusCode?: number; status?: number; data?: { message?: string } }
      const statusCode = fetchError.statusCode ?? fetchError.status ?? 500

      if (statusCode === 401) {
        // Token scaduto: pulisce lo stato locale prima di mandare al login
        if (import.meta.client) {
          localStorage.removeItem('notesync_token')
          localStorage.removeItem('notesync_userId')
          localStorage.removeItem('notesync_email')
        }
        await navigateTo('/login')
      }

      const apiError: ApiError = {
        statusCode,
        message: fetchError.data?.message ?? 'Errore di rete'
      }
      throw apiError
    }
  }

  async function login(email: string, password: string): Promise<AuthResponse> {
    return request<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: { email, password }
    })
  }

  async function register(email: string, password: string): Promise<AuthResponse> {
    return request<AuthResponse>('/api/auth/register', {
      method: 'POST',
      body: { email, password }
    })
  }

  async function getNotes(): Promise<Note[]> {
    return request<Note[]>('/api/notes')
  }

  async function createNote(title: string, content: string): Promise<Note> {
    return request<Note>('/api/notes', {
      method: 'POST',
      body: { title, content }
    })
  }

  async function updateNote(id: string, title: string, content: string): Promise<Note> {
    return request<Note>(`/api/notes/${id}`, {
      method: 'PUT',
      body: { title, content }
    })
  }

  async function deleteNote(id: string): Promise<void> {
    return request<void>(`/api/notes/${id}`, { method: 'DELETE' })
  }

  return { login, register, getNotes, createNote, updateNote, deleteNote }
}
