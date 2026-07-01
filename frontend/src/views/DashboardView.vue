<script setup lang="ts">
import { computed, onMounted, ref, onUnmounted, nextTick } from 'vue'
import Chart from 'chart.js/auto'
import { RouterLink } from 'vue-router'
import { useOrdersStore } from '../stores/orders'
import { useAuthStore } from '../stores/auth'
import api from '../api/axios'
import { reportService } from '@/api/ReportService'
import type { DashboardReport } from '@/types/report'
import type { LowStockProduct, OrderStatus } from '../types/api'
import BaseBadge from '../components/ui/BaseBadge.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import AlertBanner from '../components/ui/AlertBanner.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import SkeletonCard from '@/components/ui/SkeletonCard.vue'

const ordersStore = useOrdersStore()
const authStore = useAuthStore()

const lowStockProducts = ref<LowStockProduct[]>([])
const lowStockLoading = ref(false)

const dashData = ref<DashboardReport | null>(null)
const dashLoading = ref(false)
const dashError = ref<string | null>(null)
const trendChartRef = ref<HTMLCanvasElement | null>(null)
const paymentChartRef = ref<HTMLCanvasElement | null>(null)
let trendChartInstance: Chart | null = null
let paymentChartInstance: Chart | null = null

const growthIcon = computed(() => {
  if (!dashData.value) return ''
  const g = dashData.value.salesGrowthPercent
  if (g > 0) return '↑'
  if (g < 0) return '↓'
  return '→'
})

const growthColor = computed(() => {
  if (!dashData.value) return ''
  const g = dashData.value.salesGrowthPercent
  if (g > 0) return 'var(--color-success)'
  if (g < 0) return 'var(--color-primary)'
  return 'var(--color-text-ghost)'
})

onMounted(async () => {
  ordersStore.fetchAll()
  if (authStore.userRole === 'ADMIN') {
    await Promise.all([fetchLowStock(), loadDashboard()])
  }
  if (authStore.userRole === 'KITCHEN') {
    fetchStockNotePending()
  }
})

onUnmounted(() => {
  trendChartInstance?.destroy()
  paymentChartInstance?.destroy()
})

async function loadDashboard() {
  dashLoading.value = true
  dashError.value = null
  try {
    const res = await reportService.getDashboard()
    dashData.value = res.data
    await nextTick()
    renderTrendChart()
    renderPaymentChart()
  } catch {
    dashError.value = 'Error al cargar el dashboard ejecutivo.'
  } finally {
    dashLoading.value = false
  }
}

function renderPaymentChart() {
  paymentChartInstance?.destroy()
  if (!paymentChartRef.value || !dashData.value?.salesByPaymentMethod.length) return
  const total = dashData.value.salesByPaymentMethod.reduce((s, m) => s + m.total, 0)
  paymentChartInstance = new Chart(paymentChartRef.value, {
    type: 'doughnut',
    data: {
      labels: dashData.value.salesByPaymentMethod.map(m => m.paymentMethod),
      datasets: [{
        data: dashData.value.salesByPaymentMethod.map(m => m.total),
        backgroundColor: ['#1a1a1a', '#E02020', '#555', '#999', '#ccc'],
        borderWidth: 0,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '70%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            usePointStyle: true,
            padding: 12,
            boxWidth: 8,
            font: { size: 11 },
            generateLabels(chart) {
              const data = chart.data
              return data.labels!.map((label, i) => ({
                text: label + '  Bs ' + (data.datasets[0].data[i] as number).toFixed(0),
                fillStyle: (data.datasets[0].backgroundColor as string[])[i],
                strokeStyle: 'transparent',
                pointStyle: 'circle',
                index: i
              }))
            }
          }
        }
      }
    },
    plugins: [{
      id: 'centerText',
      beforeDraw(chart) {
        const { width, height, ctx } = chart
        ctx.save()
        const cx = width / 2
        const cy = height / 2 - 6
        ctx.textAlign = 'center'
        ctx.textBaseline = 'middle'
        ctx.font = 'bold 16px "Inter", system-ui, sans-serif'
        ctx.fillStyle = '#e0e0e0'
        ctx.fillText('Bs ' + total.toFixed(0), cx, cy - 8)
        ctx.font = '10px "Inter", system-ui, sans-serif'
        ctx.fillStyle = '#888'
        ctx.fillText('Total hoy', cx, cy + 12)
        ctx.restore()
      }
    }]
  })
}

function renderTrendChart() {
  trendChartInstance?.destroy()
  if (!trendChartRef.value || !dashData.value?.weeklyTrend.length) return
  const trend = dashData.value.weeklyTrend
  const labels = trend.map(d => {
    const parts = d.date.split('-')
    return `${parts[2]}/${parts[1]}`
  })
  trendChartInstance = new Chart(trendChartRef.value, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Ventas',
        data: trend.map(d => d.totalSales),
        borderColor: '#1a1a1a',
        backgroundColor: (ctx) => {
          const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 300)
          g.addColorStop(0, 'rgba(26, 26, 26, 0.12)')
          g.addColorStop(1, 'rgba(26, 26, 26, 0)')
          return g
        },
        fill: true,
        tension: 0.4,
        pointRadius: 3,
        pointHoverRadius: 5,
        borderWidth: 2,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 600 },
      interaction: { intersect: false, mode: 'index' },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(0,0,0,0.9)',
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: (ctx) => `Bs ${(ctx.raw as number).toFixed(2)}`
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: 'rgba(0,0,0,0.06)' },
          ticks: { callback: (value) => 'Bs ' + Number(value).toFixed(0) }
        },
        x: { grid: { display: false } }
      }
    }
  })
}

async function fetchLowStock() {
  lowStockLoading.value = true
  try {
    const { data } = await api.get<LowStockProduct[]>('/products/low-stock')
    lowStockProducts.value = data
  } catch {
    // silent fail
  } finally {
    lowStockLoading.value = false
  }
}

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

// ---------- Kitchen Stock Notes ----------
const stockNoteText = ref('')
const stockNoteSubmitting = ref(false)
const stockNotePendingCount = ref(0)
const stockNoteError = ref<string | null>(null)

async function fetchStockNotePending() {
  try {
    const { data } = await api.get('/stock-notes')
    stockNotePendingCount.value = data.filter((n: any) => n.status === 'PENDING').length
  } catch { /* silent */ }
}

async function submitStockNote() {
  if (!stockNoteText.value.trim()) return
  stockNoteSubmitting.value = true
  stockNoteError.value = null
  try {
    await api.post('/stock-notes', { message: stockNoteText.value.trim() })
    stockNoteText.value = ''
    stockNotePendingCount.value += 1
  } catch {
    stockNoteError.value = 'No se pudo enviar el aviso. Intenta de nuevo.'
  } finally {
    stockNoteSubmitting.value = false
  }
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
          <h1 class="page-title">Cocina</h1>
          <p class="page-sub">Órdenes enviadas desde caja</p>
        </div>
        <div class="kitchen-counter">
          <span class="kitchen-counter-value">{{ pendingOrders.length }}</span>
          <span class="kitchen-counter-label">Pendientes</span>
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

      <div v-else class="kitchen-grid">
        <article v-for="order in pendingOrders" :key="order.id" class="kitchen-card">
          <header class="kitchen-card-header">
            <div class="kitchen-card-info">
              <span class="kitchen-card-number">#{{ order.id }}</span>
              <span class="kitchen-card-table">{{ order.tableNumber === 0 ? 'Para llevar' : `Mesa ${order.tableNumber}` }}</span>
            </div>
            <span class="kitchen-time">{{ formatTime(order.dateTime) }}</span>
          </header>

          <ul class="kitchen-items">
            <li v-for="item in order.details" :key="item.id ?? item.productId">
              <span class="kitchen-item-qty">{{ item.quantity }}×</span>
              <span class="kitchen-item-name">{{ item.productName ?? `Producto #${item.productId}` }}</span>
            </li>
          </ul>

          <p v-if="order.notes" class="kitchen-notes">{{ order.notes }}</p>

          <footer class="kitchen-actions">
            <button class="btn-ready" :disabled="ordersStore.loading" @click="markReady(order.id)">
              Marcar como Lista
            </button>
          </footer>
        </article>
      </div>

      <section class="stock-note-section">
        <header class="stock-note-header">
          <div>
            <h2 class="section-title">Reposición de Suministros</h2>
            <p class="section-sub">Notifica al admin lo que falta en cocina</p>
          </div>
          <span v-if="stockNotePendingCount > 0" class="stock-note-badge">
            {{ stockNotePendingCount }} pendiente{{ stockNotePendingCount !== 1 ? 's' : '' }}
          </span>
        </header>

        <textarea
          v-model="stockNoteText"
          class="stock-note-input"
          placeholder="Ej: Se acabó el arroz, queda poco aceite de oliva..."
          rows="3"
          @keydown.ctrl.enter="submitStockNote"
        />

        <div class="stock-note-footer">
          <span class="stock-note-hint">Ctrl + Enter para enviar</span>
          <button
            class="btn-stock-send"
            :disabled="!stockNoteText.trim() || stockNoteSubmitting"
            @click="submitStockNote"
          >
            {{ stockNoteSubmitting ? 'Enviando...' : 'Enviar aviso' }}
          </button>
        </div>

        <div v-if="stockNoteError" class="note-error">{{ stockNoteError }}</div>
      </section>
    </template>

    <!-- ============== CASHIER ============== -->
    <template v-else-if="role === 'CASHIER'">
      <div class="page-header">
        <div>
          <h1 class="page-title">Caja</h1>
          <p class="page-sub">Tus ventas y registro de órdenes de hoy</p>
        </div>
        <RouterLink to="/orders" class="btn-new-order">+ Nueva Orden</RouterLink>
      </div>

      <div class="stats-group">
        <div class="stat-card stat-accent">
          <span class="stat-label">Ingresos hoy</span>
          <span class="stat-value">{{ formatPrice(stats.revenue) }}</span>
          <span class="stat-sub">Excluye canceladas</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Órdenes</span>
          <span class="stat-value">{{ stats.count }}</span>
          <span class="stat-sub">{{ stats.delivered }} entregadas</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Pendientes</span>
          <span class="stat-value">{{ stats.pending }}</span>
          <span class="stat-sub">En cocina</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Para recoger</span>
          <span class="stat-value">{{ stats.ready }}</span>
          <span class="stat-sub">Listas</span>
        </div>
      </div>

      <section class="registry-section">
        <header class="registry-header">
          <h2 class="section-title">Registro de órdenes</h2>
          <span class="registry-count">{{ registryOrders.length }} hoy</span>
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
            <span>Pago</span>
            <span>Total</span>
            <span>Estado</span>
          </div>

          <RouterLink
            v-for="o in registryOrders" :key="o.id"
            to="/orders" class="registry-row"
          >
            <span class="r-id">#{{ o.id }}</span>
            <span>{{ formatTime(o.dateTime) }}</span>
            <span>{{ o.tableNumber === 0 ? 'Para llevar' : `Mesa ${o.tableNumber}` }}</span>
            <span>{{ o.details.length }} producto{{ o.details.length !== 1 ? 's' : '' }}</span>
            <span class="r-payment">{{ o.paymentMethod }}</span>
            <span class="r-total">{{ formatPrice(o.total) }}</span>
            <BaseBadge :variant="statusConfig[o.status].variant">
              {{ statusConfig[o.status].label }}
            </BaseBadge>
          </RouterLink>
        </div>
      </section>
    </template>

    <!-- ============== ADMIN ============== -->
    <template v-else-if="role === 'ADMIN'">
      <div class="page-header">
        <div>
          <h1 class="page-title">Dashboard</h1>
          <p class="page-sub">Resumen del negocio</p>
        </div>
        <BaseButton variant="dark" :loading="dashLoading" @click="loadDashboard">
          {{ dashLoading ? 'Cargando...' : 'Actualizar' }}
        </BaseButton>
      </div>

      <div v-if="dashError" class="error-banner">
        {{ dashError }}
        <button class="retry-btn" @click="loadDashboard">Reintentar</button>
      </div>

      <!-- Glass notification: Low Stock -->
      <div v-if="dashData && lowStockProducts.length > 0" class="glass-notification">
        <div class="glass-icon">!</div>
        <div class="glass-body">
          <span class="glass-title">{{ lowStockProducts.length }} producto{{ lowStockProducts.length !== 1 ? 's' : '' }} con stock bajo</span>
          <span class="glass-desc">{{ lowStockProducts.map(p => p.name).join(', ') }}</span>
        </div>
        <button class="glass-close" @click="lowStockProducts = []">×</button>
      </div>

      <div v-if="dashLoading && !dashData" class="kpi-grid">
        <SkeletonCard v-for="i in 6" :key="i" />
      </div>

      <template v-if="dashData">
        <div class="kpi-grid">
          <div class="kpi-card kpi-primary">
            <div class="kpi-head">
              <span class="kpi-label">Ventas Hoy</span>
              <span class="kpi-badge" :style="{ background: growthColor, color: '#fff' }">
                {{ growthIcon }} {{ Math.abs(dashData.salesGrowthPercent) }}%
              </span>
            </div>
            <span class="kpi-number">Bs {{ dashData.todaySales.toFixed(2) }}</span>
            <span class="kpi-compare">Ayer: Bs {{ dashData.yesterdaySales.toFixed(2) }}</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Pedidos Hoy</span>
            <span class="kpi-number">{{ dashData.todayOrders }}</span>
            <span class="kpi-compare">Entregados</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Ventas Semana</span>
            <span class="kpi-number">Bs {{ dashData.weekSales.toFixed(2) }}</span>
            <span class="kpi-compare">{{ dashData.weekOrders }} pedidos</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Ventas Mes</span>
            <span class="kpi-number">Bs {{ dashData.monthSales.toFixed(2) }}</span>
            <span class="kpi-compare">Ganancia: Bs {{ dashData.monthProfit.toFixed(2) }}</span>
          </div>
          <div class="kpi-card">
            <span class="kpi-label">Pedidos Mes</span>
            <span class="kpi-number">{{ dashData.monthOrders }}</span>
            <span class="kpi-compare">Totales entregados</span>
          </div>
          <div class="kpi-card" :class="{ 'kpi-danger': dashData.pendingOrders > 0 }">
            <span class="kpi-label">Pendientes</span>
            <span class="kpi-number">{{ dashData.pendingOrders }}</span>
            <span class="kpi-compare">{{ dashData.pendingOrders > 0 ? 'Requieren atención' : 'Todo al día' }}</span>
          </div>
        </div>

        <div class="charts-row">
          <div class="chart-card">
            <h3 class="card-heading">Tendencia 7 Días</h3>
            <div class="chart-area short">
              <canvas ref="trendChartRef"></canvas>
            </div>
          </div>
          <div class="chart-card">
            <h3 class="card-heading">Métodos de Pago</h3>
            <div class="chart-area short donut">
              <canvas ref="paymentChartRef"></canvas>
            </div>
          </div>
          <div class="chart-card">
            <h3 class="card-heading">Top 5 Productos</h3>
            <div class="product-list">
              <div v-for="(p, i) in dashData.topProducts" :key="p.productName" class="product-item">
                <span class="product-rank">{{ i + 1 }}</span>
                <div class="product-body">
                  <span class="product-name">{{ p.productName }}</span>
                  <span class="product-meta">{{ p.quantitySold }} vendidos</span>
                </div>
                <span class="product-amount">Bs {{ p.revenue.toFixed(2) }}</span>
              </div>
              <div v-if="dashData.topProducts.length === 0" class="empty-row">Sin ventas hoy</div>
            </div>
          </div>
        </div>
      </template>

      <section class="registry-section">
        <header class="registry-header">
          <h3 class="card-heading">Registro de órdenes</h3>
          <span class="registry-count">{{ registryOrders.length }} hoy</span>
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
            <span>Cajero</span>
            <span>Pago</span>
            <span>Total</span>
            <span>Estado</span>
          </div>

          <RouterLink
            v-for="o in registryOrders" :key="o.id"
            to="/orders" class="registry-row"
          >
            <span class="r-id">#{{ o.id }}</span>
            <span>{{ formatTime(o.dateTime) }}</span>
            <span>Mesa {{ o.tableNumber }}</span>
            <span>{{ o.details.length }} producto{{ o.details.length !== 1 ? 's' : '' }}</span>
            <span class="r-cashier">{{ o.userName }}</span>
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
/* ── Page ── */
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

.page-title {
  font-family: var(--font-display);
  font-size: 1.65rem;
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 0.2rem;
  letter-spacing: -0.02em;
}

.page-sub {
  color: var(--color-text-subtle);
  font-size: 0.875rem;
  margin: 0;
}

/* ── Error ── */
.error-banner {
  background: #fef2f2;
  color: #991b1b;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.retry-btn {
  background: #991b1b;
  color: #fff;
  border: none;
  padding: 0.3rem 0.75rem;
  border-radius: var(--radius-sm);
  font-size: var(--text-xs);
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
  transition: opacity var(--transition-fast);
}

.retry-btn:hover { opacity: 0.85; }

/* ── KPI Grid (Admin) ── */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
}

.kpi-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem 1.5rem;
  border: 1.5px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.kpi-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.kpi-primary {
  background: linear-gradient(135deg, var(--color-dark), #2a2a2a);
  border-color: var(--color-dark);
}

.kpi-primary .kpi-label { color: rgba(255,255,255,0.65); }
.kpi-primary .kpi-number { color: #fff; }
.kpi-primary .kpi-compare { color: rgba(255,255,255,0.5); }

.kpi-danger {
  background: #fef2f2;
  border-color: #fecaca;
}

.kpi-danger .kpi-number { color: #991b1b; }

.kpi-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kpi-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-subtle);
}

.kpi-badge {
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.15rem 0.45rem;
  border-radius: 999px;
  line-height: 1.4;
}

.kpi-number {
  font-family: var(--font-display);
  font-size: 1.65rem;
  font-weight: 800;
  color: var(--color-text);
  line-height: 1.1;
}

.kpi-compare {
  font-size: 0.75rem;
  color: var(--color-text-ghost);
}

/* ── Glass Notification ── */
.glass-notification {
  display: flex;
  align-items: center;
  gap: 0.85rem;
  padding: 0.85rem 1.1rem;
  border-radius: var(--radius-lg);
  background: rgba(224, 32, 32, 0.07);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1.5px solid rgba(224, 32, 32, 0.2);
  animation: slideDown 0.35s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-8px); }
  to { opacity: 1; transform: translateY(0); }
}

.glass-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1rem;
  flex-shrink: 0;
}

.glass-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.glass-title {
  font-weight: 700;
  font-size: 0.85rem;
  color: var(--color-primary);
}

.glass-desc {
  font-size: 0.78rem;
  color: var(--color-text-subtle);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.glass-close {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.06);
  color: var(--color-text-subtle);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  flex-shrink: 0;
  transition: background var(--transition-fast);
}

.glass-close:hover {
  background: rgba(0,0,0,0.12);
}

/* ── Charts Row ── */
.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 1rem;
}

.chart-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  border: 1.5px solid transparent;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.chart-card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
}

.card-heading {
  font-family: var(--font-display);
  font-size: 0.9rem;
  font-weight: 700;
  margin: 0 0 0.85rem;
  color: var(--color-text);
}

.chart-area {
  height: 200px;
}

.chart-area.short {
  height: 160px;
}

.chart-area.donut {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 180px;
  margin: 0 auto;
}

/* ── Top Products ── */
.product-list {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0.75rem;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.product-item:hover {
  background: var(--color-bg);
}

.product-rank {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--color-bg);
  color: var(--color-text-subtle);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: 700;
  flex-shrink: 0;
}

.product-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: 600;
  font-size: 0.85rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--color-text);
}

.product-meta {
  font-size: 0.7rem;
  color: var(--color-text-ghost);
}

.product-amount {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.85rem;
  white-space: nowrap;
  color: var(--color-text);
}

.empty-row {
  text-align: center;
  color: var(--color-text-ghost);
  padding: 2rem;
  font-size: 0.85rem;
}

/* ── Stats Group (Cashier) ── */
.stats-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.15rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  transition: box-shadow var(--transition-fast), transform var(--transition-fast);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-accent {
  background: linear-gradient(135deg, var(--color-dark), #2a2a2a);
}

.stat-accent .stat-label { color: rgba(255,255,255,0.65); }
.stat-accent .stat-value { color: #fff; }
.stat-accent .stat-sub { color: rgba(255,255,255,0.5); }

.stat-label {
  font-size: 0.7rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--color-text-subtle);
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--color-text);
  font-family: var(--font-display);
  line-height: 1.1;
}

.stat-sub {
  font-size: 0.7rem;
  color: var(--color-text-ghost);
}

/* ── Registry Table ── */
.registry-section {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  border: 1.5px solid transparent;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.registry-section:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
}

.registry-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 1rem;
}

.registry-header .card-heading { margin: 0; }
.registry-header .section-title { margin: 0; }

.registry-count {
  font-size: 0.8rem;
  color: var(--color-text-ghost);
}

.registry-table {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.registry-row {
  display: grid;
  grid-template-columns: 50px 65px 70px 1fr 1fr 80px 90px 95px;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 0.75rem;
  border-radius: var(--radius-sm);
  font-size: 0.82rem;
  color: var(--color-text);
  text-decoration: none;
  transition: background var(--transition-fast);
}

.registry-row:not(.registry-head):hover {
  background: var(--color-bg);
}

.registry-head {
  font-size: 0.65rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-ghost);
  padding-bottom: 0.45rem;
  border-bottom: 1.5px solid var(--color-border);
}

.r-id { font-weight: 700; color: var(--color-text-muted); }
.r-cashier { color: var(--color-text-muted); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.r-payment { font-size: 0.7rem; text-transform: uppercase; letter-spacing: 0.04em; color: var(--color-text-ghost); }
.r-total { font-weight: 700; color: var(--color-text); font-family: var(--font-display); }

/* ── Kitchen ── */
.kitchen-counter {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.7rem 1.1rem;
  background: rgba(224, 32, 32, 0.08);
  border-radius: var(--radius-md);
  min-width: 80px;
}

.kitchen-counter-value {
  font-size: 1.65rem;
  font-weight: 800;
  color: var(--color-primary);
  line-height: 1;
}

.kitchen-counter-label {
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--color-text-subtle);
  margin-top: 0.2rem;
}

.kitchen-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 1rem;
}

.kitchen-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04);
  transition: box-shadow var(--transition-fast);
}

.kitchen-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
}

.kitchen-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.kitchen-card-info { display: flex; flex-direction: column; gap: 0.15rem; }

.kitchen-card-number {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-text);
}

.kitchen-card-table {
  font-size: 0.8rem;
  color: var(--color-text-subtle);
}

.kitchen-time {
  font-size: 0.75rem;
  color: var(--color-text-ghost);
}

.kitchen-items {
  list-style: none;
  padding: 0.7rem 0 0;
  margin: 0;
  border-top: 1.5px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.kitchen-items li {
  display: flex;
  gap: 0.5rem;
  font-size: 0.88rem;
}

.kitchen-item-qty {
  font-weight: 700;
  color: var(--color-primary);
  min-width: 26px;
}

.kitchen-item-name { color: var(--color-text); }

.kitchen-notes {
  margin: 0;
  font-size: 0.82rem;
  color: var(--color-text-subtle);
  background: var(--color-bg);
  padding: 0.5rem 0.7rem;
  border-radius: var(--radius-sm);
}

.kitchen-actions { display: flex; gap: 0.5rem; }

.btn-ready {
  flex: 1;
  padding: 0.55rem;
  border-radius: var(--radius-sm);
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  border: 1.5px solid var(--color-info);
  color: var(--color-info);
  background: transparent;
  transition: background var(--transition-fast), color var(--transition-fast);
}

.btn-ready:hover:not(:disabled) {
  background: var(--color-info);
  color: #fff;
}

.btn-ready:disabled { opacity: 0.5; cursor: not-allowed; }

/* ── Stock Notes (Kitchen) ── */
.stock-note-section {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  border: 1.5px solid transparent;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.stock-note-section:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
}

.stock-note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.stock-note-header .section-title { margin: 0; }
.stock-note-header .section-sub { margin: 0.1rem 0 0; }

.section-title {
  font-family: var(--font-display);
  font-size: 1.05rem;
  font-weight: 700;
  color: var(--color-text);
}

.section-sub {
  font-size: 0.8rem;
  color: var(--color-text-subtle);
}

.stock-note-badge {
  background: rgba(224, 32, 32, 0.08);
  color: var(--color-primary);
  border: 1.5px solid var(--color-primary);
  border-radius: 999px;
  padding: 0.2rem 0.65rem;
  font-size: 0.72rem;
  font-weight: 700;
  white-space: nowrap;
  flex-shrink: 0;
}

.stock-note-input {
  width: 100%;
  padding: 0.7rem 0.9rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-bg);
  color: var(--color-text);
  font-size: 0.88rem;
  font-family: inherit;
  resize: vertical;
  transition: border-color var(--transition-fast);
  box-sizing: border-box;
}

.stock-note-input:focus {
  outline: none;
  border-color: var(--color-dark);
}

.stock-note-input::placeholder { color: var(--color-text-ghost); }

.stock-note-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.stock-note-hint {
  font-size: 0.72rem;
  color: var(--color-text-ghost);
}

.btn-stock-send {
  padding: 0.5rem 1.1rem;
  background: var(--color-dark);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background var(--transition-fast);
  white-space: nowrap;
}

.btn-stock-send:hover:not(:disabled) { background: #333; }
.btn-stock-send:disabled { background: var(--color-border); color: var(--color-text-ghost); cursor: not-allowed; }

.note-error {
  background: rgba(224, 32, 32, 0.06);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 0.5rem 0.8rem;
  font-size: 0.82rem;
}

.btn-new-order {
  background: var(--color-primary);
  color: #fff;
  padding: 0.6rem 1.1rem;
  border-radius: var(--radius-md);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.85rem;
  transition: filter var(--transition-fast), box-shadow var(--transition-fast);
  white-space: nowrap;
}

.btn-new-order:hover {
  filter: brightness(1.08);
  box-shadow: 0 2px 8px rgba(224, 32, 32, 0.25);
}

/* ── Responsive ── */
@media (max-width: 1024px) {
  .charts-row {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
  .registry-row {
    grid-template-columns: 1fr 1fr;
    gap: 0.3rem;
  }
  .registry-head {
    display: none;
  }
}

@media print {
  .page { padding: 0; background: #fff; }
  .kpi-card, .chart-card, .registry-section { break-inside: avoid; border-color: #ddd; box-shadow: none; }
  .glass-notification { display: none; }
  .page-header :deep(.base-button) { display: none; }
}
</style>
