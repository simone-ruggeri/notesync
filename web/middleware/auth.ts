// Protegge tutte le pagine che dichiarano: definePageMeta({ middleware: 'auth' })
// Se l'utente non è autenticato, lo manda al login.
export default defineNuxtRouteMiddleware(() => {
  const authStore = useAuthStore()
  authStore.hydrate()

  if (!authStore.isAuthenticated) {
    return navigateTo('/login')
  }
})
