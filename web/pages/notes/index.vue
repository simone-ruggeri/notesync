<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const authStore = useAuthStore()
const notesStore = useNotesStore()

// Stato locale per il dialogo di eliminazione.
// Teniamo traccia di quale nota sta per essere eliminata.
const confirmOpen = ref(false)
const noteToDelete = ref<string | null>(null)

// onMounted si esegue solo nel browser, dopo che Vue ha montato il componente.
// È il posto giusto per le chiamate API iniziali.
onMounted(() => {
  notesStore.fetchNotes()
})

function openDeleteDialog(id: string) {
  noteToDelete.value = id
  confirmOpen.value = true
}

async function handleDelete() {
  if (!noteToDelete.value) return
  await notesStore.deleteNote(noteToDelete.value)
  confirmOpen.value = false
  noteToDelete.value = null
}
</script>

<template>
  <div class="min-h-screen bg-warm-bg">

    <!-- Header fisso in cima — rimane visibile anche facendo scroll -->
    <header class="sticky top-0 z-40 bg-slate-primary shadow-md">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">
        <h1 class="text-white font-bold text-lg tracking-tight">NoteSync</h1>

        <div class="flex items-center gap-3">
          <!-- Avatar con l'iniziale dell'email — mostra chi è loggato -->
          <div class="w-8 h-8 rounded-full bg-sage-accent flex items-center justify-center">
            <span class="text-white text-sm font-bold">{{ authStore.userInitial }}</span>
          </div>

          <!-- Bottone logout con icona SVG -->
          <button
            class="text-white/70 hover:text-white transition-colors p-1"
            title="Esci"
            @click="authStore.logout()"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7
                   a3 3 0 013-3h4a3 3 0 013 3v1"/>
            </svg>
          </button>
        </div>
      </div>
    </header>

    <main class="max-w-3xl mx-auto px-4 py-4">

      <!-- SearchBar -->
      <!-- v-model è un binding bidirezionale: aggiorna searchQuery nello store
           ad ogni tasto premuto, il getter filteredNotes si ricalcola automaticamente -->
      <div class="relative mb-4">
        <svg class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
             fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0"/>
        </svg>
        <input
          :value="notesStore.searchQuery"
          type="search"
          placeholder="Cerca nelle note..."
          class="w-full pl-9 pr-4 py-2.5 bg-white rounded-xl border border-gray-200
                 text-sm focus:outline-none focus:ring-2 focus:ring-slate-primary/30
                 focus:border-slate-primary transition-colors"
          @input="notesStore.setSearchQuery(($event.target as HTMLInputElement).value)"
        />
      </div>

      <!-- Stato: caricamento -->
      <div v-if="notesStore.isLoading && !notesStore.notes.length"
           class="flex justify-center py-16">
        <svg class="w-8 h-8 animate-spin text-slate-primary/40" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
      </div>

      <!-- Stato: errore -->
      <div v-else-if="notesStore.error"
           class="bg-red-50 border border-red-200 rounded-xl p-4 text-center">
        <p class="text-red-600 text-sm mb-3">{{ notesStore.error }}</p>
        <button
          class="text-sm text-red-600 underline hover:no-underline"
          @click="notesStore.fetchNotes()"
        >
          Riprova
        </button>
      </div>

      <!-- Stato: lista vuota (nessuna nota creata) -->
      <div v-else-if="!notesStore.filteredNotes.length && !notesStore.searchQuery"
           class="text-center py-16">
        <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586
                 a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
        </div>
        <p class="text-gray-500 font-medium">Nessuna nota</p>
        <p class="text-gray-400 text-sm mt-1">Tocca + per creare la prima nota</p>
      </div>

      <!-- Stato: nessun risultato di ricerca -->
      <div v-else-if="!notesStore.filteredNotes.length && notesStore.searchQuery"
           class="text-center py-16">
        <p class="text-gray-500">Nessun risultato per "<strong>{{ notesStore.searchQuery }}</strong>"</p>
      </div>

      <!-- Lista note: grid 1 colonna su mobile, 2 su tablet/desktop -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <!-- v-for genera un NoteCard per ogni nota filtrata.
             :key è obbligatorio: aiuta Vue a capire quale elemento aggiornare
             quando l'array cambia, senza rifare il render di tutto. -->
        <div
          v-for="note in notesStore.filteredNotes"
          :key="note.id"
          class="relative group"
        >
          <NoteCard :note="note" />

          <!-- Bottone elimina: appare all'hover sulla card -->
          <button
            class="absolute top-2 right-2 w-7 h-7 rounded-full bg-white shadow-sm border border-gray-200
                   flex items-center justify-center opacity-0 group-hover:opacity-100
                   hover:bg-red-50 hover:border-red-200 transition-all duration-150"
            title="Elimina nota"
            @click.prevent="openDeleteDialog(note.id)"
          >
            <svg class="w-3.5 h-3.5 text-gray-400 hover:text-red-500" fill="none"
                 stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5
                   4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
            </svg>
          </button>
        </div>
      </div>
    </main>

    <!-- FAB (Floating Action Button): bottone verde fisso in basso a destra -->
    <NuxtLink
      to="/notes/new"
      class="fixed bottom-6 right-6 w-14 h-14 bg-sage-accent rounded-full shadow-lg
             flex items-center justify-center hover:bg-sage-accent/90 hover:shadow-xl
             active:scale-95 transition-all duration-150"
    >
      <svg class="w-7 h-7 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M12 4v16m8-8H4"/>
      </svg>
    </NuxtLink>

    <!-- ConfirmDialog: il componente figlio riceve props e ascolta eventi emit -->
    <ConfirmDialog
      :open="confirmOpen"
      title="Elimina nota"
      message="Sei sicuro? L'operazione non può essere annullata."
      @confirm="handleDelete"
      @cancel="confirmOpen = false"
    />
  </div>
</template>
