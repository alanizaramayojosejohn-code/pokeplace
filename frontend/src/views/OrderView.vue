<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
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

const orders = useOrdersStore()
const products = useProductsStore()
const categories = useCategoriesStore()
const clients = useClientsStore()
const auth = useAuthStore()

type Filter = 'ALL' | OrderStatus
const filter = ref<Filter>('ALL')

const showPOS = ref(false)
const showDetail = ref(false)
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

const form = ref({
  tableNumber: 1,
  paymentMethod: 'EFECTIVO',
  clientId: null as number | null,
  notes: '',
})

onMounted(async () => {
  await Promise.all([
    orders.fetchAll(),
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

const paymentOptions = [
  { value: 'EFECTIVO', label: '💵 Efectivo' },
  { value: 'TARJETA', label: '💳 Tarjeta' },
  { value: 'QR', label: '📱 QR / Transferencia' },
]

const filteredOrders = computed(() => {
  const list = [...orders.items].sort((a, b) =>
    new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime(),
  )
  if (filter.value === 'ALL') return list
  return list.filter((o) => o.status === filter.value)
})

const filteredCatalog = computed(() => {
  return products.items.filter((p) => {
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
  ...clients.items.map((c) => ({ value: c.id, label: `${c.name} · CI ${c.ci}` })),
])

const total = computed(() => cart.value.reduce((sum, l) => sum + l.price * l.quantity, 0))

const counts = computed(() => ({
  ALL: orders.items.length,
  PENDING: orders.items.filter((o) => o.status === 'PENDING').length,
  READY_FOR_PICKUP: orders.items.filter((o) => o.status === 'READY_FOR_PICKUP').length,
  DELIVERED: orders.items.filter((o) => o.status === 'DELIVERED').length,
  CANCELLED: orders.items.filter((o) => o.status === 'CANCELLED').length,
}))

function openPOS() {
  cart.value = []
  form.value = { tableNumber: 1, paymentMethod: 'EFECTIVO', clientId: null, notes: '' }
  catalogQuery.value = ''
  catalogCategoryId.value = null
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
  const clientId = form.value.clientId && form.value.clientId > 0 ? form.value.clientId : null
  const ok = await orders.create({
    tableNumber: Number(form.value.tableNumber),
    paymentMethod: form.value.paymentMethod,
    notes: form.value.notes || undefined,
    clientId,
    userId: auth.userId!,
    details: cart.value.map((l) => ({ productId: l.productId, quantity: l.quantity })),
  })
  if (ok) {
    showPOS.value = false
    await products.fetchAll() // refresca stocks
  }
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
        Todas <span class="count">{{ counts.ALL }}</span>
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
      v-else-if="filteredOrders.length === 0"
      icon="🧾"
      title="Sin órdenes"
      message="Crea tu primera orden para empezar."
    />

    <div v-else class="orders-grid">
      <article
        v-for="o in filteredOrders"
        :key="o.id"
        class="order-card"
        @click="openDetail(o)"
      >
        <div class="order-top">
          <span class="table-num">Mesa {{ o.tableNumber }}</span>
          <BaseBadge :variant="statusConfig[o.status].variant">
            {{ statusConfig[o.status].label }}
          </BaseBadge>
        </div>
        <div class="order-body">
          <p class="order-items">{{ o.details.length }} producto{{ o.details.length !== 1 ? 's' : '' }}</p>
          <p v-if="o.clientName" class="order-client">👤 {{ o.clientName }}</p>
          <p class="order-time">🕐 {{ formatTime(o.dateTime) }}</p>
        </div>
        <div class="order-bottom">
          <span class="order-total">{{ formatPrice(o.total) }}</span>
          <span class="order-payment">{{ o.paymentMethod }}</span>
        </div>
      </article>
    </div>

    <!-- POS Drawer -->
    <BaseDrawer v-model="showPOS" title="Nueva Orden" wide>
      <div class="pos-layout">
        <!-- Catálogo -->
        <section class="catalog">
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
          <h3 class="cart-title">🛒 Carrito</h3>
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
            <BaseInput
              v-model="form.tableNumber"
              label="Mesa"
              type="number"
              :min="1"
              required
            />
            <BaseSelect
              v-model="form.paymentMethod"
              label="Método de pago"
              :options="paymentOptions"
              required
            />
            <BaseSelect
              v-model="form.clientId"
              label="Cliente (opcional)"
              :options="clientOptions"
            />
            <BaseTextarea v-model="form.notes" label="Notas" placeholder="Sin cebolla, extra palta..." />
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
        <p><strong>Mesa:</strong> {{ selectedOrder.tableNumber }}</p>
        <p><strong>Fecha:</strong> {{ formatTime(selectedOrder.dateTime) }}</p>
        <p><strong>Cajero:</strong> {{ selectedOrder.userName }}</p>
        <p v-if="selectedOrder.clientName"><strong>Cliente:</strong> {{ selectedOrder.clientName }}</p>
        <p><strong>Pago:</strong> {{ selectedOrder.paymentMethod }}</p>
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

@media (max-width: 900px) {
  .pos-layout {
    grid-template-columns: 1fr;
  }
}
</style>
