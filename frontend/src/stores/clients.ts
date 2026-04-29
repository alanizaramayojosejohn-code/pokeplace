import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'
import { extractErrorMessage } from '../api/errors'
import type { Client } from '../types/api'

export const useClientsStore = defineStore('clients', () => {
  const items = ref<Client[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Client[]>('/clients')
      items.value = data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar los clientes')
    } finally {
      loading.value = false
    }
  }

  async function create(payload: { ci: number; name: string }) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.post<Client>('/clients', payload)
      items.value.push(data)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo crear el cliente')
      return false
    } finally {
      loading.value = false
    }
  }

  async function update(id: number, payload: { ci: number; name: string }) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.put<Client>(`/clients/${id}`, payload)
      const idx = items.value.findIndex((c) => c.id === id)
      if (idx !== -1) items.value[idx] = data
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo actualizar el cliente')
      return false
    } finally {
      loading.value = false
    }
  }

  async function remove(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/clients/${id}`)
      items.value = items.value.filter((c) => c.id !== id)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo eliminar el cliente')
      return false
    } finally {
      loading.value = false
    }
  }

  return { items, loading, error, fetchAll, create, update, remove }
})
