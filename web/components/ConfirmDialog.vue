<script setup lang="ts">
defineProps<{
  open: boolean
  title: string
  message: string
}>()

// defineEmits dichiara gli eventi che questo componente può lanciare verso il genitore.
// Il genitore ascolta con @confirm="..." e @cancel="..."
const emit = defineEmits<{
  confirm: []
  cancel: []
}>()
</script>

<template>
  <!-- Teleport sposta il modal fuori dall'albero DOM normale e lo mette in <body>.
       Questo evita problemi di z-index o overflow: hidden nei componenti genitori. -->
  <Teleport to="body">
    <!-- v-if rimuove completamente il modal dal DOM quando è chiuso (non solo nascosto) -->
    <div v-if="open" class="fixed inset-0 z-50 flex items-center justify-center p-4">

      <!-- Overlay scuro: click fuori chiude il dialogo -->
      <div
        class="absolute inset-0 bg-black/50 backdrop-blur-sm"
        @click="emit('cancel')"
      />

      <!-- Card del dialogo — z-10 per stare sopra l'overlay -->
      <div class="relative z-10 bg-white rounded-2xl shadow-xl w-full max-w-sm p-6">
        <h3 class="text-lg font-semibold text-slate-primary mb-2">{{ title }}</h3>
        <p class="text-gray-600 text-sm mb-6">{{ message }}</p>

        <div class="flex gap-3">
          <button
            class="flex-1 py-2.5 border border-gray-200 rounded-xl text-gray-700
                   text-sm font-medium hover:bg-gray-50 transition-colors"
            @click="emit('cancel')"
          >
            Annulla
          </button>
          <button
            class="flex-1 py-2.5 bg-red-500 text-white rounded-xl
                   text-sm font-medium hover:bg-red-600 transition-colors"
            @click="emit('confirm')"
          >
            Elimina
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
