import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'

interface User {
  id: number
  name: string
  lastname: string
  email: string
  phone: string
  ci: number
  role: string
}

interface CreateUserPayload {
  name: string
  lastname: string
  email: string
  password: string
  phone: string
  ci: number
  role: string
}

export const useUsersStore = defineStore('users', () => {
  const users = ref<User[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchUsers() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<User[]>('/users')
      users.value = data
    } catch (e) {
      error.value = 'Failed to load users'
    } finally {
      loading.value = false
    }
  }

  // Crea un nuevo usuario
  async function createUser(payload: CreateUserPayload) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.post<User>('/users', payload)
      users.value.push(data) // agrega el nuevo usuario a la lista local
    } catch (e) {
      error.value = 'Failed to create user'
    } finally {
      loading.value = false
    }
  }

  async function updateUser(id: number, payload: Partial<CreateUserPayload>) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.put<User>(`/users/${id}`, payload)
      // Reemplaza el usuario actualizado en la lista local
      const index = users.value.findIndex((u) => u.id === id)
      if (index !== -1) users.value[index] = data
    } catch (e) {
      error.value = 'Failed to update user'
    } finally {
      loading.value = false
    }
  }

  // Elimina un usuario
  async function deleteUser(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/users/${id}`)
      users.value = users.value.filter((u) => u.id !== id) // lo quita de la lista local
    } catch (e) {
      error.value = 'Failed to delete user'
    } finally {
      loading.value = false
    }
  }

  return { users, loading, error, fetchUsers, createUser, updateUser, deleteUser }
})
