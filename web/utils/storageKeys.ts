// Chiavi localStorage centralizzate per evitare stringhe magiche sparse nel codice.
// Usare sempre queste costanti invece di scrivere le stringhe direttamente.
export const STORAGE_KEYS = {
  token: 'notesync_token',
  userId: 'notesync_userId',
  email: 'notesync_email',
} as const
