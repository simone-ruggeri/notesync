<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const route = useRoute()       // accede ai parametri dell'URL
const notesStore = useNotesStore()

// route.params.id contiene la parte variabile dell'URL: /notes/abc123 → "abc123"
const noteId = route.params.id as string

const title = ref('')
const content = ref('')
const isSaving = ref(false)
const confirmOpen = ref(false)
const lastUpdated = ref<number | null>(null)

const canSave = computed(() => title.value.trim().length > 0 && !isSaving.value)

onMounted(async () => {
  // Cerca prima nello store locale (già caricato dalla lista note).
  // Evita una chiamata API extra se l'utente arriva dalla lista.
  let note = notesStore.notes.find(n => n.id === noteId)

  if (!note) {
    // Fallback: accesso diretto all'URL (link esterno o refresh pagina)
    await notesStore.fetchNotes()
    note = notesStore.notes.find(n => n.id === noteId)
  }

  if (note) {
    title.value = note.title
    content.value = note.content
    lastUpdated.value = note.updatedAt
  } else {
    navigateTo('/notes')
  }
})

async function save() {
  if (!canSave.value) return
  isSaving.value = true
  const ok = await notesStore.updateNote(noteId, title.value.trim(), content.value)
  isSaving.value = false
  if (ok) await navigateTo('/notes')
}

async function handleDelete() {
  const ok = await notesStore.deleteNote(noteId)
  if (ok) await navigateTo('/notes')
}

function formatLastUpdated(ts: number): string {
  return new Intl.DateTimeFormat('it-IT', {
    day: 'numeric', month: 'short',
    hour: '2-digit', minute: '2-digit'
  }).format(new Date(ts))
}
</script>

<template>
  <div class="min-h-screen bg-white flex flex-col">

    <!-- Header editor -->
    <header class="sticky top-0 z-40 bg-slate-primary shadow-sm">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
        <NuxtLink to="/notes"
          class="text-white/70 hover:text-white transition-colors p-1 -ml-1">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 19l-7-7 7-7"/>
          </svg>
        </NuxtLink>

        <span class="text-white font-medium">Modifica nota</span>

        <button
          :disabled="!canSave"
          class="text-white/70 hover:text-white disabled:text-white/30 transition-colors p-1 -mr-1"
          @click="save"
        >
          <svg v-if="isSaving" class="w-6 h-6 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
          </svg>
        </button>
      </div>
    </header>

    <!-- Area editor -->
    <div class="flex-1 max-w-3xl mx-auto w-full px-6 py-5 flex flex-col">
      <input
        v-model="title"
        type="text"
        placeholder="Titolo"
        class="w-full text-xl font-semibold text-slate-primary placeholder-gray-300
               border-none outline-none bg-transparent"
      />
      <div class="h-px bg-gray-100 my-4" />
      <textarea
        v-model="content"
        placeholder="Scrivi qui..."
        class="flex-1 w-full text-gray-700 placeholder-gray-300 text-[15px] leading-relaxed
               border-none outline-none bg-transparent resize-none min-h-[50vh]"
      />
    </div>

    <!-- Footer: data ultima modifica + bottone elimina -->
    <footer class="max-w-3xl mx-auto w-full px-6 py-4 border-t border-gray-100
                   flex items-center justify-between">
      <span v-if="lastUpdated" class="text-xs text-gray-400">
        Modificato {{ formatLastUpdated(lastUpdated) }}
      </span>
      <span v-else class="text-xs text-gray-400" />

      <button
        class="flex items-center gap-1.5 text-sm text-red-400 hover:text-red-600 transition-colors"
        @click="confirmOpen = true"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5
               4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
        </svg>
        Elimina
      </button>
    </footer>

    <ConfirmDialog
      :open="confirmOpen"
      title="Elimina nota"
      message="Sei sicuro? L'operazione non può essere annullata."
      @confirm="handleDelete"
      @cancel="confirmOpen = false"
    />
  </div>
</template>
