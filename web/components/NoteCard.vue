<script setup lang="ts">
import type { Note } from '~/types/api'

// defineProps dichiara i dati che questo componente riceve dal genitore.
// TypeScript controlla che il genitore passi sempre una Note valida.
const props = defineProps<{ note: Note }>()

// Funzione pura: converte un timestamp ms in stringa leggibile.
// Nessuna dipendenza reattiva — viene eseguita solo al render.
function formatDate(ts: number): string {
  const date = new Date(ts)
  const now = new Date()
  const diffDays = Math.floor((now.getTime() - date.getTime()) / 86_400_000)

  if (diffDays === 0) return 'Oggi'
  if (diffDays === 1) return 'Ieri'
  return new Intl.DateTimeFormat('it-IT', { day: 'numeric', month: 'short' }).format(date)
}
</script>

<template>
  <!-- NuxtLink è il componente di navigazione di Nuxt — genera un <a> ottimizzato.
       Al click carica la pagina senza ricaricare l'intera app (SPA navigation). -->
  <NuxtLink :to="`/notes/${note.id}`" class="block">
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-4
                hover:shadow-md hover:border-gray-200 active:scale-[0.99]
                transition-all duration-150 cursor-pointer">

      <!-- Riga superiore: titolo + data -->
      <div class="flex items-start justify-between gap-2 mb-1">
        <h3 class="font-semibold text-slate-primary truncate flex-1 leading-snug">
          {{ note.title || 'Senza titolo' }}
        </h3>
        <span class="text-xs text-gray-400 shrink-0 mt-0.5">
          {{ formatDate(note.updatedAt) }}
        </span>
      </div>

      <!-- Preview del contenuto: line-clamp-2 taglia il testo a 2 righe con "..." -->
      <p class="text-gray-500 text-sm line-clamp-2 leading-relaxed">
        {{ note.content || 'Nessun contenuto' }}
      </p>
    </div>
  </NuxtLink>
</template>
