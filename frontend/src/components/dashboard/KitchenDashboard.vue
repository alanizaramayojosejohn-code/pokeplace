<script setup lang="ts">
import { ref } from 'vue'

// Datos de ejemplo — luego los conectaremos con la API real
const orders = ref([
  {
    id: 1,
    table: 3,
    time: '10:30 AM',
    elapsed: '12 min',
    status: 'PENDING',
    items: [
      { name: 'Poke Bowl Salmon', quantity: 2 },
      { name: 'Poke Bowl Tuna', quantity: 1 },
    ],
  },
  {
    id: 2,
    table: 7,
    time: '10:35 AM',
    elapsed: '7 min',
    status: 'IN_PROGRESS',
    items: [
      { name: 'Poke Bowl Veggie', quantity: 1 },
      { name: 'Poke Bowl Shrimp', quantity: 1 },
    ],
  },
  {
    id: 3,
    table: 5,
    time: '10:40 AM',
    elapsed: '2 min',
    status: 'PENDING',
    items: [{ name: 'Poke Bowl Salmon', quantity: 3 }],
  },
])

function markInProgress(id: number) {
  const order = orders.value.find((o) => o.id === id)
  if (order) order.status = 'IN_PROGRESS'
}

function markReady(id: number) {
  orders.value = orders.value.filter((o) => o.id !== id)
}

const pendingCount = () => orders.value.filter((o) => o.status === 'PENDING').length
const inProgressCount = () => orders.value.filter((o) => o.status === 'IN_PROGRESS').length
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>Panel de Cocina</h1>
        <p>Administrar ordenes</p>
      </div>
      <div class="counters">
        <div class="counter counter-pending">
          <span class="counter-value">{{ pendingCount() }}</span>
          <span class="counter-label">Pendiente</span>
        </div>
        <div class="counter counter-progress">
          <span class="counter-value">{{ inProgressCount() }}</span>
          <span class="counter-label">En Progreso</span>
        </div>
      </div>
    </div>

    <!-- Tablero de pedidos -->
    <div class="orders-grid">
      <div
        v-for="order in orders"
        :key="order.id"
        :class="['order-card', order.status === 'IN_PROGRESS' ? 'order-progress' : 'order-pending']"
      >
        <div class="order-header">
          <div class="order-info">
            <span class="order-number">#{{ order.id }}</span>
            <span class="order-table">Table {{ order.table }}</span>
          </div>
          <div class="order-time">
            <span class="elapsed">⏱ {{ order.elapsed }}</span>
            <span
              :class="[
                'status-badge',
                order.status === 'IN_PROGRESS' ? 'status-progress' : 'status-pending',
              ]"
            >
              {{ order.status === 'IN_PROGRESS' ? 'In Progress' : 'Pending' }}
            </span>
          </div>
        </div>

        <!-- Items del pedido -->
        <ul class="order-items">
          <li v-for="item in order.items" :key="item.name">
            <span class="item-qty">{{ item.quantity }}x</span>
            <span class="item-name">{{ item.name }}</span>
          </li>
        </ul>

        <!-- Acciones -->
        <div class="order-actions">
          <button
            v-if="order.status === 'PENDING'"
            class="btn-progress"
            @click="markInProgress(order.id)"
          >
            Comenzar Preparación
          </button>
          <button
            v-if="order.status === 'IN_PROGRESS'"
            class="btn-ready"
            @click="markReady(order.id)"
          >
            ✓ Marcar como listo
          </button>
        </div>
      </div>

      <div v-if="orders.length === 0" class="empty-state">
        <span>🎉</span>
        <p>No hay ordenes pendientes!</p>
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

.counters {
  display: flex;
  gap: 1rem;
}

.counter {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.8rem 1.2rem;
  border-radius: 6px;
  min-width: 80px;
}

.counter-pending {
  background: #fff3cd;
}
.counter-progress {
  background: #dbeafe;
}

.counter-value {
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #111;
}

.counter-label {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #888;
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.order-card {
  background: white;
  border-radius: 6px;
  padding: 1.2rem;
  border-left: 4px solid #ddd;
}

.order-pending {
  border-left-color: #f59e0b;
}
.order-progress {
  border-left-color: #3b82f6;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.order-number {
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: #111;
}

.order-table {
  font-size: 0.85rem;
  color: #888;
}

.order-time {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 0.3rem;
}

.elapsed {
  font-size: 0.8rem;
  color: #888;
}

.status-badge {
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
  text-transform: uppercase;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}
.status-progress {
  background: #dbeafe;
  color: #1d4ed8;
}

.order-items {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem;
  border-top: 1px solid #f0f0f0;
  padding-top: 0.8rem;
}

.order-items li {
  display: flex;
  gap: 0.5rem;
  padding: 0.3rem 0;
  font-size: 0.9rem;
}

.item-qty {
  font-weight: 700;
  color: #e02020;
  min-width: 24px;
}

.item-name {
  color: #333;
}

.order-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-progress {
  flex: 1;
  padding: 0.6rem;
  background: transparent;
  border: 1.5px solid #f59e0b;
  color: #856404;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.15s;
  font-family: 'DM Sans', sans-serif;
}
.btn-progress:hover {
  background: #fff3cd;
}

.btn-ready {
  flex: 1;
  padding: 0.6rem;
  background: #22c55e;
  border: none;
  color: white;
  border-radius: 4px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.15s;
  font-family: 'DM Sans', sans-serif;
}
.btn-ready:hover {
  background: #16a34a;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 3rem;
  color: #888;
  font-size: 1.1rem;
}

.empty-state span {
  font-size: 3rem;
  display: block;
  margin-bottom: 0.5rem;
}
</style>
