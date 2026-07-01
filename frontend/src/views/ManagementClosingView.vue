<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import Chart from 'chart.js/auto'
import { reportService } from '@/api/ReportService'
import type { MonthlyReport, YearComparison } from '@/types/report'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import { downloadBlob } from '@/utils/download'

const currentYear = new Date().getFullYear()
const selectedYear = ref<number>(currentYear)
const compareYear = ref<number | null>(null)
const data = ref<MonthlyReport[]>([])
const comparison = ref<YearComparison | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const downloading = ref<string | null>(null)

const monthNames = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
]

const totalSales = computed(() =>
  data.value.reduce((s, r) => s + r.totalSales, 0)
)
const totalProfit = computed(() =>
  data.value.reduce((s, r) => s + r.totalProfit, 0)
)
const totalOrders = computed(() =>
  data.value.reduce((s, r) => s + r.orderCount, 0)
)
const avgMargin = computed(() =>
  totalSales.value > 0 ? ((totalProfit.value / totalSales.value) * 100) : 0
)

const barChartRef = ref<HTMLCanvasElement | null>(null)
let chartInstance: Chart | null = null

const yearOptions = computed(() => {
  const years: number[] = []
  for (let i = 0; i < 10; i++) years.push(currentYear - i)
  return years
})

onMounted(() => {
  loadData()
})

watch(selectedYear, () => {
  loadData()
})

watch(compareYear, () => {
  if (compareYear.value) loadComparison()
  else comparison.value = null
})

async function loadData(): Promise<void> {
  loading.value = true
  error.value = null
  try {
    const { data: result } = await reportService.getMonthly(selectedYear.value)
    data.value = result
    await nextTick()
    renderChart()
  } catch {
    error.value = 'Error al cargar el reporte.'
  } finally {
    loading.value = false
  }
}

async function loadComparison(): Promise<void> {
  if (!compareYear.value) return
  try {
    const { data: result } = await reportService.getYearComparison(
      Math.min(selectedYear.value, compareYear.value),
      Math.max(selectedYear.value, compareYear.value)
    )
    comparison.value = result
  } catch {
    // silent
  }
}

function renderChart(): void {
  if (chartInstance) {
    chartInstance.destroy()
    chartInstance = null
  }
  if (!barChartRef.value || data.value.length === 0) return

  const labels = data.value.map(r => monthNames[r.month - 1] || '')
  chartInstance = new Chart(barChartRef.value, {
    type: 'bar',
    data: {
      labels,
      datasets: [
        {
          label: 'Ventas',
          data: data.value.map(r => r.totalSales),
          backgroundColor: 'rgba(32, 32, 32, 0.85)',
          borderRadius: 6,
          borderSkipped: false,
        },
        {
          label: 'Ganancia',
          data: data.value.map(r => r.totalProfit),
          backgroundColor: 'rgba(224, 32, 32, 0.8)',
          borderRadius: 6,
          borderSkipped: false,
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 600 },
      interaction: {
        intersect: false,
        mode: 'index'
      },
      plugins: {
        legend: {
          position: 'top',
          labels: { usePointStyle: true, padding: 16 }
        },
        tooltip: {
          backgroundColor: 'rgba(0,0,0,0.9)',
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: (ctx) => `${ctx.dataset.label}: Bs ${(ctx.raw as number).toFixed(2)}`
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: 'rgba(0,0,0,0.06)' },
          ticks: {
            callback: (value) => 'Bs ' + Number(value).toFixed(0)
          }
        },
        x: {
          grid: { display: false }
        }
      }
    }
  })
}

async function downloadExcel(): Promise<void> {
  downloading.value = 'excel'
  try {
    const response = await reportService.downloadExcel(selectedYear.value)
    downloadBlob(response.data, `cierre-gestion-${selectedYear.value}.xlsx`)
  } catch {
    error.value = 'Error al descargar Excel.'
  } finally {
    downloading.value = null
  }
}

async function downloadPdf(): Promise<void> {
  downloading.value = 'pdf'
  try {
    const response = await reportService.downloadPdf(selectedYear.value)
    downloadBlob(response.data, `cierre-gestion-${selectedYear.value}.pdf`)
  } catch {
    error.value = 'Error al descargar PDF.'
  } finally {
    downloading.value = null
  }
}
</script>

<template>
  <div class="page">
    <PageHeader title="Cierre de Gestión" subtitle="Reporte mensual de ventas y ganancias">
      <template #actions>
        <div class="filters">
          <select v-model.number="selectedYear" class="filter-input">
            <option v-for="y in yearOptions" :key="y" :value="y">{{ y }}</option>
          </select>
          <select v-model.number="compareYear" class="filter-input">
            <option :value="null">Sin comparar</option>
            <option v-for="y in yearOptions.filter(y => y !== selectedYear)" :key="y" :value="y">
              vs {{ y }}
            </option>
          </select>
          <BaseButton variant="dark" :loading="loading" @click="loadData">
            Cargar
          </BaseButton>
        </div>
      </template>
    </PageHeader>

    <LoadingState v-if="loading && data.length === 0" />

    <EmptyState
      v-else-if="!loading && data.length === 0"
      icon="📊"
      title="Sin datos"
      message="No hay ventas registradas en este año."
    />

    <template v-if="data.length > 0">
      <div class="stats-row">
        <div class="stat-card">
          <span class="stat-label">Total Ventas</span>
          <span class="stat-value">Bs {{ totalSales.toFixed(2) }}</span>
          <span v-if="comparison" class="stat-growth" :class="comparison.salesGrowthPercent >= 0 ? 'positive' : 'negative'">
            {{ comparison.salesGrowthPercent >= 0 ? '↑' : '↓' }} {{ Math.abs(comparison.salesGrowthPercent) }}% vs {{ comparison.year1 }}
          </span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Ganancia Total</span>
          <span class="stat-value profit">Bs {{ totalProfit.toFixed(2) }}</span>
          <span v-if="comparison" class="stat-growth" :class="comparison.profitGrowthPercent >= 0 ? 'positive' : 'negative'">
            {{ comparison.profitGrowthPercent >= 0 ? '↑' : '↓' }} {{ Math.abs(comparison.profitGrowthPercent) }}% vs {{ comparison.year1 }}
          </span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Pedidos</span>
          <span class="stat-value">{{ totalOrders }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Margen Promedio</span>
          <span class="stat-value">{{ avgMargin.toFixed(1) }}%</span>
          <div class="margin-bar">
            <div class="margin-fill" :style="{ width: Math.min(avgMargin, 100) + '%' }"></div>
          </div>
        </div>
      </div>

      <div class="card chart-card">
        <h3 class="card-title">Ventas vs Ganancia por Mes</h3>
        <div class="chart-wrapper">
          <canvas ref="barChartRef"></canvas>
        </div>
      </div>

      <div class="card">
        <div class="table-header">
          <h3 class="card-title">Detalle Mensual</h3>
          <div class="export-buttons">
            <BaseButton variant="secondary" size="sm" :loading="downloading === 'excel'" @click="downloadExcel">
              Excel
            </BaseButton>
            <BaseButton variant="secondary" size="sm" :loading="downloading === 'pdf'" @click="downloadPdf">
              PDF
            </BaseButton>
          </div>
        </div>
        <div class="table-scroll">
          <table class="table">
            <thead>
              <tr>
                <th>Mes</th>
                <th>Pedidos</th>
                <th>Ventas</th>
                <th>Ganancia</th>
                <th>Margen</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in data" :key="row.month">
                <td class="month-name">{{ monthNames[row.month - 1] || row.month }}</td>
                <td>{{ row.orderCount }}</td>
                <td class="amount">Bs {{ row.totalSales.toFixed(2) }}</td>
                <td class="amount profit">Bs {{ row.totalProfit.toFixed(2) }}</td>
                <td class="amount">
                  {{ row.totalSales > 0 ? ((row.totalProfit / row.totalSales) * 100).toFixed(1) : '0.0' }}%
                </td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="total-row">
                <td class="month-name">TOTAL</td>
                <td>{{ totalOrders }}</td>
                <td class="amount">Bs {{ totalSales.toFixed(2) }}</td>
                <td class="amount profit">Bs {{ totalProfit.toFixed(2) }}</td>
                <td class="amount">{{ avgMargin.toFixed(1) }}%</td>
              </tr>
            </tfoot>
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
  gap: 0.75rem;
  flex-wrap: wrap;
}

.filter-input {
  padding: 0.5rem 0.75rem;
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
  animation: fadeInUp 0.4s ease-out;
}

.stat-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1rem 1.25rem;
  border: 1.5px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  transition: all var(--transition-fast);
}

.stat-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--color-dark);
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--color-text-subtle);
}

.stat-value {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text);
}

.stat-value.profit {
  color: var(--color-primary);
}

.stat-growth {
  font-size: var(--text-xs);
  font-weight: 600;
}

.stat-growth.positive { color: #16a34a; }
.stat-growth.negative { color: #dc2626; }

.margin-bar {
  height: 6px;
  background: var(--color-border);
  border-radius: 99px;
  overflow: hidden;
  margin-top: 0.2rem;
}

.margin-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 99px;
  transition: width 0.6s ease;
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

.chart-card .chart-wrapper {
  height: 200px;
}

.table-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.table-header .card-title {
  margin: 0;
}

.export-buttons {
  display: flex;
  gap: 0.5rem;
}

.table-scroll {
  overflow-x: auto;
}

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

.table tfoot .total-row {
  font-weight: 700;
  border-top: 2px solid var(--color-text);
}

.month-name {
  font-weight: 600;
}

.amount {
  font-family: var(--font-display);
}

.amount.profit {
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: stretch;
  }
  .stats-row {
    grid-template-columns: 1fr 1fr;
  }
  .table-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
