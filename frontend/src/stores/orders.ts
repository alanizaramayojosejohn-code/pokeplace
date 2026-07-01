import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'
import { extractErrorMessage } from '../api/errors'
import type { CreateOrderPayload, Order, OrderStatus } from '../types/api'

export interface PagedResult<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export const useOrdersStore = defineStore('orders', () => {
  const items = ref<Order[]>([])
  const page = ref(0)
  const totalPages = ref(0)
  const totalElements = ref(0)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const pendingCount = ref(0)
  const readyCount = ref(0)
  const deliveredCount = ref(0)
  const cancelledCount = ref(0)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Order[]>('/orders/all')
      items.value = data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar las órdenes')
    } finally {
      loading.value = false
    }
  }

  async function fetchPage(p = 0, size = 20, status?: OrderStatus) {
    loading.value = true
    error.value = null
    try {
      const params: Record<string, string | number> = { page: p, size }
      if (status) params.status = status
      const { data } = await api.get<PagedResult<Order>>('/orders', { params })
      items.value = data.content
      page.value = data.page
      totalPages.value = data.totalPages
      totalElements.value = data.totalElements
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar las órdenes')
    } finally {
      loading.value = false
    }
  }

  async function fetchCounts() {
    try {
      const { data } = await api.get<{
        all: number; pending: number; readyForPickup: number
        delivered: number; cancelled: number
      }>('/orders/counts')
      pendingCount.value = data.pending
      readyCount.value = data.readyForPickup
      deliveredCount.value = data.delivered
      cancelledCount.value = data.cancelled
    } catch {
      // ignore
    }
  }

  async function create(payload: CreateOrderPayload) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.post<Order>('/orders', payload)
      items.value.unshift(data)
      return data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo crear la orden')
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateStatus(id: number, status: OrderStatus) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.patch<Order>(`/orders/${id}/status`, null, {
        params: { status },
      })
      const idx = items.value.findIndex((o) => o.id === id)
      if (idx !== -1) items.value[idx] = data
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo actualizar el estado')
      return false
    } finally {
      loading.value = false
    }
  }

  async function remove(id: number) {
    loading.value = true
    error.value = null
    try {
      await api.delete(`/orders/${id}`)
      items.value = items.value.filter((o) => o.id !== id)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo eliminar la orden')
      return false
    } finally {
      loading.value = false
    }
  }

  return {
    items, page, totalPages, totalElements, loading, error,
    pendingCount, readyCount, deliveredCount, cancelledCount,
    fetchPage, fetchAll, fetchCounts, create, updateStatus, remove,
  }
})
