<script setup lang="ts">
const { toasts } = useToast()
</script>

<template>
  <!-- Teleport: il container dei toast vive in <body>, sopra tutto il resto -->
  <Teleport to="body">
    <!-- fixed + bottom-center: i toast appaiono in basso al centro -->
    <div class="fixed bottom-6 left-1/2 -translate-x-1/2 z-[100]
                flex flex-col items-center gap-2 pointer-events-none">

      <!-- TransitionGroup anima automaticamente l'entrata e l'uscita degli elementi.
           'name' collega il tag alle classi CSS sotto (toast-enter-*, toast-leave-*).
           tag="div" wrappa il gruppo in un div (obbligatorio per TransitionGroup). -->
      <TransitionGroup name="toast" tag="div" class="flex flex-col items-center gap-2">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="px-4 py-2.5 rounded-full text-sm font-medium shadow-lg
                 pointer-events-auto flex items-center gap-2 whitespace-nowrap"
          :class="{
            'bg-slate-primary text-white':  toast.type === 'info',
            'bg-green-600 text-white':      toast.type === 'success',
            'bg-red-500 text-white':        toast.type === 'error',
          }"
        >
          <!-- Icona diversa per ogni tipo di toast -->
          <svg v-if="toast.type === 'success'" class="w-4 h-4 shrink-0" fill="none"
               stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5"
              d="M5 13l4 4L19 7"/>
          </svg>
          <svg v-else-if="toast.type === 'error'" class="w-4 h-4 shrink-0" fill="none"
               stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 9v2m0 4h.01M12 3a9 9 0 100 18A9 9 0 0012 3z"/>
          </svg>
          {{ toast.message }}
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
/* Animazione entrata: il toast scivola su dal basso */
.toast-enter-active {
  transition: all 0.25s ease-out;
}
/* Animazione uscita: il toast sfuma e sale */
.toast-leave-active {
  transition: all 0.2s ease-in;
}
.toast-enter-from {
  opacity: 0;
  transform: translateY(12px) scale(0.95);
}
.toast-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}
/* Necessario per far animare lo spostamento degli altri toast quando uno esce */
.toast-move {
  transition: transform 0.2s ease;
}
</style>
