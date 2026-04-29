import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '../api/axios'
import { extractErrorMessage } from '../api/errors'
import type { CreateOrderPayload, Order, OrderStatus } from '../types/api'

export const useOrdersStore = defineStore('orders', () => {
  const items = ref<Order[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get<Order[]>('/orders')
      items.value = data
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudieron cargar las órdenes')
    } finally {
      loading.value = false
    }
  }

  async function create(payload: CreateOrderPayload) {
    loading.value = true
    error.value = null
    try {
      const { data } = await api.post<Order>('/orders', payload)
      items.value.unshift(data)
      return true
    } catch (e) {
      error.value = extractErrorMessage(e, 'No se pudo crear la orden')
      return false
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

  return { items, loading, error, fetchAll, create, updateStatus, remove }
})
