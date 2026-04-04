<script setup lang="ts">
import { ref } from 'vue'

// Datos de ejemplo — luego los conectaremos con la API real
const activeOrders = ref([
  { id: 1, table: 3, items: 4, total: 45.5, status: 'Pendiente', time: '10 min ago' },
  { id: 2, table: 7, items: 2, total: 22.0, status: 'Pendiente', time: '5 min ago' },
  { id: 3, table: 1, items: 6, total: 78.0, status: 'Entregado', time: '2 min ago' },
])

const statusColors: Record<string, string> = {
  PENDING: 'status-pending',
  DELIVERED: 'status-delivered',
  CANCELLED: 'status-cancelled',
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>Panel de caja</h1>
        <p>Administra ordenes activas y pagos</p>
      </div>
      <button class="btn-primary">+ Nueva Orden</button>
    </div>

    <!-- Resumen rápido -->
    <div class="stats-grid">
      <div class="stat-card">
        <span class="stat-label">Ordenes Activas</span>
        <span class="stat-value">{{ activeOrders.length }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-label">Total de hoy</span>
        <span class="stat-value"
          >Bs {{ activeOrders.reduce((a, o) => a + o.total, 0).toFixed(2) }}</span
        >
      </div>
    </div>

    <!-- Lista de pedidos activos -->
    <div class="table-card">
      <h2>Ordenes Activas</h2>
      <table>
        <thead>
          <tr>
            <th>Orden #</th>
            <th>Mesa</th>
            <th>Productos</th>
            <th>Total</th>
            <th>Tiempo</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in activeOrders" :key="order.id">
            <td>#{{ order.id }}</td>
            <td>Table {{ order.table }}</td>
            <td>{{ order.items }} items</td>
            <td>${{ order.total.toFixed(2) }}</td>
            <td>{{ order.time }}</td>
            <td>
              <span :class="['status-badge', statusColors[order.status]]">
                {{ order.status }}
              </span>
            </td>
            <td class="actions">
              <button class="btn-action">Ver Orden</button>
            </td>
          </tr>
        </tbody>
      </table>
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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

.btn-primary {
  background: #e02020;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-primary:hover {
  background: #c01010;
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

.table-card {
  background: white;
  border-radius: 6px;
  overflow: hidden;
  padding: 1.5rem;
}

.table-card h2 {
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  color: #111;
  margin: 0 0 1rem;
}

table {
  width: 100%;
  border-collapse: collapse;
}
thead {
  background: #111;
  color: white;
}

th {
  padding: 0.9rem 1rem;
  text-align: left;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 500;
}

td {
  padding: 0.9rem 1rem;
  border-bottom: 1px solid #f0f0f0;
  font-size: 0.9rem;
  color: #333;
}

tr:last-child td {
  border-bottom: none;
}
tr:hover td {
  background: #fafafa;
}

.status-badge {
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}
.status-delivered {
  background: #d1fae5;
  color: #065f46;
}
.status-cancelled {
  background: #fee2e2;
  color: #991b1b;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn-action {
  background: transparent;
  border: 1px solid #111;
  color: #111;
  padding: 0.3rem 0.8rem;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-action:hover {
  background: #111;
  color: white;
}
</style>
