<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'  // ← nextTick aquí arriba
import Chart from 'chart.js/auto'
import { reportService } from '@/api/ReportService'
import type { SalesReportSummary } from '@/types/report'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'

const startDate = ref<string>(
  new Date(new Date().getFullYear(), new Date().getMonth(), 1)
    .toISOString().split('T')[0] || ''
)
const endDate = ref<string>(new Date().toISOString().split('T')[0] || '')
const summary = ref<SalesReportSummary | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

const paymentChart = ref<HTMLCanvasElement | null>(null)
const chartsSection = ref<HTMLDivElement | null>(null)
let paymentChartInstance: Chart | null = null

onMounted(() => {
  loadReport()
})

async function loadReport(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    const { data } = await reportService.getSummary(startDate.value, endDate.value)
    summary.value = data
    await nextTick()
    if (!paymentChartInstance) {
      renderPaymentChart()
    }
    // Scroll hacia la tabla después de cargar los datos
    chartsSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  } catch {
    error.value = 'Failed to load report. Please try again.'
  } finally {
    loading.value = false
  }
}

function renderPaymentChart(): void {
  if (paymentChartInstance) paymentChartInstance.destroy()
  if (!paymentChart.value || !summary.value) return
  paymentChartInstance = new Chart(paymentChart.value, {
    type: 'doughnut',
    data: {
      labels: summary.value.salesByPaymentMethod.map(m => m.paymentMethod),
      datasets: [{
        data: summary.value.salesByPaymentMethod.map(m => m.total),
        backgroundColor: ['#2563eb', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6', '#14b8a6'],
        borderColor: '#ffffff',
        borderWidth: 2,
        hoverOffset: 10,
      }]
    },
    options: {
      responsive: true,
      plugins: { legend: { position: 'bottom' } }
    }
  })
}
</script>
<template>
  <div class="page">
    <PageHeader
      title="Sales Reports"
      :subtitle="summary ? `${summary.totalOrders} delivered order${summary.totalOrders !== 1 ? 's' : ''}` : 'Select a range to generate'"
    >
      <template #actions>
        <div class="filters">
          <input type="date" v-model="startDate" class="date-input" />
          <input type="date" v-model="endDate" class="date-input" />
          <BaseButton variant="dark" :loading="loading" @click="loadReport">
            Generate Report
          </BaseButton>
        </div>
      </template>
    </PageHeader>

    <LoadingState v-if="loading && !summary" />

    <EmptyState
      v-else-if="!summary"
      icon="📊"
      title="No report yet"
      message="Select a date range and click Generate Report."
    />

    <template v-else>
      <!-- Summary Cards -->
      <div class="stats-grid">
        <div class="stat-card">
          <span class="stat-label">Total</span>
          <span class="stat-value">Bs {{ summary.periodTotal.toFixed(2) }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Ordenes Entregadas</span>
          <span class="stat-value">{{ summary.totalOrders }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Promedio por orden</span>
          <span class="stat-value">
            Bs {{ summary.totalOrders > 0
              ? (summary.periodTotal / summary.totalOrders).toFixed(2)
              : '0.00' }}
          </span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Productos vendidos</span>
          <span class="stat-value">
            {{ summary.bestSellingProducts.reduce((s, p) => s + p.quantitySold, 0) }}
          </span>
        </div>
      </div>

      <!-- Charts Row -->
<div class="charts-grid" ref="chartsSection">
  <div class="card">
    <h3 class="card-title">Productos vendidos por Rango de Fechas</h3>
    <table class="table">
      <thead>
        <tr>
          <th>#</th>
          <th>Product</th>
          <th>Qty Sold</th>
          <th>Revenue</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(p, i) in summary.bestSellingProducts"
          :key="p.productName"
        >
          <td class="rank">{{ i + 1 }}</td>
          <td class="product-name">{{ p.productName }}</td>
          <td>{{ p.quantitySold }}</td>
          <td class="revenue">Bs {{ p.revenue.toFixed(2) }}</td>
        </tr>
        <tr v-if="summary.bestSellingProducts.length === 0">
          <td colspan="4" class="empty-row">No products found in this period.</td>
        </tr>
      </tbody>
    </table>
  </div>

  <div class="card">
    <h3 class="card-title">Ventas por metodo de Pago</h3>
    <canvas ref="paymentChart"></canvas>
  </div>
</div>

      <!-- Best Selling Products -->
      <div class="card">
        <h3 class="card-title">Productos mas vendidos</h3>
        <table class="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Product</th>
              <th>Qty Sold</th>
              <th>Revenue</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(p, i) in summary.bestSellingProducts" :key="p.productName">
              <td class="rank">{{ i + 1 }}</td>
              <td class="product-name">{{ p.productName }}</td>
              <td>{{ p.quantitySold }}</td>
              <td class="revenue">Bs {{ p.revenue.toFixed(2) }}</td>
            </tr>
          </tbody>
        </table>
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

/* Filters */
.filters {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.date-input {
  padding: 0.5rem 0.75rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-family: var(--font-body);
  transition: border-color var(--transition-fast);
}

.date-input:focus {
  outline: none;
  border-color: var(--color-dark);
}

/* Summary Cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem 1.5rem;
  border: 1.5px solid transparent;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
  transition: all var(--transition-fast);
}

.stat-card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--color-text-subtle);
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  color: var(--color-text);
}

/* Charts */
.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1rem;
}

/* Generic Card */
.card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  border: 1.5px solid transparent;
  transition: all var(--transition-fast);
}

.card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
}

.card-title {
  font-family: var(--font-display);
  font-size: var(--text-md);
  font-weight: 700;
  margin: 0 0 1.25rem;
}

/* Table */
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
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
  padding: 0.75rem;
}

.rank {
  color: var(--color-text-subtle);
  font-weight: 700;
  width: 2rem;
}

.product-name {
  font-weight: 600;
}
.empty-row {
  text-align: center;
  color: var(--color-text-subtle);
  padding: 2rem;
  font-size: var(--text-sm);
}
.revenue {
  font-weight: 700;
  font-family: var(--font-display);
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
  .filters {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>