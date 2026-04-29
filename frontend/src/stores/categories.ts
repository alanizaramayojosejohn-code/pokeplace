import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'
import { extractErrorMessage } from '../api/errors'
import type { Category } from '../types/api'

export const useCategoriesStore = defineStore('categories', () => {
  const items = ref<Category[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Category[]>('/categories')
      items.value = data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar las categorías')
    } finally {
      loading.value = false
    }
  }

  async function create(payload: { name: string }) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.post<Category>('/categories', payload)
      items.value.push(data)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo crear la categoría')
      return false
    } finally {
      loading.value = false
    }
  }

  async function update(id: number, payload: { name: string }) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.put<Category>(`/categories/${id}`, payload)
      const idx = items.value.findIndex((c) => c.id === id)
      if (idx !== -1) items.value[idx] = data
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo actualizar la categoría')
      return false
    } finally {
      loading.value = false
    }
  }

  async function remove(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/categories/${id}`)
      items.value = items.value.filter((c) => c.id !== id)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo eliminar la categoría')
      return false
    } finally {
      loading.value = false
    }
  }

  return { items, loading, error, fetchAll, create, update, remove }
})
