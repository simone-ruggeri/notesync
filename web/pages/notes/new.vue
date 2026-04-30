<script setup lang="ts">
definePageMeta({ middleware: 'auth' })

const notesStore = useNotesStore()

const title = ref('')
const content = ref('')
const isSaving = ref(false)

// computed: il bottone save è disabilitato se il titolo è vuoto o si sta salvando
const canSave = computed(() => title.value.trim().length > 0 && !isSaving.value)

async function save() {
  if (!canSave.value) return
  isSaving.value = true
  const note = await notesStore.createNote(title.value.trim(), content.value)
  isSaving.value = false
  if (note) {
    await navigateTo('/notes')
  }
}
</script>

<template>
  <div class="min-h-screen bg-white flex flex-col">

    <!-- Header editor -->
    <header class="sticky top-0 z-40 bg-slate-primary shadow-sm">
      <div class="max-w-3xl mx-auto px-4 h-14 flex items-center justify-between">

        <!-- Bottone indietro -->
        <NuxtLink to="/notes"
          class="text-white/70 hover:text-white transition-colors p-1 -ml-1">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M15 19l-7-7 7-7"/>
          </svg>
        </NuxtLink>

        <span class="text-white font-medium">Nuova nota</span>

        <!-- Bottone salva — disabilitato se titolo vuoto -->
        <button
          :disabled="!canSave"
          class="text-white/70 hover:text-white disabled:text-white/30
                 transition-colors p-1 -mr-1"
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

    <!-- Editor: campo titolo + separatore + textarea contenuto -->
    <!-- flex-1 fa sì che l'area di testo occupi tutto lo spazio verticale rimasto -->
    <div class="flex-1 max-w-3xl mx-auto w-full px-6 py-5 flex flex-col gap-0">
      <input
        v-model="title"
        type="text"
        placeholder="Titolo"
        class="w-full text-xl font-semibold text-slate-primary placeholder-gray-300
               border-none outline-none bg-transparent"
      />

      <div class="h-px bg-gray-100 my-4" />

      <!-- textarea si espande con il contenuto grazie a min-h + resize-none -->
      <textarea
        v-model="content"
        placeholder="Scrivi qui..."
        class="flex-1 w-full text-gray-700 placeholder-gray-300 text-[15px] leading-relaxed
               border-none outline-none bg-transparent resize-none min-h-[60vh]"
      />
    </div>
  </div>
</template>
