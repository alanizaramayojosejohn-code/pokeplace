<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import Chart from 'chart.js/auto'
import { reportService } from '@/api/ReportService'
import type { DashboardReport } from '@/types/report'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import SkeletonCard from '@/components/ui/SkeletonCard.vue'

const data = ref<DashboardReport | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const autoRefresh = ref(false)
let refreshInterval: ReturnType<typeof setInterval> | null = null

const trendChartRef = ref<HTMLCanvasElement | null>(null)
let trendChartInstance: Chart | null = null

const growthIcon = computed(() => {
  if (!data.value) return ''
  const g = data.value.salesGrowthPercent
  if (g > 0) return '↑'
  if (g < 0) return '↓'
  return '→'
})

const growthColor = computed(() => {
  if (!data.value) return ''
  const g = data.value.salesGrowthPercent
  if (g > 0) return '#16a34a'
  if (g < 0) return '#dc2626'
  return '#6b7280'
})

onMounted(() => {
  loadDashboard()
})

onUnmounted(() => {
  trendChartInstance?.destroy()
  stopAutoRefresh()
})

watch(autoRefresh, (val) => {
  if (val) startAutoRefresh()
  else stopAutoRefresh()
})

function startAutoRefresh() {
  stopAutoRefresh()
  refreshInterval = setInterval(loadDashboard, 30000)
}

function stopAutoRefresh() {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

async function loadDashboard() {
  loading.value = true
  error.value = null
  try {
    const res = await reportService.getDashboard()
    data.value = res.data
    await nextTick()
    renderTrendChart()
  } catch {
    error.value = 'Error al cargar el dashboard.'
  } finally {
    loading.value = false
  }
}

function renderTrendChart() {
  trendChartInstance?.destroy()
  if (!trendChartRef.value || !data.value?.weeklyTrend.length) return

  const trend = data.value.weeklyTrend
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
          const gradient = ctx.chart.ctx.createLinearGradient(0, 0, 0, 300)
          gradient.addColorStop(0, 'rgba(26, 26, 26, 0.15)')
          gradient.addColorStop(1, 'rgba(26, 26, 26, 0)')
          return gradient
        },
        fill: true,
        tension: 0.4,
        pointRadius: 4,
        pointHoverRadius: 6,
        borderWidth: 2.5,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: { duration: 800 },
      interaction: { intersect: false, mode: 'index' },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(0,0,0,0.9)',
          padding: 12,
          cornerRadius: 8,
          callbacks: {
            label: (ctx) => `$${(ctx.raw as number).toFixed(2)}`
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: 'rgba(0,0,0,0.06)' },
          ticks: {
            callback: (value) => '$' + Number(value).toFixed(0)
          }
        },
        x: {
          grid: { display: false }
        }
      }
    }
  })
}
</script>

<template>
  <div class="page">
    <PageHeader title="Dashboard Ejecutivo" subtitle="Resumen rápido del negocio">
      <template #actions>
        <label class="auto-refresh-label">
          <input type="checkbox" v-model="autoRefresh" class="auto-refresh-check" />
          Auto-refresh
        </label>
        <BaseButton variant="dark" :loading="loading" @click="loadDashboard">
          {{ loading ? 'Cargando...' : 'Actualizar' }}
        </BaseButton>
      </template>
    </PageHeader>

    <div v-if="error" class="error-banner">
      {{ error }}
      <button class="retry-btn" @click="loadDashboard">Reintentar</button>
    </div>
    <div v-if="loading && !data" class="kpi-grid">
      <SkeletonCard v-for="i in 6" :key="i" />
    </div>

    <template v-if="data">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-top">
            <span class="kpi-label">Ventas Hoy</span>
            <span class="kpi-badge" :style="{ background: growthColor, color: '#fff' }">
              {{ growthIcon }} {{ Math.abs(data.salesGrowthPercent) }}%
            </span>
          </div>
          <span class="kpi-value">${{ data.todaySales.toFixed(2) }}</span>
          <span class="kpi-sub">Ayer: ${{ data.yesterdaySales.toFixed(2) }}</span>
        </div>

        <div class="kpi-card">
          <span class="kpi-label">Pedidos Hoy</span>
          <span class="kpi-value">{{ data.todayOrders }}</span>
          <span class="kpi-sub">Entregados</span>
        </div>

        <div class="kpi-card">
          <span class="kpi-label">Ventas Semana</span>
          <span class="kpi-value">${{ data.weekSales.toFixed(2) }}</span>
          <span class="kpi-sub">{{ data.weekOrders }} pedidos</span>
        </div>

        <div class="kpi-card">
          <span class="kpi-label">Ventas Mes</span>
          <span class="kpi-value">${{ data.monthSales.toFixed(2) }}</span>
          <span class="kpi-sub">Ganancia: ${{ data.monthProfit.toFixed(2) }}</span>
        </div>

        <div class="kpi-card">
          <span class="kpi-label">Pedidos Mes</span>
          <span class="kpi-value">{{ data.monthOrders }}</span>
          <span class="kpi-sub">Totales entregados</span>
        </div>

        <div class="kpi-card" :class="{ 'kpi-alert': data.pendingOrders > 0 }">
          <span class="kpi-label">Pendientes</span>
          <span class="kpi-value">{{ data.pendingOrders }}</span>
          <span class="kpi-sub" v-if="data.pendingOrders > 0">Requieren atención</span>
          <span class="kpi-sub" v-else>Todo al día</span>
        </div>
      </div>

      <div class="charts-grid">
        <div class="card chart-card-wide">
          <h3 class="card-title">Tendencia Últimos 7 Días</h3>
          <div class="chart-wrapper">
            <canvas ref="trendChartRef"></canvas>
          </div>
        </div>

        <div class="card">
          <h3 class="card-title">Top 5 Productos</h3>
          <div class="product-list">
            <div v-for="(p, i) in data.topProducts" :key="p.productName" class="product-row">
              <div class="product-rank">{{ i + 1 }}</div>
              <div class="product-info">
                <span class="product-name">{{ p.productName }}</span>
                <span class="product-meta">{{ p.quantitySold }} vendidos</span>
              </div>
              <span class="product-revenue">${{ p.revenue.toFixed(2) }}</span>
            </div>
            <div v-if="data.topProducts.length === 0" class="empty-row">
              Sin ventas hoy
            </div>
          </div>
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

.error-banner {
  background: #fef2f2;
  color: #991b1b;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  border: 1px solid #fecaca;
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

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1rem;
  animation: fadeInUp 0.4s ease-out;
}

.kpi-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem 1.5rem;
  border: 1.5px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  transition: all var(--transition-fast);
}

.kpi-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--color-dark);
}

.kpi-card.kpi-alert {
  border-color: #fecaca;
  background: #fef2f2;
}

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.kpi-label {
  font-size: var(--text-sm);
  color: var(--color-text-subtle);
}

.kpi-badge {
  font-size: var(--text-xs);
  font-weight: 700;
  padding: 0.15rem 0.5rem;
  border-radius: 999px;
}

.kpi-value {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 800;
  color: var(--color-text);
}

.kpi-sub {
  font-size: var(--text-xs);
  color: var(--color-text-subtle);
}

.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1rem;
}

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

.chart-wrapper {
  height: 280px;
}

.product-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.product-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.65rem 0.75rem;
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
}

.product-row:hover {
  background: var(--color-bg);
}

.product-rank {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-dark);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-xs);
  font-weight: 700;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-weight: 600;
  font-size: var(--text-sm);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-meta {
  font-size: var(--text-xs);
  color: var(--color-text-subtle);
}

.product-revenue {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: var(--text-sm);
  white-space: nowrap;
}

.empty-row {
  text-align: center;
  color: var(--color-text-subtle);
  padding: 2rem;
  font-size: var(--text-sm);
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.auto-refresh-label {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: var(--text-sm);
  color: var(--color-text-subtle);
  cursor: pointer;
  user-select: none;
}

.auto-refresh-check {
  accent-color: var(--color-dark);
}

@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media print {
  .page { padding: 0; background: #fff; }
  .kpi-card { break-inside: avoid; border-color: #ddd; }
  .card { break-inside: avoid; border-color: #ddd; box-shadow: none; }
  .auto-refresh-label { display: none; }
  .page-header :deep(.base-button) { display: none; }
}
</style>
