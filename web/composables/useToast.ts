export type ToastType = 'success' | 'error' | 'info'

export interface Toast {
  id: number
  message: string
  type: ToastType
}

// useState di Nuxt crea uno stato condiviso tra tutti i componenti con lo stesso key.
// È come ref() ma globale — funziona senza Pinia per stati semplici come questo.
export function useToast() {
  const toasts = useState<Toast[]>('toasts', () => [])
  let nextId = 0

  function show(message: string, type: ToastType = 'info', duration = 3000) {
    const id = nextId++
    toasts.value.push({ id, message, type })
    // setTimeout esegue la callback dopo `duration` ms — rimuove il toast dalla lista
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, duration)
  }

  const success = (msg: string) => show(msg, 'success')
  const error = (msg: string) => show(msg, 'error')
  const info = (msg: string) => show(msg, 'info')

  return { toasts, success, error, info }
}
