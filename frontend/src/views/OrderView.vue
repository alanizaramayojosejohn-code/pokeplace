<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useOrdersStore } from '../stores/orders'
import { useProductsStore } from '../stores/products'
import { useCategoriesStore } from '../stores/categories'
import { useClientsStore } from '../stores/clients'
import { useAuthStore } from '../stores/auth'
import type { Order, OrderStatus, Product } from '../types/api'
import PageHeader from '../components/ui/PageHeader.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseSelect from '../components/ui/BaseSelect.vue'
import BaseTextarea from '../components/ui/BaseTextarea.vue'
import BaseDrawer from '../components/ui/BaseDrawer.vue'
import BaseBadge from '../components/ui/BaseBadge.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import AlertBanner from '../components/ui/AlertBanner.vue'
import SearchBar from '../components/ui/SearchBar.vue'
import PaginationBar from '../components/ui/PaginationBar.vue'

const orders = useOrdersStore()
const products = useProductsStore()
const categories = useCategoriesStore()
const clients = useClientsStore()
const auth = useAuthStore()

type Filter = 'ALL' | OrderStatus
const filter = ref<Filter>('ALL')

const showPOS = ref(false)
const showDetail = ref(false)
const showReceipt = ref(false)
const createdOrder = ref<Order | null>(null)
const selectedOrder = ref<Order | null>(null)

interface CartLine {
  productId: number
  name: string
  price: number
  quantity: number
  type: string
  stock?: number
}
const cart = ref<CartLine[]>([])
const catalogQuery = ref('')
const catalogCategoryId = ref<number | null>(null)
const catalogTypeFilter = ref<'EDIBLE' | 'INEDIBLE'>('EDIBLE')

const form = ref<{
  tableNumber: number | 'TAKEAWAY'
  paymentMethod: string
  clientId: number | null
  notes: string
  amountPaid: number
}>({
  tableNumber: 1,
  paymentMethod: 'EFECTIVO',
  clientId: null,
  notes: '',
  amountPaid: 0,
})

const changeAmount = computed(() => {
  if (form.value.paymentMethod !== 'EFECTIVO') return 0
  return Math.max(0, form.value.amountPaid - total.value)
})

const formTouched = ref<Set<string>>(new Set())

function markFormTouched(field: string) {
  formTouched.value.add(field)
}

const formErrors = computed(() => ({
  amountPaid: form.value.paymentMethod === 'EFECTIVO' && form.value.amountPaid < total.value
    ? 'Debe ser mayor o igual al total' : '',
}))

onMounted(async () => {
  await Promise.all([
    orders.fetchPage(0, 20, filter.value === 'ALL' ? undefined : filter.value),
    orders.fetchCounts(),
    products.fetchAll(),
    categories.fetchAll(),
    clients.fetchAll(),
  ])
})

const statusConfig: Record<OrderStatus, { label: string; variant: 'warning' | 'success' | 'danger' | 'info' }> = {
  PENDING: { label: 'Pendiente', variant: 'warning' },
  READY_FOR_PICKUP: { label: 'Lista para recoger', variant: 'info' },
  DELIVERED: { label: 'Entregada', variant: 'success' },
  CANCELLED: { label: 'Cancelada', variant: 'danger' },
}

const paymentMethodIcons: Record<string, string> = {
  EFECTIVO: '<svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="1.5" y="4" width="15" height="10" rx="1.5"/><circle cx="9" cy="9" r="2.5"/></svg>',
  TARJETA: '<svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="3.5" width="16" height="11" rx="1.5"/><path d="M1 7.5h16"/></svg>',
  QR: '<svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 2.5h5v5h-5zM10.5 2.5h5v5h-5zM2.5 10.5h5v5h-5zM10.5 10.5h2v2h-2zM12.5 10.5h2v2h-2zM10.5 12.5h2v2h-2zM12.5 12.5h2v2h-2zM10.5 14.5h2v1.5h-2zM14.5 10.5h1.5v2H16v-2zM14.5 12.5H16v1.5h-1.5zM14.5 14.5H16V16h-1.5z"/><path d="M4 4h1.5v1.5H4zM12.5 4H14v1.5h-1.5zM4 12.5h1.5V14H4z"/></svg>',
}

const paymentMethods = [
  { value: 'EFECTIVO', label: 'Efectivo' },
  { value: 'TARJETA', label: 'Tarjeta' },
  { value: 'QR', label: 'QR' },
]

const filteredCatalog = computed(() => {
  return products.items.filter((p) => {
    if (p.type !== catalogTypeFilter.value) return false
    if (catalogCategoryId.value && p.categoryId !== catalogCategoryId.value) return false
    const q = catalogQuery.value.toLowerCase()
    if (!q) return true
    return p.name.toLowerCase().includes(q) || p.pokeName?.toLowerCase().includes(q)
  })
})

const categoryFilterOptions = computed(() => [
  { value: 0, label: 'Todas las categorías' },
  ...categories.items.map((c) => ({ value: c.id, label: c.name })),
])

const clientOptions = computed(() => [
  { value: 0, label: 'Sin cliente' },
  ...clients.allItems.map((c) => ({ value: c.id, label: `${c.name} · ${c.nit}` })),
])

const total = computed(() => cart.value.reduce((sum, l) => sum + l.price * l.quantity, 0))

watch(
  () => form.value.paymentMethod,
  () => {
    form.value.amountPaid = total.value
  },
)

watch(total, (newTotal) => {
  form.value.amountPaid = newTotal
})

const counts = computed(() => ({
  ALL: orders.pendingCount + orders.readyCount + orders.deliveredCount + orders.cancelledCount,
  PENDING: orders.pendingCount,
  READY_FOR_PICKUP: orders.readyCount,
  DELIVERED: orders.deliveredCount,
  CANCELLED: orders.cancelledCount,
}))

function goToPage(p: number) {
  orders.fetchPage(p, 20, filter.value === 'ALL' ? undefined : filter.value)
}

const showQuickClient = ref(false)
const quickClientForm = ref({ name: '', nit: '', ci: '', phone: '', email: '' })
const quickClientLoading = ref(false)
const quickClientTouched = ref<Set<string>>(new Set())

function markQuickClientTouched(field: string) {
  quickClientTouched.value.add(field)
}

const quickClientNamePattern = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/
const quickClientPhonePattern = /^[0-9]{8,15}$/
const quickClientEmailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const quickClientDigitsOnly = /^[0-9]+$/
const quickClientNitPattern = /^[0-9]{7,15}$/
const quickClientCiPattern = /^[0-9]{7,10}$/

const quickClientErrors = computed(() => ({
  name: !quickClientForm.value.name ? 'El nombre es obligatorio'
    : !quickClientNamePattern.test(quickClientForm.value.name) ? 'Solo letras y espacios' : '',
  nit: !quickClientForm.value.nit ? 'El NIT es obligatorio'
    : !quickClientDigitsOnly.test(quickClientForm.value.nit) ? 'Solo números'
    : !quickClientNitPattern.test(quickClientForm.value.nit) ? 'Debe tener 7-15 dígitos' : '',
  ci: !quickClientForm.value.ci ? 'El CI es obligatorio'
    : !quickClientDigitsOnly.test(quickClientForm.value.ci) ? 'Solo números'
    : !quickClientCiPattern.test(quickClientForm.value.ci) ? 'Debe tener 7-10 dígitos' : '',
  phone: !quickClientForm.value.phone ? 'El celular es obligatorio'
    : !quickClientPhonePattern.test(quickClientForm.value.phone) ? 'Debe tener 8-15 dígitos' : '',
  email: quickClientForm.value.email && !quickClientEmailPattern.test(quickClientForm.value.email) ? 'Email inválido' : '',
}))

function toggleQuickClient() {
  showQuickClient.value = !showQuickClient.value
  if (showQuickClient.value) {
    quickClientForm.value = { name: '', nit: '', ci: '', phone: '', email: '' }
    quickClientTouched.value = new Set()
  }
}

async function submitQuickClient() {
  ;['name', 'nit', 'ci', 'phone', 'email'].forEach((f) => quickClientTouched.value.add(f))
  if (!Object.values(quickClientErrors.value).every((e) => !e)) return
  const f = quickClientForm.value
  quickClientLoading.value = true
  try {
    const created = await clients.create({ name: f.name, nit: f.nit, ci: f.ci, phone: f.phone, email: f.email || undefined })
    if (created) {
      form.value.clientId = created.id
      showQuickClient.value = false
    }
  } finally {
    quickClientLoading.value = false
  }
}

watch(filter, (f) => {
  orders.fetchPage(0, 20, f === 'ALL' ? undefined : f)
})

function openPOS() {
  cart.value = []
  form.value = { tableNumber: 1, paymentMethod: 'EFECTIVO', clientId: null, notes: '', amountPaid: total.value }
  catalogQuery.value = ''
  catalogCategoryId.value = null
  catalogTypeFilter.value = 'EDIBLE'
  orders.error = null
  showPOS.value = true
}

function addToCart(p: Product) {
  if (p.type === 'INEDIBLE' && (p.stock ?? 0) === 0) return
  const existing = cart.value.find((l) => l.productId === p.id)
  if (existing) {
    if (p.type === 'INEDIBLE' && existing.quantity >= (p.stock ?? 0)) return
    existing.quantity += 1
  } else {
    cart.value.push({
      productId: p.id,
      name: p.name,
      price: p.price,
      quantity: 1,
      type: p.type,
      stock: p.stock,
    })
  }
}

function incQty(line: CartLine) {
  if (line.type === 'INEDIBLE' && line.stock != null && line.quantity >= line.stock) return
  line.quantity += 1
}

function decQty(line: CartLine) {
  line.quantity -= 1
  if (line.quantity <= 0) removeLine(line.productId)
}

function removeLine(productId: number) {
  cart.value = cart.value.filter((l) => l.productId !== productId)
}

async function submitOrder() {
  if (cart.value.length === 0) {
    orders.error = 'Agrega al menos un producto'
    return
  }
  markFormTouched('amountPaid')
  if (formErrors.value.amountPaid) return
  const clientId = form.value.clientId && form.value.clientId > 0 ? form.value.clientId : null
  const order = await orders.create({
    tableNumber: form.value.tableNumber === 'TAKEAWAY' ? 0 : form.value.tableNumber,
    paymentMethod: form.value.paymentMethod,
    amountPaid: Number(form.value.amountPaid),
    notes: form.value.notes || undefined,
    clientId,
    userId: auth.userId!,
    details: cart.value.map((l) => ({ productId: l.productId, quantity: l.quantity })),
  })
  if (order) {
    showReceipt.value = true
    createdOrder.value = order
    await products.fetchAll()
  }
}

function printReceipt() {
  window.print()
}

function closeReceipt() {
  showReceipt.value = false
  createdOrder.value = null
  showPOS.value = false
}

function openDetail(order: Order) {
  selectedOrder.value = order
  showDetail.value = true
}

async function changeStatus(status: OrderStatus) {
  if (!selectedOrder.value) return
  const ok = await orders.updateStatus(selectedOrder.value.id, status)
  if (ok) {
    selectedOrder.value = orders.items.find((o) => o.id === selectedOrder.value!.id) ?? null
  }
}

async function handleDelete(id: number) {
  if (confirm('¿Eliminar esta orden?')) {
    await orders.remove(id)
    showDetail.value = false
  }
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleString('es', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}
function formatPrice(n: number) {
  return `Bs ${n.toFixed(2)}`
}

const posTitle = computed(() => {
  if (showReceipt.value && createdOrder.value) return `Recibo #${createdOrder.value.id}`
  return 'Nueva Orden'
})
</script>

<template>
  <div class="page">
    <PageHeader title="Órdenes" :subtitle="`${orders.items.length} órdenes registradas`">
      <template #actions>
        <BaseButton variant="primary" @click="openPOS">+ Nueva Orden</BaseButton>
      </template>
    </PageHeader>

    <div class="tabs">
      <button :class="['tab', filter === 'ALL' && 'tab-active']" @click="filter = 'ALL'">
        Todas
      </button>
      <button :class="['tab', filter === 'PENDING' && 'tab-active']" @click="filter = 'PENDING'">
        Pendientes <span class="count count-warning">{{ counts.PENDING }}</span>
      </button>
      <button :class="['tab', filter === 'READY_FOR_PICKUP' && 'tab-active']" @click="filter = 'READY_FOR_PICKUP'">
        Para recoger <span class="count count-info">{{ counts.READY_FOR_PICKUP }}</span>
      </button>
      <button :class="['tab', filter === 'DELIVERED' && 'tab-active']" @click="filter = 'DELIVERED'">
        Entregadas <span class="count count-success">{{ counts.DELIVERED }}</span>
      </button>
      <button :class="['tab', filter === 'CANCELLED' && 'tab-active']" @click="filter = 'CANCELLED'">
        Canceladas <span class="count count-danger">{{ counts.CANCELLED }}</span>
      </button>
    </div>

    <LoadingState v-if="orders.loading && orders.items.length === 0" />

    <EmptyState
      v-else-if="orders.items.length === 0"
      icon="🧾"
      title="Sin órdenes"
      message="Crea tu primera orden para empezar."
    />

    <template v-else>
      <div class="orders-grid">
        <article
          v-for="o in orders.items"
          :key="o.id"
          class="order-card"
          @click="openDetail(o)"
        >
          <div class="order-top">
            <span class="table-num">{{ o.tableNumber === 0 ? 'Para llevar' : `Mesa ${o.tableNumber}` }}</span>
            <BaseBadge :variant="statusConfig[o.status].variant">
              {{ statusConfig[o.status].label }}
            </BaseBadge>
          </div>
          <div class="order-body">
            <p class="order-items">{{ o.details.length }} producto{{ o.details.length !== 1 ? 's' : '' }}</p>
            <p v-if="o.clientName" class="order-client"> {{ o.clientName }}</p>
            <p class="order-time"> {{ formatTime(o.dateTime) }}</p>
          </div>
          <div class="order-bottom">
            <span class="order-total">{{ formatPrice(o.total) }}</span>
            <span class="order-payment">{{ o.paymentMethod }}</span>
          </div>
        </article>
      </div>

      <PaginationBar
        :page="orders.page"
        :total-pages="orders.totalPages"
        :total-elements="orders.totalElements"
        @page-change="goToPage"
      />
    </template>

    <!-- POS Drawer -->
    <BaseDrawer v-model="showPOS" :title="posTitle" wide>
      <div v-if="showReceipt && createdOrder" class="receipt">
        <div class="receipt-paper">
          <h2 class="receipt-title">Poképlace</h2>
          <p class="receipt-subtitle">Recibo de venta</p>

          <div class="receipt-divider"></div>

          <div class="receipt-info">
            <p><strong>#{{ createdOrder.id }}</strong></p>
            <p>{{ formatTime(createdOrder.dateTime) }}</p>
            <p>{{ createdOrder.tableNumber === 0 ? 'Para llevar' : `Mesa ${createdOrder.tableNumber}` }}</p>
            <p>Cajero: {{ createdOrder.userName }}</p>
            <p v-if="createdOrder.clientName">Cliente: {{ createdOrder.clientName }}</p>
          </div>

          <div class="receipt-divider"></div>

          <table class="receipt-items">
            <thead>
              <tr>
                <th class="col-qty">Cant</th>
                <th class="col-desc">Producto</th>
                <th class="col-price">Precio</th>
                <th class="col-sub">Subtotal</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in createdOrder.details" :key="d.id">
                <td>{{ d.quantity }}×</td>
                <td>{{ d.productName }}</td>
                <td>{{ d.subtotal != null ? formatPrice(d.subtotal / d.quantity) : '—' }}</td>
                <td class="receipt-amount">{{ formatPrice(d.subtotal ?? 0) }}</td>
              </tr>
            </tbody>
          </table>

          <div class="receipt-divider"></div>

          <div class="receipt-totals">
            <div class="receipt-total-row">
              <span>Total</span>
              <strong>{{ formatPrice(createdOrder.total) }}</strong>
            </div>
            <div class="receipt-total-row">
              <span>Método de pago</span>
              <span>{{ createdOrder.paymentMethod }}</span>
            </div>
            <div v-if="createdOrder.amountPaid != null" class="receipt-total-row">
              <span>Recibido</span>
              <span>{{ formatPrice(createdOrder.amountPaid) }}</span>
            </div>
            <div v-if="createdOrder.change != null && createdOrder.change > 0" class="receipt-total-row">
              <span>Cambio</span>
              <span>{{ formatPrice(createdOrder.change) }}</span>
            </div>
          </div>

          <div v-if="createdOrder.notes" class="receipt-notes">
            <div class="receipt-divider"></div>
            <p>Notas: {{ createdOrder.notes }}</p>
          </div>

          <div class="receipt-divider"></div>

          <p class="receipt-footer">¡Gracias por su compra!</p>
        </div>

        <div class="receipt-actions">
          <BaseButton variant="primary" block @click="printReceipt">
            Imprimir recibo
          </BaseButton>
          <BaseButton variant="ghost" block @click="closeReceipt">
            Cerrar
          </BaseButton>
        </div>
      </div>
      <div v-else class="pos-layout">
        <!-- Catálogo -->
        <section class="catalog">
          <div class="catalog-type-toggle">
            <button
              :class="['type-btn', catalogTypeFilter === 'EDIBLE' && 'type-btn-active']"
              @click="catalogTypeFilter = 'EDIBLE'; catalogCategoryId = null"
            >
              Comestibles
            </button>
            <button
              :class="['type-btn', catalogTypeFilter === 'INEDIBLE' && 'type-btn-active']"
              @click="catalogTypeFilter = 'INEDIBLE'; catalogCategoryId = null"
            >
              Souvenirs
            </button>
          </div>
          <div class="catalog-filters">
            <SearchBar v-model="catalogQuery" placeholder="Buscar producto..." />
            <BaseSelect
              v-model="catalogCategoryId"
              :options="categoryFilterOptions"
              placeholder="Todas las categorías"
            />
          </div>
          <div class="catalog-grid">
            <button
              v-for="p in filteredCatalog"
              :key="p.id"
              :disabled="p.type === 'INEDIBLE' && (p.stock ?? 0) === 0"
              class="product-tile"
              @click="addToCart(p)"
            >
              <div class="tile-top">
                <BaseBadge :variant="p.type === 'EDIBLE' ? 'primary' : 'info'">
                  {{ p.type === 'EDIBLE' ? 'Poke' : 'Stock' }}
                </BaseBadge>
                <span v-if="p.type === 'INEDIBLE'" class="tile-stock">{{ p.stock }}</span>
              </div>
              <h4>{{ p.name }}</h4>
              <p v-if="p.pokeName" class="tile-poke">{{ p.pokeName }}</p>
              <span class="tile-price">{{ formatPrice(p.price) }}</span>
            </button>
            <p v-if="filteredCatalog.length === 0" class="catalog-empty">Sin productos</p>
          </div>
        </section>

        <!-- Cart + Form -->
        <aside class="cart">
          <h3 class="cart-title">Carrito</h3>
          <div v-if="cart.length === 0" class="cart-empty">
            Agrega productos del catálogo
          </div>
          <ul v-else class="cart-list">
            <li v-for="line in cart" :key="line.productId" class="cart-line">
              <div class="line-info">
                <strong>{{ line.name }}</strong>
                <small>{{ formatPrice(line.price) }} c/u</small>
              </div>
              <div class="qty">
                <button @click="decQty(line)">−</button>
                <span>{{ line.quantity }}</span>
                <button @click="incQty(line)">+</button>
              </div>
              <span class="line-sub">{{ formatPrice(line.price * line.quantity) }}</span>
              <button class="line-remove" @click="removeLine(line.productId)">✕</button>
            </li>
          </ul>

          <div class="cart-total">
            <span>Total</span>
            <strong>{{ formatPrice(total) }}</strong>
          </div>

          <div class="cart-form">
            <BaseSelect
              v-model="form.tableNumber"
              label="Mesa"
              :options="[
                { value: 1, label: 'Mesa 1' },
                { value: 2, label: 'Mesa 2' },
                { value: 3, label: 'Mesa 3' },
                { value: 4, label: 'Mesa 4' },
                { value: 5, label: 'Mesa 5' },
                { value: 6, label: 'Mesa 6' },
                { value: 'TAKEAWAY', label: 'Para llevar' },
              ]"
              required
            />
            <div class="payment-method-group">
              <label class="field-label">Método de pago</label>
              <div class="payment-method-btns">
                <button
                  v-for="m in paymentMethods"
                  :key="m.value"
                  type="button"
                  :class="['payment-method-btn', form.paymentMethod === m.value && 'payment-method-btn-active']"
                  @click="form.paymentMethod = m.value"
                >
                  <span class="pm-icon" v-html="paymentMethodIcons[m.value]"></span>
                  {{ m.label }}
                </button>
              </div>
            </div>
            <div class="client-field-row">
              <BaseSelect
                v-model="form.clientId"
                label="Cliente (opcional)"
                :options="clientOptions"
                class="client-select"
              />
              <button class="btn-add-client" type="button" title="Nuevo cliente" @click="toggleQuickClient">
                <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M8 3v10M3 8h10"/></svg>
              </button>
            </div>
            <div v-if="showQuickClient" class="quick-client-form">
              <BaseInput v-model="quickClientForm.name" placeholder="Nombre"
                :error="quickClientTouched.has('name') ? quickClientErrors.name : ''"
                @blur="markQuickClientTouched('name')" />
              <div class="quick-client-row">
                <BaseInput v-model="quickClientForm.nit" placeholder="NIT"
                  :error="quickClientTouched.has('nit') ? quickClientErrors.nit : ''"
                  @blur="markQuickClientTouched('nit')" />
                <BaseInput v-model="quickClientForm.ci" placeholder="CI"
                  :error="quickClientTouched.has('ci') ? quickClientErrors.ci : ''"
                  @blur="markQuickClientTouched('ci')" />
              </div>
              <BaseInput v-model="quickClientForm.phone" placeholder="Celular"
                :error="quickClientTouched.has('phone') ? quickClientErrors.phone : ''"
                @blur="markQuickClientTouched('phone')" />
              <BaseInput v-model="quickClientForm.email" placeholder="Email (opcional)" type="email"
                :error="quickClientTouched.has('email') ? quickClientErrors.email : ''"
                @blur="markQuickClientTouched('email')" />
              <BaseButton size="sm" variant="primary" block :loading="quickClientLoading" @click="submitQuickClient">
                {{ quickClientLoading ? 'Guardando...' : 'Guardar Cliente' }}
              </BaseButton>
            </div>
            <BaseTextarea v-model="form.notes" label="Notas" placeholder="Sin cebolla, extra palta..." />
          </div>

          <div class="payment-section">
            <template v-if="form.paymentMethod === 'EFECTIVO'">
              <BaseInput
                v-model="form.amountPaid"
                label="Monto recibido (Bs)"
                type="number"
                :min="total"
                step="0.5"
                required
                :error="formTouched.has('amountPaid') ? formErrors.amountPaid : ''"
                @blur="markFormTouched('amountPaid')"
              />
              <div class="change-row">
                <span>Cambio</span>
                <strong :class="changeAmount > 0 ? 'change-positive' : 'change-zero'">
                  {{ formatPrice(changeAmount) }}
                </strong>
              </div>
            </template>
            <div v-else class="auto-payment-row">
              <span>Total a cobrar</span>
              <strong>{{ formatPrice(total) }}</strong>
            </div>
          </div>

          <AlertBanner v-if="orders.error" :message="orders.error" />

          <BaseButton
            variant="primary"
            size="lg"
            block
            :disabled="cart.length === 0"
            :loading="orders.loading"
            @click="submitOrder"
          >
            Crear orden · {{ formatPrice(total) }}
          </BaseButton>
        </aside>
      </div>
    </BaseDrawer>

    <!-- Detail Drawer -->
    <BaseDrawer v-model="showDetail" :title="`Orden #${selectedOrder?.id ?? ''}`">
      <div v-if="selectedOrder" class="detail-summary">
        <BaseBadge :variant="statusConfig[selectedOrder.status].variant">
          {{ statusConfig[selectedOrder.status].label }}
        </BaseBadge>
        <p><strong>Mesa:</strong> {{ selectedOrder.tableNumber === 0 ? 'Para llevar' : selectedOrder.tableNumber }}</p>
        <p><strong>Fecha:</strong> {{ formatTime(selectedOrder.dateTime) }}</p>
        <p><strong>Cajero:</strong> {{ selectedOrder.userName }}</p>
        <p v-if="selectedOrder.clientName"><strong>Cliente:</strong> {{ selectedOrder.clientName }}</p>
        <p><strong>Pago:</strong> {{ selectedOrder.paymentMethod }}</p>
        <p v-if="selectedOrder.amountPaid != null"><strong>Recibido:</strong> {{ formatPrice(selectedOrder.amountPaid) }}</p>
        <p v-if="selectedOrder.change != null && selectedOrder.change > 0"><strong>Cambio:</strong> {{ formatPrice(selectedOrder.change) }}</p>
        <p v-if="selectedOrder.notes"><strong>Notas:</strong> {{ selectedOrder.notes }}</p>
      </div>

      <div v-if="selectedOrder" class="detail-details">
        <h4>Productos</h4>
        <ul>
          <li v-for="d in selectedOrder.details" :key="d.id">
            <span>{{ d.quantity }}× {{ d.productName }}</span>
            <span>{{ formatPrice(d.subtotal ?? 0) }}</span>
          </li>
        </ul>
        <div class="detail-total">
          <span>Total</span>
          <strong>{{ formatPrice(selectedOrder.total) }}</strong>
        </div>
      </div>

      <div
        v-if="selectedOrder && (selectedOrder.status === 'PENDING' || selectedOrder.status === 'READY_FOR_PICKUP')"
        class="detail-actions"
      >
        <BaseButton variant="primary" block :loading="orders.loading" @click="changeStatus('DELIVERED')">
          Marcar como entregada
        </BaseButton>
        <BaseButton
          v-if="selectedOrder.status === 'PENDING'"
          variant="danger"
          block
          :loading="orders.loading"
          @click="changeStatus('CANCELLED')"
        >
          Cancelar orden
        </BaseButton>
      </div>

      <AlertBanner v-if="orders.error" :message="orders.error" />

      <template v-if="selectedOrder" #footer>
        <BaseButton variant="secondary" block @click="showDetail = false">Cerrar</BaseButton>
        <BaseButton variant="danger" block @click="handleDelete(selectedOrder.id)">
          Eliminar
        </BaseButton>
      </template>
    </BaseDrawer>
  </div>
</template>

<style scoped>
.page {
  padding: 2rem;
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 0.25rem;
  background: var(--color-surface);
  padding: 0.3rem;
  border-radius: var(--radius-lg);
  border: 1.5px solid var(--color-border);
  align-self: flex-start;
}

.tab {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  transition: all var(--transition-fast);
}

.tab:hover {
  color: var(--color-text);
}
.tab-active {
  background: var(--color-dark);
  color: white;
}

.count {
  background: var(--color-surface-muted);
  color: var(--color-text-muted);
  font-size: var(--text-xs);
  padding: 0.1rem 0.4rem;
  border-radius: var(--radius-pill);
  font-weight: 700;
}
.tab-active .count {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}
.count-warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}
.count-success {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.count-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
.count-info {
  background: var(--color-info-soft);
  color: var(--color-info);
}

/* Orders grid */
.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.order-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border: 1.5px solid transparent;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.order-card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-num {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
}

.order-items,
.order-client,
.order-time {
  color: var(--color-text-subtle);
  font-size: var(--text-sm);
  margin: 0.15rem 0;
}

.order-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}

.order-total {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: var(--text-lg);
  color: var(--color-primary);
}

.order-payment {
  font-size: var(--text-xs);
  color: var(--color-text-ghost);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* POS */
.pos-layout {
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  gap: 1.5rem;
  min-height: 500px;
}

.catalog-type-toggle {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.type-btn {
  flex: 1;
  padding: 0.55rem 1rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.type-btn:hover {
  color: var(--color-text);
  border-color: var(--color-dark);
}

.type-btn-active {
  background: var(--color-dark);
  color: white;
  border-color: var(--color-dark);
}

.catalog-filters {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.catalog-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 0.5rem;
}

.product-tile {
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 0.75rem;
  cursor: pointer;
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  transition: all var(--transition-fast);
}

.product-tile:hover:not(:disabled) {
  border-color: var(--color-dark);
  transform: translateY(-1px);
}

.product-tile:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.tile-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tile-stock {
  font-size: var(--text-xs);
  color: var(--color-text-subtle);
}

.product-tile h4 {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text);
}

.tile-poke {
  margin: 0;
  font-size: var(--text-xs);
  color: var(--color-primary);
  font-style: italic;
}

.tile-price {
  font-weight: 700;
  color: var(--color-primary);
  font-size: var(--text-sm);
  margin-top: 0.25rem;
}

.catalog-empty {
  grid-column: 1 / -1;
  text-align: center;
  color: var(--color-text-ghost);
  padding: 2rem;
}

/* Cart */
.cart {
  background: var(--color-surface-muted);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.cart-title {
  font-family: var(--font-display);
  margin: 0;
  font-size: var(--text-lg);
}

.cart-empty {
  padding: 2rem 1rem;
  text-align: center;
  color: var(--color-text-ghost);
  font-size: var(--text-sm);
}

.cart-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-height: 220px;
  overflow-y: auto;
}

.cart-line {
  display: grid;
  grid-template-columns: 1fr auto auto auto;
  align-items: center;
  gap: 0.5rem;
  background: var(--color-surface);
  padding: 0.5rem 0.75rem;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
}

.line-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.line-info strong {
  font-size: var(--text-sm);
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.line-info small {
  color: var(--color-text-ghost);
  font-size: var(--text-xs);
}

.qty {
  display: flex;
  align-items: center;
  gap: 0.35rem;
}

.qty button {
  width: 24px;
  height: 24px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  cursor: pointer;
  font-weight: 700;
  color: var(--color-text);
}

.qty button:hover {
  background: var(--color-dark);
  color: white;
  border-color: var(--color-dark);
}

.qty span {
  min-width: 20px;
  text-align: center;
  font-weight: 600;
}

.line-sub {
  font-weight: 700;
  color: var(--color-primary);
  font-size: var(--text-sm);
}

.line-remove {
  background: transparent;
  border: none;
  color: var(--color-text-ghost);
  cursor: pointer;
  font-size: var(--text-sm);
}
.line-remove:hover {
  color: var(--color-danger);
}

.cart-total {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 0;
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
  font-family: var(--font-display);
}

.cart-total strong {
  color: var(--color-primary);
  font-size: var(--text-lg);
}

.cart-form {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.client-field-row {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}
.client-field-row .client-select {
  flex: 1;
}

.btn-add-client {
  width: 42px;
  height: 42px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.btn-add-client:hover {
  border-color: var(--color-dark);
  color: var(--color-dark);
}
.btn-add-client svg {
  width: 18px;
  height: 18px;
}

.quick-client-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  border: 1.5px solid var(--color-border);
  animation: fadeInUp 0.2s ease-out;
}

.quick-client-row {
  display: flex;
  gap: 0.5rem;
}
.quick-client-row > * {
  flex: 1;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

.payment-method-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-label {
  font-size: var(--text-sm);
  font-weight: 600;
  color: var(--color-text-muted);
}

.payment-method-btns {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.5rem;
}

.payment-method-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 0.65rem 0.5rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.payment-method-btn:hover {
  border-color: var(--color-dark);
  color: var(--color-text);
}

.payment-method-btn-active {
  background: var(--color-dark);
  border-color: var(--color-dark);
  color: white;
}

.pm-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}
.pm-icon :deep(svg) {
  width: 1.5rem;
  height: 1.5rem;
}

.payment-section {
  background: var(--color-surface);
  padding: 1rem;
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.auto-payment-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-family: var(--font-display);
  font-size: var(--text-lg);
  padding: 0.25rem 0;
}

.auto-payment-row strong {
  color: var(--color-primary);
}

.change-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--text-lg);
  font-family: var(--font-display);
  padding: 0.5rem 0;
  border-top: 1px dashed var(--color-border);
}

.change-positive {
  color: var(--color-success);
  font-size: var(--text-xl);
}

.change-zero {
  color: var(--color-text-ghost);
}

/* Detail */
.detail-summary {
  background: var(--color-surface-muted);
  padding: 1rem;
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.detail-summary p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.detail-details h4 {
  font-family: var(--font-display);
  font-size: var(--text-md);
  margin: 0 0 0.5rem;
}

.detail-details ul {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.detail-details li {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0.75rem;
  background: var(--color-surface-muted);
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
}

.detail-total {
  display: flex;
  justify-content: space-between;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
  font-family: var(--font-display);
  margin-top: 0.75rem;
}

.detail-total strong {
  color: var(--color-primary);
  font-size: var(--text-lg);
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

/* ── Tablet ── */
@media (max-width: 900px) {
  .page {
    padding: 1.25rem;
    gap: 1rem;
  }

  .tabs {
    align-self: stretch;
    overflow-x: auto;
    scrollbar-width: none;
  }
  .tabs::-webkit-scrollbar { display: none; }

  .tab {
    white-space: nowrap;
    flex-shrink: 0;
  }

  .pos-layout {
    grid-template-columns: 1fr;
    gap: 1rem;
  }

  .catalog {
    min-height: 0;
  }

  .catalog-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    max-height: 40vh;
    overflow-y: auto;
  }

  .cart-list {
    max-height: 140px;
  }
}

/* ── Mobile ── */
@media (max-width: 600px) {
  .page {
    padding: 0.875rem;
    gap: 0.875rem;
  }

  .orders-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.625rem;
  }

  .order-card {
    padding: 0.875rem;
    gap: 0.5rem;
  }

  .table-num {
    font-size: var(--text-md);
  }

  .catalog-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .type-btn {
    padding: 0.45rem 0.5rem;
    font-size: var(--text-xs);
  }

  .payment-method-btn {
    padding: 0.5rem 0.25rem;
    font-size: var(--text-xs);
  }

  .pm-icon {
    font-size: 1rem;
  }
}

/* Receipt */
.receipt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  padding: 1rem 0;
}

.receipt-paper {
  width: 100%;
  max-width: 320px;
  background: #fff;
  color: #111;
  padding: 1.5rem 1rem;
  font-size: 0.8rem;
  line-height: 1.4;
}

.receipt-title {
  text-align: center;
  margin: 0 0 0.2rem;
  font-size: 1.6rem;
  font-weight: 800;
  color: #111;
}

.receipt-subtitle {
  text-align: center;
  margin: 0 0 0.5rem;
  font-size: 0.85rem;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.receipt-divider {
  border-top: 1px dashed #999;
  margin: 0.5rem 0;
}

.receipt-info p {
  margin: 0.15rem 0;
  color: #333;
}

.receipt-items {
  width: 100%;
  border-collapse: collapse;
}

.receipt-items th {
  text-align: left;
  font-size: 0.7rem;
  text-transform: uppercase;
  color: #777;
  padding: 0.25rem 0;
  border-bottom: 1px solid #ccc;
}

.receipt-items td {
  padding: 0.25rem 0;
  color: #222;
}

.col-qty { width: 2rem; }
.col-price { width: 3.5rem; text-align: right; }
.col-sub { width: 3.5rem; text-align: right; }
.col-desc { }

.receipt-items td:nth-child(3),
.receipt-items td:nth-child(4) {
  text-align: right;
}

.receipt-amount {
  font-weight: 600;
}

.receipt-totals {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.receipt-total-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #333;
}

.receipt-total-row strong {
  font-size: 1rem;
  color: #111;
}

.receipt-notes p {
  margin: 0;
  color: #555;
  font-style: italic;
}

.receipt-footer {
  text-align: center;
  margin: 0;
  font-size: 0.75rem;
  color: #888;
}

.receipt-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 100%;
  max-width: 320px;
}

@media print {
  body * {
    visibility: hidden;
  }
  .receipt,
  .receipt * {
    visibility: visible;
  }
  .receipt {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    padding: 0;
  }
  .receipt-paper {
    max-width: 100%;
    box-shadow: none;
    padding: 0.5in;
  }
  .receipt-actions {
    display: none !important;
  }
  .receipt-divider {
    border-top: 1px dashed #000;
  }
}
</style>
