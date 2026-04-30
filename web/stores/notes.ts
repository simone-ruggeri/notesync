import { defineStore } from 'pinia'
import type { Note } from '~/types/api'

export const useNotesStore = defineStore('notes', () => {
  const notes = ref<Note[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)
  const searchQuery = ref('')

  const filteredNotes = computed(() => {
    const q = searchQuery.value.toLowerCase().trim()
    const list = q
      ? notes.value.filter(n =>
          n.title.toLowerCase().includes(q) ||
          n.content.toLowerCase().includes(q)
        )
      : notes.value
    return [...list].sort((a, b) => b.updatedAt - a.updatedAt)
  })

  async function fetchNotes() {
    isLoading.value = true
    error.value = null
    try {
      const api = useApi()
      notes.value = await api.getNotes()
    } catch {
      error.value = 'Impossibile caricare le note'
    } finally {
      isLoading.value = false
    }
  }

  async function createNote(title: string, content: string): Promise<Note | null> {
    isLoading.value = true
    error.value = null
    try {
      const api = useApi()
      const newNote = await api.createNote(title, content)
      notes.value.unshift(newNote)
      useToast().success('Nota creata')
      return newNote
    } catch {
      useToast().error('Impossibile creare la nota')
      return null
    } finally {
      isLoading.value = false
    }
  }

  async function updateNote(id: string, title: string, content: string): Promise<boolean> {
    isLoading.value = true
    error.value = null
    try {
      const api = useApi()
      const updated = await api.updateNote(id, title, content)
      const idx = notes.value.findIndex(n => n.id === id)
      if (idx !== -1) notes.value[idx] = updated
      useToast().success('Nota salvata')
      return true
    } catch {
      useToast().error('Impossibile salvare la nota')
      return false
    } finally {
      isLoading.value = false
    }
  }

  async function deleteNote(id: string): Promise<boolean> {
    isLoading.value = true
    error.value = null
    try {
      const api = useApi()
      await api.deleteNote(id)
      notes.value = notes.value.filter(n => n.id !== id)
      useToast().success('Nota eliminata')
      return true
    } catch {
      useToast().error('Impossibile eliminare la nota')
      return false
    } finally {
      isLoading.value = false
    }
  }

  function setSearchQuery(query: string) {
    searchQuery.value = query
  }

  function clearError() {
    error.value = null
  }

  return {
    notes, isLoading, error, searchQuery,
    filteredNotes,
    fetchNotes, createNote, updateNote, deleteNote,
    setSearchQuery, clearError
  }
})
