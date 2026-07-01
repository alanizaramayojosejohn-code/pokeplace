<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Chart from 'chart.js/auto'
import { reportService } from '@/api/ReportService'
import type {
  SalesReportSummary,
  CategoryReport,
  SalesOrder
} from '@/types/report'
import { useUsersStore } from '@/stores/user'
import { useClientsStore } from '@/stores/clients'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseBadge from '@/components/ui/BaseBadge.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import SearchBar from '@/components/ui/SearchBar.vue'
import AlertBanner from '@/components/ui/AlertBanner.vue'
import { downloadBlob } from '@/utils/download'

const usersStore = useUsersStore()
const clientsStore = useClientsStore()
const route = useRoute()
const router = useRouter()

const today = new Date().toISOString().split('T')[0] || ''
const defaultStart = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
  .toISOString().split('T')[0] || ''
const startDate = ref<string>((route.query.start as string) || defaultStart)
const endDate = ref<string>((route.query.end as string) || today)
const summary = ref<SalesReportSummary | null>(null)
const orders = ref<SalesOrder[]>([])
const categorySales = ref<CategoryReport[]>([])
const loading = ref(false)
const downloading = ref<string | null>(null)
const error = ref<string | null>(null)
const downloadError = ref<string | null>(null)
const searchOrder = ref('')

const paymentChart = ref<HTMLCanvasElement | null>(null)
const trendChart = ref<HTMLCanvasElement | null>(null)
let paymentChartInstance: Chart | null = null
let trendChartInstance: Chart | null = null

const filteredOrders = computed(() => {
  if (!searchOrder.value.trim()) return orders.value
  const q = searchOrder.value.toLowerCase()
  return orders.value.filter(o =>
    `${o.id} ${o.clientName ?? ''} ${o.userName ?? ''} ${o.paymentMethod}`
      .toLowerCase().includes(q)
  )
})

onMounted(async () => {
  await Promise.all([
    usersStore.users.length === 0 ? usersStore.fetchUsers() : Promise.resolve(),
    clientsStore.items.length === 0 ? clientsStore.fetchAll() : Promise.resolve(),
  ])
  loadReport()
})

onUnmounted(() => {
  paymentChartInstance?.destroy()
  trendChartInstance?.destroy()
})

watch([startDate, endDate], () => {
  router.replace({ query: { start: startDate.value, end: endDate.value } })
  loadReport()
})

async function loadReport(): Promise<void> {
  if (startDate.value > endDate.value) {
    error.value = 'La fecha de inicio no puede ser mayor a la fecha final.'
    return
  }
  loading.value = true
  error.value = null
  downloadError.value = null
  try {
    const [summaryRes, catRes, ordersRes] = await Promise.all([
      reportService.getSummary(startDate.value, endDate.value),
      reportService.getSalesByCategory(startDate.value, endDate.value),
      reportService.getSalesOrders(startDate.value, endDate.value),
    ])
    summary.value = summaryRes.data
    categorySales.value = catRes.data
    orders.value = ordersRes.data
    await nextTick()
    renderCharts()
  } catch {
    error.value = 'Error al cargar el reporte. Intente de nuevo.'
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  renderPaymentChart()
  renderTrendChart()
}

function renderPaymentChart() {
  paymentChartInstance?.destroy()
  if (!paymentChart.value || !summary.value) return
  paymentChartInstance = new Chart(paymentChart.value, {
    type: 'doughnut',
    data: {
      labels: summary.value.salesByPaymentMethod.map(m => m.paymentMethod),
      datasets: [{
        data: summary.value.salesByPaymentMethod.map(m => m.total),
        backgroundColor: ['#1a1a1a', '#E02020', '#555', '#999', '#ccc'],
        borderWidth: 0,
      }]
    },
    options: {
      responsive: true,
      cutout: '68%',
      plugins: {
        legend: { position: 'bottom', labels: { usePointStyle: true, padding: 10, boxWidth: 8, font: { size: 10 } } }
      }
    }
  })
}

function renderTrendChart() {
  trendChartInstance?.destroy()
  if (!trendChart.value || !summary.value?.dailySales.length) return

  const days = summary.value.dailySales
  const labels = days.map(d => {
    const p = d.date.split('-')
    return `${p[2]}/${p[1]}`
  })

  trendChartInstance = new Chart(trendChart.value, {
    type: 'line',
    data: {
      labels,
      datasets: [{
        label: 'Ventas',
        data: days.map(d => d.totalSales),
        borderColor: '#1a1a1a',
        backgroundColor: (ctx) => {
          const g = ctx.chart.ctx.createLinearGradient(0, 0, 0, 150)
          g.addColorStop(0, 'rgba(26, 26, 26, 0.1)')
          g.addColorStop(1, 'rgba(26, 26, 26, 0)')
          return g
        },
        fill: true,
        tension: 0.4,
        pointRadius: 2,
        pointHoverRadius: 4,
        borderWidth: 1.5,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 500 },
      interaction: { intersect: false, mode: 'index' },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(0,0,0,0.9)',
          padding: 8,
          cornerRadius: 6,
          callbacks: {
            label: (ctx) => `Bs ${(ctx.raw as number).toFixed(2)}`
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: 'rgba(0,0,0,0.05)' },
          ticks: {
            font: { size: 10 },
            callback: (value) => 'Bs ' + Number(value).toFixed(0)
          }
        },
        x: {
          grid: { display: false },
          ticks: { font: { size: 10 } }
        }
      }
    }
  })
}

async function downloadExcel() {
  downloading.value = 'excel'
  downloadError.value = null
  try {
    const res = await reportService.downloadSummaryExcel(startDate.value, endDate.value)
    downloadBlob(res.data, `reporte-ventas-${startDate.value}-${endDate.value}.xlsx`)
  } catch {
    downloadError.value = 'Error al descargar Excel'
  } finally {
    downloading.value = null
  }
}

async function downloadPdf() {
  downloading.value = 'pdf'
  downloadError.value = null
  try {
    const res = await reportService.downloadSummaryPdf(startDate.value, endDate.value)
    downloadBlob(res.data, `reporte-ventas-${startDate.value}-${endDate.value}.pdf`)
  } catch {
    downloadError.value = 'Error al descargar PDF'
  } finally {
    downloading.value = null
  }
}

function formatDateTime(d: string) {
  return new Date(d).toLocaleString('es-BO')
}
</script>

<template>
  <div class="page">
    <PageHeader
      title="Reporte de Ventas"
      :subtitle="summary ? `${summary.totalOrders} pedido${summary.totalOrders !== 1 ? 's' : ''} entregado${summary.totalOrders !== 1 ? 's' : ''}` : 'Seleccione un rango para generar'
    ">
      <template #actions>
        <div class="filters">
          <input type="date" v-model="startDate" class="filter-input" />
          <input type="date" v-model="endDate" class="filter-input" />
          <BaseButton variant="dark" :loading="loading" @click="loadReport">
            Generar
          </BaseButton>
          <BaseButton v-if="summary" variant="secondary" size="sm" :loading="downloading === 'excel'" @click="downloadExcel">
            Excel
          </BaseButton>
          <BaseButton v-if="summary" variant="secondary" size="sm" :loading="downloading === 'pdf'" @click="downloadPdf">
            PDF
          </BaseButton>
        </div>
      </template>
    </PageHeader>

    <AlertBanner v-if="error" variant="danger" :message="error" />
    <AlertBanner v-if="downloadError" variant="danger" :message="downloadError" />

    <LoadingState v-if="loading && !summary" />

    <EmptyState
      v-else-if="!summary"
      title="Sin reporte"
      message="Seleccione un rango de fechas y haga clic en Generar Reporte."
    />

    <template v-else>
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-label">Total Período</span>
          <span class="stat-value">Bs {{ summary.periodTotal.toFixed(2) }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Pedidos</span>
          <span class="stat-value">{{ summary.totalOrders }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Promedio x Pedido</span>
          <span class="stat-value">Bs {{ summary.totalOrders > 0 ? (summary.periodTotal / summary.totalOrders).toFixed(2) : '0.00' }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Productos Vendidos</span>
          <span class="stat-value">{{ summary.bestSellingProducts.reduce((s, p) => s + p.quantitySold, 0) }}</span>
        </div>
      </div>

      <div class="charts-row">
        <div class="card chart-wide">
          <h3 class="card-title">Tendencia Diaria</h3>
          <div class="chart-box">
            <canvas ref="trendChart"></canvas>
          </div>
        </div>
        <div class="card chart-narrow">
          <h3 class="card-title">Métodos de Pago</h3>
          <div class="chart-box-sm">
            <canvas ref="paymentChart"></canvas>
          </div>
        </div>
      </div>

      <div class="card">
        <h3 class="card-title">Ventas</h3>
        <SearchBar v-model="searchOrder" placeholder="Buscar por ID, cliente, empleado, método de pago..." />
        <div class="table-scroll">
          <table class="table">
            <thead>
              <tr>
                <th>#</th>
                <th>Fecha</th>
                <th>Mesa</th>
                <th>Cliente</th>
                <th>Empleado</th>
                <th>Pago</th>
                <th>Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="o in filteredOrders" :key="o.id">
                <td class="td-id">{{ o.id }}</td>
                <td class="td-date">{{ formatDateTime(o.dateTime) }}</td>
                <td>{{ o.tableNumber === 0 ? 'Para llevar' : o.tableNumber }}</td>
                <td>{{ o.clientName ?? '—' }}</td>
                <td>{{ o.userName ?? '—' }}</td>
                <td><BaseBadge variant="dark">{{ o.paymentMethod }}</BaseBadge></td>
                <td class="td-total">Bs {{ o.total.toFixed(2) }}</td>
              </tr>
              <tr v-if="filteredOrders.length === 0">
                <td colspan="7" class="empty-row">Sin ventas en este período.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="compact-grid">
        <div class="card">
          <h3 class="card-title">Productos</h3>
          <table class="table compact">
            <thead>
              <tr>
                <th>Producto</th>
                <th class="col-num">Cant.</th>
                <th class="col-num">Bs</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in summary.bestSellingProducts" :key="p.productName">
                <td class="td-product">{{ p.productName }}</td>
                <td class="col-num">{{ p.quantitySold }}</td>
                <td class="col-num td-total">{{ p.revenue.toFixed(2) }}</td>
              </tr>
              <tr v-if="summary.bestSellingProducts.length === 0">
                <td colspan="3" class="empty-row">Sin productos.</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="card">
          <h3 class="card-title">Categorías</h3>
          <table class="table compact">
            <thead>
              <tr>
                <th>Categoría</th>
                <th class="col-num">Cant.</th>
                <th class="col-num">Bs</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in categorySales" :key="c.categoryName">
                <td class="td-product">{{ c.categoryName }}</td>
                <td class="col-num">{{ c.quantitySold }}</td>
                <td class="col-num td-total">{{ c.revenue.toFixed(2) }}</td>
              </tr>
              <tr v-if="categorySales.length === 0">
                <td colspan="3" class="empty-row">Sin datos.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
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

.filters {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.filter-input {
  padding: 0.45rem 0.7rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-family: var(--font-body);
  transition: border-color var(--transition-fast);
}

.filter-input:focus {
  outline: none;
  border-color: var(--color-dark);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 0.75rem;
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1rem 1.25rem;
  border: 1.5px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  transition: all var(--transition-fast);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--color-dark);
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--color-text-subtle);
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text);
}

.charts-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 0.75rem;
}

.card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  border: 1.5px solid transparent;
  transition: all var(--transition-fast);
}

.card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
}

.card-title {
  font-family: var(--font-display);
  font-size: var(--text-sm);
  font-weight: 700;
  margin: 0 0 0.75rem;
}

.chart-box {
  height: 160px;
}

.chart-box-sm {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-box-sm canvas {
  max-width: 140px;
  max-height: 140px;
}

.table-scroll {
  overflow-x: auto;
  margin-top: 0.5rem;
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
}

.table.compact {
  font-size: var(--text-xs);
}

.table.compact td,
.table.compact th {
  padding: 0.4rem 0.5rem;
}

.table thead tr {
  border-bottom: 2px solid var(--color-border);
}

.table th {
  text-align: left;
  padding: 0.5rem 0.75rem;
  color: var(--color-text-subtle);
  font-weight: 600;
  font-size: var(--text-xs);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.table tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition-fast);
}

.table tbody tr:hover {
  background: var(--color-bg);
}

.table td {
  padding: 0.6rem 0.75rem;
}

.col-num {
  text-align: right;
  white-space: nowrap;
}

.td-id {
  color: var(--color-text-ghost);
  font-size: var(--text-xs);
}

.td-date {
  font-size: var(--text-xs);
  white-space: nowrap;
}

.td-product {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.td-total {
  font-weight: 700;
  font-family: var(--font-display);
  white-space: nowrap;
}

.empty-row {
  text-align: center;
  color: var(--color-text-subtle);
  padding: 1.5rem;
  font-size: var(--text-xs);
}

.compact-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

@media (max-width: 768px) {
  .charts-row {
    grid-template-columns: 1fr;
  }

  .compact-grid {
    grid-template-columns: 1fr;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
