<script setup lang="ts">
// definePageMeta è una macro di Nuxt: istruisce il router a eseguire
// il middleware 'guest' prima di mostrare questa pagina.
definePageMeta({ middleware: 'guest' })

const authStore = useAuthStore()

// ref() crea variabili reattive locali al componente.
// Quando cambiano, Vue re-renderizza automaticamente le parti di template che le usano.
const activeTab = ref<'login' | 'register'>('login')
const email = ref('')
const password = ref('')
const showPassword = ref(false)

// Computed che legge dallo store — si aggiorna automaticamente quando lo store cambia
const isLoading = computed(() => authStore.isLoading)
const error = computed(() => authStore.error)

function switchTab(tab: 'login' | 'register') {
  activeTab.value = tab
  authStore.clearError()
}

async function handleSubmit() {
  if (activeTab.value === 'login') {
    await authStore.login(email.value, password.value)
  } else {
    await authStore.register(email.value, password.value)
  }
}
</script>

<template>
  <!-- Sfondo crema, centrato verticalmente e orizzontalmente -->
  <div class="min-h-screen bg-warm-bg flex items-center justify-center p-4">

    <!-- Card principale: max 448px, angoli arrotondati, ombra -->
    <div class="w-full max-w-md bg-white rounded-2xl shadow-lg overflow-hidden">

      <!-- Header della card — sfondo navy -->
      <div class="bg-slate-primary px-8 py-6 text-center">
        <div class="w-12 h-12 bg-sage-accent rounded-full flex items-center justify-center mx-auto mb-3">
          <span class="text-white text-xl font-bold">N</span>
        </div>
        <h1 class="text-white text-2xl font-bold tracking-tight">NoteSync</h1>
        <p class="text-white/60 text-sm mt-1">Le tue note, sempre sincronizzate</p>
      </div>

      <!-- Body della card -->
      <div class="px-8 py-6">

        <!-- Segmented control: due tab Login / Registrati -->
        <!-- Questo pattern è il "tab switching" — activeTab decide quale form mostrare -->
        <div class="flex bg-gray-100 rounded-xl p-1 mb-6">
          <button
            v-for="tab in (['login', 'register'] as const)"
            :key="tab"
            class="flex-1 py-2 rounded-lg text-sm font-medium transition-all duration-200"
            :class="activeTab === tab
              ? 'bg-sage-accent text-white shadow-sm'
              : 'text-gray-600 hover:text-gray-800'"
            @click="switchTab(tab)"
          >
            {{ tab === 'login' ? 'Accedi' : 'Registrati' }}
          </button>
        </div>

        <!-- Form — @submit.prevent intercetta il submit HTML e chiama handleSubmit -->
        <form @submit.prevent="handleSubmit" class="space-y-4">

          <!-- Campo Email -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              v-model="email"
              type="email"
              required
              autocomplete="email"
              placeholder="nome@esempio.com"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl text-sm
                     focus:outline-none focus:ring-2 focus:ring-slate-primary/30 focus:border-slate-primary
                     transition-colors"
            />
          </div>

          <!-- Campo Password con toggle visibilità -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <div class="relative">
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                required
                autocomplete="current-password"
                placeholder="••••••••"
                class="w-full px-4 py-3 pr-12 border border-gray-200 rounded-xl text-sm
                       focus:outline-none focus:ring-2 focus:ring-slate-primary/30 focus:border-slate-primary
                       transition-colors"
              />
              <!-- Bottone mostra/nascondi password -->
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                @click="showPassword = !showPassword"
              >
                <!-- Icona occhio SVG inline — cambia in base a showPassword -->
                <svg v-if="!showPassword" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7
                       -1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/>
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7
                       a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878
                       l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59
                       m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7
                       a10.025 10.025 0 01-4.132 5.411m0 0L21 21"/>
                </svg>
              </button>
            </div>
          </div>

          <!-- Messaggio di errore (visibile solo se error non è null) -->
          <!-- v-if rimuove/aggiunge l'elemento dal DOM in base alla condizione -->
          <p v-if="error" class="text-red-500 text-sm text-center">{{ error }}</p>

          <!-- Bottone submit -->
          <!-- :disabled è un binding dinamico — disabilita il bottone durante il caricamento -->
          <button
            type="submit"
            :disabled="isLoading"
            class="w-full py-3 bg-slate-primary text-white rounded-xl font-medium
                   hover:bg-slate-primary/90 active:scale-[0.98]
                   disabled:opacity-60 disabled:cursor-not-allowed
                   transition-all duration-200"
          >
            <!-- v-if / v-else per mostrare spinner o testo -->
            <span v-if="isLoading" class="flex items-center justify-center gap-2">
              <svg class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              Caricamento...
            </span>
            <span v-else>
              {{ activeTab === 'login' ? 'Accedi' : 'Crea account' }}
            </span>
          </button>
        </form>
      </div>
    </div>
  </div>
</template>
