export interface Category {
  id: number
  name: string
}

export type ProductType = 'EDIBLE' | 'INEDIBLE'

export interface Product {
  id: number
  name: string
  price: number
  type: ProductType
  categoryId: number
  categoryName: string
  pokeName?: string
  stock?: number
  minStock?: number
}

export interface Client {
  id: number
  ci: number
  name: string
}

export type OrderStatus = 'PENDING' | 'READY_FOR_PICKUP' | 'DELIVERED' | 'CANCELLED'

export interface OrderDetail {
  id?: number
  productId: number
  productName?: string
  quantity: number
  subtotal?: number
}

export interface Order {
  id: number
  tableNumber: number
  dateTime: string
  paymentMethod: string
  status: OrderStatus
  total: number
  notes?: string
  clientId?: number
  clientName?: string
  userId: number
  userName: string
  details: OrderDetail[]
}

export interface CreateOrderPayload {
  tableNumber: number
  paymentMethod: string
  notes?: string
  clientId?: number | null
  userId: number
  details: { productId: number; quantity: number }[]
}

export interface ApiError {
  timestamp?: string
  status?: number
  message?: string
  errors?: Record<string, string>
}
