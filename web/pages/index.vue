<script setup lang="ts">
const authStore = useAuthStore()
authStore.hydrate()
await navigateTo(authStore.isAuthenticated ? '/notes' : '/login', { replace: true })
</script>

<template>
  <div></div>
</template>
