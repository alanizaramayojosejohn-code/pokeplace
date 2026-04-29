<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { useOrdersStore } from '../stores/orders'
import { useAuthStore } from '../stores/auth'
import type { OrderStatus } from '../types/api'
import BaseBadge from '../components/ui/BaseBadge.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import AlertBanner from '../components/ui/AlertBanner.vue'

const ordersStore = useOrdersStore()
const authStore = useAuthStore()

onMounted(() => {
  ordersStore.fetchAll()
})

const role = computed(() => authStore.userRole)

// ---------- Kitchen ----------
const pendingOrders = computed(() =>
  [...ordersStore.items]
    .filter((o) => o.status === 'PENDING')
    .sort((a, b) => new Date(a.dateTime).getTime() - new Date(b.dateTime).getTime()),
)

async function markReady(id: number) {
  await ordersStore.updateStatus(id, 'READY_FOR_PICKUP')
}
async function markDelivered(id: number) {
  await ordersStore.updateStatus(id, 'DELIVERED')
}

// ---------- Cashier / Admin ----------
function isToday(iso: string) {
  const d = new Date(iso)
  const now = new Date()
  return (
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
  )
}

const todayOrders = computed(() => ordersStore.items.filter((o) => isToday(o.dateTime)))

const scopedTodayOrders = computed(() =>
  role.value === 'ADMIN'
    ? todayOrders.value
    : todayOrders.value.filter((o) => o.userId === authStore.userId),
)

const registryOrders = computed(() =>
  [...scopedTodayOrders.value].sort(
    (a, b) => new Date(b.dateTime).getTime() - new Date(a.dateTime).getTime(),
  ),
)

const stats = computed(() => {
  const list = scopedTodayOrders.value
  const valid = list.filter((o) => o.status !== 'CANCELLED')
  return {
    count: list.length,
    revenue: valid.reduce((s, o) => s + o.total, 0),
    pending: list.filter((o) => o.status === 'PENDING').length,
    ready: list.filter((o) => o.status === 'READY_FOR_PICKUP').length,
    delivered: list.filter((o) => o.status === 'DELIVERED').length,
  }
})

const statusConfig: Record<OrderStatus, { label: string; variant: 'warning' | 'success' | 'danger' | 'info' }> = {
  PENDING: { label: 'Pendiente', variant: 'warning' },
  READY_FOR_PICKUP: { label: 'Lista', variant: 'info' },
  DELIVERED: { label: 'Entregada', variant: 'success' },
  CANCELLED: { label: 'Cancelada', variant: 'danger' },
}

function formatTime(iso: string) {
  return new Date(iso).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

function formatPrice(n: number) {
  return `Bs ${n.toFixed(2)}`
}
</script>

<template>
  <div class="page">
    <!-- ============== KITCHEN ============== -->
    <template v-if="role === 'KITCHEN'">
      <div class="page-header">
        <div>
          <h1>Cocina</h1>
          <p>Órdenes enviadas desde caja</p>
        </div>
        <div class="counter">
          <span class="counter-value">{{ pendingOrders.length }}</span>
          <span class="counter-label">Pendientes</span>
        </div>
      </div>

      <LoadingState v-if="ordersStore.loading && pendingOrders.length === 0" />
      <AlertBanner v-else-if="ordersStore.error" :message="ordersStore.error" />
      <EmptyState
        v-else-if="pendingOrders.length === 0"
        icon="🎉"
        title="No hay órdenes pendientes"
        message="Todo al día por ahora."
      />

      <div v-else class="orders-grid">
        <article v-for="order in pendingOrders" :key="order.id" class="order-card">
          <header class="order-header">
            <div class="order-info">
              <span class="order-number">#{{ order.id }}</span>
              <span class="order-table">Mesa {{ order.tableNumber }}</span>
            </div>
            <span class="received-at">⏱ {{ formatTime(order.dateTime) }}</span>
          </header>

          <ul class="order-items">
            <li v-for="item in order.details" :key="item.id ?? item.productId">
              <span class="item-qty">{{ item.quantity }}×</span>
              <span class="item-name">{{ item.productName ?? `Producto #${item.productId}` }}</span>
            </li>
          </ul>

          <p v-if="order.notes" class="order-notes">📝 {{ order.notes }}</p>

          <footer class="order-actions">
            <button class="btn-pickup" :disabled="ordersStore.loading" @click="markReady(order.id)">
              📦 Lista
            </button>
            <button class="btn-complete" :disabled="ordersStore.loading" @click="markDelivered(order.id)">
              ✓ Entregada
            </button>
          </footer>
        </article>
      </div>
    </template>

    <!-- ============== CASHIER / ADMIN ============== -->
    <template v-else>
      <div class="page-header">
        <div>
          <h1>{{ role === 'ADMIN' ? 'Resumen del día' : 'Caja' }}</h1>
          <p>
            {{
              role === 'ADMIN'
                ? 'Actividad de hoy en el local'
                : 'Tus ventas y registro de órdenes de hoy'
            }}
          </p>
        </div>
        <RouterLink v-if="role === 'CASHIER'" to="/orders" class="cta-new-order">
          + Nueva Orden
        </RouterLink>
      </div>

      <div class="stats-grid">
        <div class="stat-card stat-primary">
          <span class="stat-label">Ingresos hoy</span>
          <span class="stat-value">{{ formatPrice(stats.revenue) }}</span>
          <span class="stat-foot">Excluye canceladas</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Órdenes</span>
          <span class="stat-value">{{ stats.count }}</span>
          <span class="stat-foot">{{ stats.delivered }} entregadas</span>
        </div>
        <div class="stat-card stat-warning">
          <span class="stat-label">Pendientes</span>
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-foot">En cocina</span>
        </div>
        <div class="stat-card stat-info">
          <span class="stat-label">Para recoger</span>
          <span class="stat-value">{{ stats.ready }}</span>
          <span class="stat-foot">Listas</span>
        </div>
      </div>

      <section class="registry">
        <header class="registry-header">
          <h2>Registro de órdenes</h2>
          <span class="registry-sub">{{ registryOrders.length }} hoy</span>
        </header>

        <LoadingState v-if="ordersStore.loading && registryOrders.length === 0" />
        <AlertBanner v-else-if="ordersStore.error" :message="ordersStore.error" />
        <EmptyState
          v-else-if="registryOrders.length === 0"
          icon="🧾"
          title="Aún no hay órdenes hoy"
          message="Las órdenes que registres aparecerán aquí."
        />

        <div v-else class="registry-table">
          <div class="registry-row registry-head">
            <span>#</span>
            <span>Hora</span>
            <span>Mesa</span>
            <span>Productos</span>
            <span v-if="role === 'ADMIN'">Cajero</span>
            <span>Pago</span>
            <span>Total</span>
            <span>Estado</span>
          </div>

          <RouterLink
            v-for="o in registryOrders"
            :key="o.id"
            to="/orders"
            class="registry-row"
            :class="{ 'with-cashier': role === 'ADMIN' }"
          >
            <span class="r-id">#{{ o.id }}</span>
            <span>{{ formatTime(o.dateTime) }}</span>
            <span>Mesa {{ o.tableNumber }}</span>
            <span>{{ o.details.length }} producto{{ o.details.length !== 1 ? 's' : '' }}</span>
            <span v-if="role === 'ADMIN'" class="r-cashier">{{ o.userName }}</span>
            <span class="r-payment">{{ o.paymentMethod }}</span>
            <span class="r-total">{{ formatPrice(o.total) }}</span>
            <BaseBadge :variant="statusConfig[o.status].variant">
              {{ statusConfig[o.status].label }}
            </BaseBadge>
          </RouterLink>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.page {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.page-header h1 {
  font-size: 1.8rem;
  color: var(--color-text);
  margin: 0 0 0.2rem;
}

.page-header p {
  color: var(--color-text-subtle);
  font-size: 0.9rem;
  margin: 0;
}

/* ---------- Kitchen counter ---------- */
.counter {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.8rem 1.2rem;
  background: var(--color-primary-soft);
  border-radius: var(--radius-md);
  min-width: 90px;
}

.counter-value {
  font-size: 1.8rem;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
}

.counter-label {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--color-text-subtle);
  margin-top: 0.2rem;
}

/* ---------- Kitchen orders grid ---------- */
.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.order-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 1.2rem;
  border-left: 4px solid var(--color-primary);
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
  box-shadow: var(--shadow-sm);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.order-number {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--color-text);
}

.order-table {
  font-size: 0.85rem;
  color: var(--color-text-subtle);
}

.received-at {
  font-size: 0.8rem;
  color: var(--color-text-subtle);
}

.order-items {
  list-style: none;
  padding: 0.8rem 0 0;
  margin: 0;
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.order-items li {
  display: flex;
  gap: 0.5rem;
  font-size: 0.9rem;
}

.item-qty {
  font-weight: 700;
  color: var(--color-primary);
  min-width: 28px;
}

.item-name {
  color: var(--color-text);
}

.order-notes {
  margin: 0;
  font-size: 0.85rem;
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
  padding: 0.5rem 0.7rem;
  border-radius: var(--radius-sm);
}

.order-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-complete,
.btn-pickup {
  flex: 1;
  padding: 0.6rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: inherit;
  border: 1.5px solid transparent;
}

.btn-complete:disabled,
.btn-pickup:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-complete {
  background: var(--color-success);
  color: white;
}
.btn-complete:not(:disabled):hover {
  background: #15803d;
}

.btn-pickup {
  background: transparent;
  border-color: var(--color-info);
  color: var(--color-info);
}
.btn-pickup:not(:disabled):hover {
  background: var(--color-info-soft);
}

/* ---------- Cashier / Admin ---------- */
.cta-new-order {
  background: var(--color-primary);
  color: white;
  padding: 0.7rem 1.2rem;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.cta-new-order:hover {
  filter: brightness(1.05);
  box-shadow: var(--shadow-sm);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 1.1rem 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  border-left: 4px solid var(--color-border);
  box-shadow: var(--shadow-sm);
}

.stat-card.stat-primary {
  border-left-color: var(--color-primary);
}
.stat-card.stat-warning {
  border-left-color: var(--color-warning);
}
.stat-card.stat-info {
  border-left-color: var(--color-info);
}

.stat-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--color-text-subtle);
  font-weight: 600;
}

.stat-value {
  font-size: 1.7rem;
  font-weight: 700;
  color: var(--color-text);
  font-family: var(--font-display);
  line-height: 1.1;
}

.stat-foot {
  font-size: 0.75rem;
  color: var(--color-text-ghost);
}

/* ---------- Registry ---------- */
.registry {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: 1.25rem;
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.registry-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.registry-header h2 {
  font-size: 1.15rem;
  margin: 0;
  color: var(--color-text);
}

.registry-sub {
  font-size: 0.8rem;
  color: var(--color-text-subtle);
}

.registry-table {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.registry-row {
  display: grid;
  grid-template-columns: 60px 70px 80px 1fr 90px 90px 110px;
  align-items: center;
  gap: 0.75rem;
  padding: 0.7rem 0.9rem;
  border-radius: var(--radius-sm);
  font-size: 0.85rem;
  color: var(--color-text);
  text-decoration: none;
  transition: background var(--transition-fast);
}

.registry-row.with-cashier {
  grid-template-columns: 60px 70px 80px 1fr 1fr 90px 90px 110px;
}

.registry-row:not(.registry-head):hover {
  background: var(--color-surface-muted);
}

.registry-head {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--color-text-ghost);
  font-weight: 600;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0.5rem;
}

.r-id {
  font-weight: 700;
  color: var(--color-text-muted);
}

.r-cashier {
  color: var(--color-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.r-payment {
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--color-text-ghost);
}

.r-total {
  font-weight: 700;
  color: var(--color-primary);
  font-family: var(--font-display);
}

@media (max-width: 768px) {
  .registry-row,
  .registry-row.with-cashier {
    grid-template-columns: 1fr 1fr;
    gap: 0.4rem;
  }
  .registry-head {
    display: none;
  }
}
</style>
