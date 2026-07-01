import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'
import { extractErrorMessage } from '../api/errors'
import type { Client } from '../types/api'
import type { PagedResult } from './orders'

export const useClientsStore = defineStore('clients', () => {
  const items = ref<Client[]>([])
  const allItems = ref<Client[]>([])
  const page = ref(0)
  const totalPages = ref(0)
  const totalElements = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchPage(p = 0, size = 20) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<PagedResult<Client>>('/clients', {
        params: { page: p, size },
      })
      items.value = data.content
      page.value = data.page
      totalPages.value = data.totalPages
      totalElements.value = data.totalElements
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar los clientes')
    } finally {
      loading.value = false
    }
  }

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Client[]>('/clients/all')
      allItems.value = data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar los clientes')
    } finally {
      loading.value = false
    }
  }

  async function create(payload: { nit: string; name: string; ci: string; phone: string; email?: string }) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.post<Client>('/clients', payload)
      items.value.push(data)
      allItems.value.push(data)
      return data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo crear el cliente')
      return null
    } finally {
      loading.value = false
    }
  }

  async function update(id: number, payload: { nit: string; name: string; ci: string; phone: string; email?: string }) {
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

  return {
    items, allItems, page, totalPages, totalElements, loading, error,
    fetchPage, fetchAll, create, update, remove,
  }
})
