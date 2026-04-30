// Protegge le pagine pubbliche (es. /login) da utenti già autenticati.
// Se sei già loggato e provi ad andare su /login, ti manda direttamente a /notes.
export default defineNuxtRouteMiddleware(() => {
  const authStore = useAuthStore()
  authStore.hydrate()

  if (authStore.isAuthenticated) {
    return navigateTo('/notes')
  }
})
