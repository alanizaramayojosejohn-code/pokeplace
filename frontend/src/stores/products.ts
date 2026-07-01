import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'
import { extractErrorMessage } from '../api/errors'
import type { Product } from '../types/api'

export interface EdiblePayload {
  name: string
  price: number
  categoryId: number
  pokeName: string
}

export interface InediblePayload {
  name: string
  price: number
  categoryId: number
  stock: number
  minStock: number
}

export const useProductsStore = defineStore('products', () => {
  const items = ref<Product[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Product[]>('/products')
      items.value = data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar los productos')
    } finally {
      loading.value = false
    }
  }

  async function createEdible(payload: EdiblePayload) {
    return save(() => api.post<Product>('/products/edible', payload))
  }

  async function createInedible(payload: InediblePayload) {
    return save(() => api.post<Product>('/products/inedible', payload))
  }

  async function updateEdible(id: number, payload: EdiblePayload) {
    return replace(id, () => api.put<Product>(`/products/edible/${id}`, payload))
  }

  async function updateInedible(id: number, payload: InediblePayload) {
    return replace(id, () => api.put<Product>(`/products/inedible/${id}`, payload))
  }

  async function remove(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/products/${id}`)
      items.value = items.value.filter((p) => p.id !== id)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo eliminar el producto')
      return false
    } finally {
      loading.value = false
    }
  }

  async function save(request: () => Promise<{ data: Product }>) {
    loading.value = true
    error.value = null
    try {
      const { data } = await request()
      items.value.push(data)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo guardar el producto')
      return false
    } finally {
      loading.value = false
    }
  }

  async function replace(id: number, request: () => Promise<{ data: Product }>) {
    loading.value = true
    error.value = null
    try {
      const { data } = await request()
      const idx = items.value.findIndex((p) => p.id === id)
      if (idx !== -1) items.value[idx] = data
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo actualizar el producto')
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    items,
    loading,
    error,
    fetchAll,
    createEdible,
    createInedible,
    updateEdible,
    updateInedible,
    remove,
  }
})
