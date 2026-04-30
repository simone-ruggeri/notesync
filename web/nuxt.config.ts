export default defineNuxtConfig({
  ssr: false,
  compatibilityDate: '2026-04-30',
  devtools: { enabled: true },
  modules: ['@pinia/nuxt', '@nuxtjs/tailwindcss'],
  runtimeConfig: {
    public: {
      apiBase: 'http://localhost:8080'
    }
  },
  typescript: {
    strict: true
  },
  css: ['~/assets/css/main.css'],
  app: {
    head: {
      title: 'NoteSync',
      meta: [
        { name: 'description', content: 'Your personal notes, always in sync' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' }
      ]
    }
  }
})
