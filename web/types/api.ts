export interface Note {
  id: string
  userId: string
  title: string
  content: string
  createdAt: number
  updatedAt: number
}

export interface AuthRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  userId: string
}

export interface NoteRequest {
  title: string
  content: string
}

export interface ApiError {
  statusCode: number
  message: string
}
