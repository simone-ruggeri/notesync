import { defineStore } from 'pinia'
import type { ApiError } from '~/types/api'

export const useAuthStore = defineStore('auth', () => {
  // --- State ---
  // ref() crea una variabile reattiva: quando cambia, Vue aggiorna automaticamente
  // i componenti che la usano. Inizializziamo da localStorage solo lato client.
  const token = ref<string | null>(null)
  const userId = ref<string | null>(null)
  const email = ref<string | null>(null)
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // --- Getters ---
  // computed() è un valore derivato che si ricalcola automaticamente
  // quando le sue dipendenze (token, email) cambiano.
  const isAuthenticated = computed(() => !!token.value)

  const userInitial = computed(() => {
    return email.value ? email.value[0].toUpperCase() : '?'
  })

  // --- Hydration da localStorage ---
  // localStorage non esiste durante SSR (il server non ha un browser).
  // Questa funzione va chiamata solo lato client (onMounted nei componenti).
  function hydrate() {
    // import.meta.client è true solo nel browser — mai sul server Node.js
    if (!import.meta.client) return
    token.value = localStorage.getItem('notesync_token')
    userId.value = localStorage.getItem('notesync_userId')
    email.value = localStorage.getItem('notesync_email')
  }

  // --- Actions ---
  async function login(emailInput: string, password: string) {
    isLoading.value = true
    error.value = null
    try {
      const api = useApi()
      const response = await api.login(emailInput, password)
      _saveSession(response.token, response.userId, emailInput)
      await navigateTo('/notes')
    } catch (err) {
      error.value = _parseError(err, 'Credenziali non valide')
    } finally {
      // finally viene eseguito sempre, anche se c'è un errore
      isLoading.value = false
    }
  }

  async function register(emailInput: string, password: string) {
    isLoading.value = true
    error.value = null
    try {
      const api = useApi()
      const response = await api.register(emailInput, password)
      _saveSession(response.token, response.userId, emailInput)
      await navigateTo('/notes')
    } catch (err) {
      error.value = _parseError(err, 'Registrazione fallita')
    } finally {
      isLoading.value = false
    }
  }

  async function logout() {
    localStorage.removeItem('notesync_token')
    localStorage.removeItem('notesync_userId')
    localStorage.removeItem('notesync_email')
    token.value = null
    userId.value = null
    email.value = null
    await navigateTo('/login')
  }

  function clearError() {
    error.value = null
  }

  // --- Helpers privati ---
  function _saveSession(newToken: string, newUserId: string, newEmail: string) {
    token.value = newToken
    userId.value = newUserId
    email.value = newEmail
    localStorage.setItem('notesync_token', newToken)
    localStorage.setItem('notesync_userId', newUserId)
    localStorage.setItem('notesync_email', newEmail)
  }

  function _parseError(err: unknown, fallback: string): string {
    const apiErr = err as ApiError
    if (apiErr?.statusCode === 409) return 'Email già registrata'
    if (apiErr?.statusCode === 401) return 'Credenziali non valide'
    if (apiErr?.message) return apiErr.message
    return fallback
  }

  return {
    token, userId, email, isLoading, error,
    isAuthenticated, userInitial,
    hydrate, login, register, logout, clearError
  }
})
