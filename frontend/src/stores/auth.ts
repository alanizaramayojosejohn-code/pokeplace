import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/axios'

interface User {
  name: string
  email: string
  role: string
}

interface AuthResponse {
  token: string
  name: string
  email: string
  role: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const user = ref<User | null>(JSON.parse(localStorage.getItem('user') ?? 'null'))

  const isAuthenticated = computed(() => !!token.value)
  const userRole = computed(() => user.value?.role ?? null)

  async function login(email: string, password: string) {
    const { data } = await api.post<AuthResponse>('/auth/login', { email, password })

    token.value = data.token
    user.value = { name: data.name, email: data.email, role: data.role }

    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))

    api.defaults.headers.common['Authorization'] = `Bearer ${data.token}`
  }

  async function googleLogin(googleToken: string) {
    const { data } = await api.post<AuthResponse>('/auth/google', { token: googleToken })

    token.value = data.token
    user.value = { name: data.name, email: data.email, role: data.role }

    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user.value))

    api.defaults.headers.common['Authorization'] = `Bearer ${data.token}`
  }

  function logout() {
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

  return { token, user, isAuthenticated, userRole, login, googleLogin, logout, initialize }
})
