<template>
  <!-- NuxtPage con transizione fade tra le pagine.
       mode="out-in" aspetta che la pagina uscente finisca prima di far entrare la nuova. -->
  <NuxtPage :transition="{ name: 'fade' }" />

  <!-- ToastContainer vive qui, a livello root, così è sempre visibile
       indipendentemente da quale pagina è attiva. -->
  <ToastContainer />
</template>

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
