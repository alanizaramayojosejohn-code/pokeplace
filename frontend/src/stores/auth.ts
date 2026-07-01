import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/axios'

interface AuthUser {
  id: number
  name: string
  email: string
  role: string
}

interface AuthResponse {
  token: string
  userId: number
  name: string
  email: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<AuthUser | null>(JSON.parse(localStorage.getItem('user') ?? 'null'))

  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role ?? null)
  const userId = computed(() => user.value?.id ?? null)

  function persist(data: AuthResponse) {
    token.value = data.token
    user.value = { id: data.userId, name: data.name, email: data.email, role: data.role }

    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))

    api.defaults.headers.common['Authorization'] = `Bearer ${data.token}`
  }

  async function login(email: string, password: string) {
    const { data } = await api.post<AuthResponse>('/auth/login', { email, password })
    persist(data)
  }

  async function googleLogin(googleToken: string) {
    const { data } = await api.post<AuthResponse>('/auth/google', { token: googleToken })
    persist(data)
  }

  async function logout() {
    if (user.value?.email) {
      try { await api.post(`/auth/logout?email=${encodeURIComponent(user.value.email)}`) } catch { /* best effort */ }
    }
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    delete api.defaults.headers.common['Authorization']
  }

  function initialize() {
    if (token.value) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token.value}`
    }
  }

  return {
    token,
    user,
    isAuthenticated,
    userRole,
    userId,
    login,
    googleLogin,
    logout,
    initialize,
  }
})
