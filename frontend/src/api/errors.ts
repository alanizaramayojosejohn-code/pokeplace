import axios from 'axios'
import type { ApiError } from '../types/api'

export function extractErrorMessage(error: unknown, fallback = 'Error inesperado'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiError | undefined
    if (data?.errors) {
      return Object.values(data.errors).join(' · ')
    }
    if (data?.message) return data.message
    return error.message || fallback
  }
  return fallback
}
