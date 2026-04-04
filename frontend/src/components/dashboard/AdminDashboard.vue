<script setup lang="ts">
import { ref } from 'vue'

const salesData = ref([
  { month: 'Enero', total: 1200 },
  { month: 'Febrero', total: 1800 },
  { month: 'Marzo', total: 1400 },
  { month: 'Abril', total: 2200 },
  { month: 'Mayo', total: 1900 },
  { month: 'Junio', total: 2800 },
])

const maxSale = Math.max(...salesData.value.map((d) => d.total))

const stats = ref([
  { label: 'Total', value: 'Bs 11,300', change: '+12%' },
  { label: 'Ordenes de hoy', value: '24', change: '+4%' },
  { label: 'Usuarios activos', value: '3', change: '0%' },
  { label: 'Productos en el Menu', value: '15', change: '' },
])
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>Panel de Administrador</h1>
        <p>Estado actual del negocio</p>
      </div>
    </div>

    <div class="stats-grid">
      <div v-for="stat in stats" :key="stat.label" class="stat-card">
        <span class="stat-label">{{ stat.label }}</span>
        <span class="stat-value">{{ stat.value }}</span>
        <span v-if="stat.change" class="stat-change">{{ stat.change }}</span>
      </div>
    </div>

    <div class="chart-card">
      <h2>Vnetas Mensuales</h2>
      <div class="chart">
        <div v-for="item in salesData" :key="item.month" class="bar-group">
          <div class="bar-label-top">${{ item.total }}</div>
          <div class="bar" :style="{ height: (item.total / maxSale) * 200 + 'px' }"></div>
          <div class="bar-label">{{ item.month }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700&family=DM+Sans:wght@400;500&display=swap');

.page {
  padding: 2rem;
  font-family: 'DM Sans', sans-serif;
}

.page-header {
  margin-bottom: 1.5rem;
}
.page-header h1 {
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  color: #111;
  margin: 0 0 0.2rem;
}
.page-header p {
  color: #888;
  font-size: 0.9rem;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 6px;
  border-left: 3px solid #e02020;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.stat-label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #888;
}

.stat-value {
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  font-weight: 700;
  color: #111;
}

.stat-change {
  font-size: 0.8rem;
  color: #22c55e;
  font-weight: 500;
}

.chart-card {
  background: white;
  padding: 1.5rem;
  border-radius: 6px;
}

.chart-card h2 {
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  color: #111;
  margin: 0 0 1.5rem;
}

.chart {
  display: flex;
  align-items: flex-end;
  gap: 1rem;
  height: 250px;
  padding-bottom: 2rem;
  border-bottom: 2px solid #f0f0f0;
}

.bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 0.4rem;
}

.bar {
  width: 100%;
  background: #e02020;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
  min-height: 4px;
}

.bar-label {
  font-size: 0.8rem;
  color: #888;
  text-align: center;
}

.bar-label-top {
  font-size: 0.7rem;
  color: #111;
  font-weight: 500;
}
</style>
