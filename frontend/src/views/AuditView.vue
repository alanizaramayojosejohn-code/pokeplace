<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios'

interface AuditEntry {
  entityId: number
  entityType: 'ORDER' | 'USER'   // lo asignamos nosotros al mergear
  createdBy: string | null
  updatedBy: string | null
  createdAt: string | null
  updatedAt: string | null
}

const logs = ref<AuditEntry[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const searchQuery = ref('')
const filterType = ref('ALL')
const filterUser = ref('')
const startDate = ref(
  new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0]
)
const endDate = ref(new Date().toISOString().split('T')[0])

const showDrawer = ref(false)
const selectedLog = ref<AuditEntry | null>(null)

onMounted(() => fetchLogs())

async function fetchLogs() {
  loading.value = true
  error.value = null
  try {
    // Siempre cargamos ambos y mergeamos
    const [ordersRes, usersRes] = await Promise.all([
      api.get('/audit/orders'),
      api.get('/audit/users'),
    ])

    const orders: AuditEntry[] = ordersRes.data.map((o: any) => ({
      ...o,
      entityType: 'ORDER' as const,
    }))
    const users: AuditEntry[] = usersRes.data.map((u: any) => ({
      ...u,
      entityType: 'USER' as const,
    }))

    logs.value = [...orders, ...users]
  } catch {
    error.value = 'Failed to load audit logs.'
  } finally {
    loading.value = false
  }
}

// Todo el filtrado ocurre aquí, sin llamadas extra al backend
const filtered = computed(() => {
  const start = startDate.value ? new Date(startDate.value) : null
  // endDate al final del día para incluir registros de ese día
  const end = endDate.value ? new Date(endDate.value + 'T23:59:59') : null
  const userFilter = filterUser.value.trim().toLowerCase()
  const search = searchQuery.value.toLowerCase()

  return logs.value.filter(log => {
    // Filtro por tipo
    if (filterType.value !== 'ALL' && log.entityType !== filterType.value) return false

    // Filtro por usuario (createdBy O updatedBy)
    if (userFilter) {
      const matchCreated = log.createdBy?.toLowerCase().includes(userFilter) ?? false
      const matchUpdated = log.updatedBy?.toLowerCase().includes(userFilter) ?? false
      if (!matchCreated && !matchUpdated) return false
    }

    // Filtro por rango de fechas (usa createdAt como referencia)
    if (start || end) {
      const ref = log.createdAt ? new Date(log.createdAt) : null
      if (!ref) return false
      if (start && ref < start) return false
      if (end && ref > end) return false
    }

    // Búsqueda libre
    if (search) {
      const haystack = `${log.createdBy ?? ''} ${log.updatedBy ?? ''} ${log.entityType} ${log.entityId}`.toLowerCase()
      if (!haystack.includes(search)) return false
    }

    return true
  })
})

function openDetail(log: AuditEntry) {
  selectedLog.value = log
  showDrawer.value = true
}
function closeDrawer() {
  showDrawer.value = false
  selectedLog.value = null
}
function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString()
}

const typeConfig: Record<string, { label: string; class: string }> = {
  ORDER: { label: 'Order', class: 'type-order' },
  USER:  { label: 'User',  class: 'type-user'  },
}
</script>

<template>
  <div class="page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1>Auditoría</h1>
        <p>{{ filtered.length }} registro{{ filtered.length !== 1 ? 's' : '' }} encontrado{{ filtered.length !== 1 ? 's' : '' }}</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="filters-bar">
      <div class="search-bar">
        <span class="search-icon">⌕</span>
        <input v-model="searchQuery" type="text" placeholder="Buscar por usuario, entidad..." />
      </div>
      <div class="filter-group">
        <select v-model="filterType" class="select-filter">
          <option value="ALL">All Types</option>
          <option value="ORDER">Orders</option>
          <option value="USER">Users</option>
        </select>
        <input v-model="filterUser" type="text" class="input-filter" placeholder="Filter by user..." />
        <input type="date" v-model="startDate" class="date-input" />
        <input type="date" v-model="endDate" class="date-input" />
        <button class="btn-search" @click="fetchLogs">Search</button>
      </div>
    </div>

    <!-- Error -->
    <div v-if="error" class="error-banner">⚠ {{ error }}</div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <span>Loading audit logs...</span>
    </div>

    <!-- Empty -->
    <div v-else-if="filtered.length === 0" class="empty-state">
      <div class="empty-icon">🔍</div>
      <h3>No records found</h3>
      <p>Try adjusting your filters.</p>
    </div>

    <!-- Table -->
    <div v-else class="table-wrapper">
      <table class="audit-table">  <!-- ← clase corregida -->
        <thead>
  <tr>
    <th>Entity ID</th>
    <th>Type</th>
    <th>Created By</th>
    <th>Updated By</th>
    <th>Created At</th>
    <th>Updated At</th>
    <th>Detail</th>
  </tr>
</thead>
<tbody>
  <tr v-for="log in filtered" :key="`${log.entityType}-${log.entityId}`">
    <td class="td-entity">{{ log.entityId }}</td>
    <td>
      <span :class="['type-badge', typeConfig[log.entityType]?.class]">
        {{ typeConfig[log.entityType]?.label ?? log.entityType }}
      </span>
    </td>
    <td class="td-user">{{ log.createdBy ?? '—' }}</td>
    <td class="td-user">{{ log.updatedBy ?? '—' }}</td>
    <td class="td-date">{{ log.createdAt ? formatDate(log.createdAt) : '—' }}</td>
    <td class="td-date">{{ log.updatedAt ? formatDate(log.updatedAt) : '—' }}</td>
    <td>
      <button class="btn-detail" @click="openDetail(log)">Ver</button>
    </td>
  </tr>
</tbody>
      </table>
    </div>

    <!-- Overlay -->
    <Transition name="fade">
      <div v-if="showDrawer" class="overlay" @click="closeDrawer" />
    </Transition>

    <!-- Drawer Detail -->
    <Transition name="slide">
      <div v-if="showDrawer && selectedLog" class="drawer">
        <div class="drawer-header">
          <h2>Audit Detail #{{ selectedLog.entityId }}</h2>
          <button class="btn-close" @click="closeDrawer">✕</button>
        </div>

        <div class="drawer-body">
  <div class="detail-row">
    <span class="detail-label">Entity Type</span>
    <span :class="['type-badge', typeConfig[selectedLog.entityType]?.class]">
      {{ typeConfig[selectedLog.entityType]?.label ?? selectedLog.entityType }}
    </span>
  </div>
  <div class="detail-row">
    <span class="detail-label">Entity ID</span>
    <span class="detail-value">{{ selectedLog.entityId }}</span>
  </div>
  <div class="detail-row">
    <span class="detail-label">Created By</span>
    <span class="detail-value">{{ selectedLog.createdBy ?? '—' }}</span>
  </div>
  <div class="detail-row">
    <span class="detail-label">Updated By</span>
    <span class="detail-value">{{ selectedLog.updatedBy ?? '—' }}</span>
  </div>
  <div class="detail-row">
    <span class="detail-label">Created At</span>
    <span class="detail-value">{{ selectedLog.createdAt ? formatDate(selectedLog.createdAt) : '—' }}</span>
  </div>
  <div class="detail-row">
    <span class="detail-label">Updated At</span>
    <span class="detail-value">{{ selectedLog.updatedAt ? formatDate(selectedLog.updatedAt) : '—' }}</span>
  </div>
</div>

        <div class="drawer-footer">
          <button class="btn-cancel" @click="closeDrawer">Cerrar</button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
* { box-sizing: border-box; }

.page {
  padding: 2rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  min-height: 100vh;
  background: #f7f7f7;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1.5rem;
}
.header-left h1 {
  font-size: 2rem;
  font-weight: 900;
  color: #111;
  margin: 0 0 0.2rem;
  letter-spacing: -0.5px;
}
.header-left p { color: #999; font-size: 0.9rem; margin: 0; }

.filters-bar {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  border: 1.5px solid #e8e8e8;
  border-radius: 10px;
  padding: 0.7rem 1rem;
  transition: border-color 0.2s;
}
.search-bar:focus-within { border-color: #111; }
.search-icon { font-size: 1.2rem; color: #999; }
.search-bar input {
  border: none; outline: none;
  font-size: 0.95rem; color: #111;
  width: 100%; background: transparent;
}
.filter-group { display: flex; gap: 0.75rem; flex-wrap: wrap; }
.select-filter, .input-filter, .date-input {
  padding: 0.6rem 0.9rem;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  font-size: 0.9rem;
  color: #111;
  background: white;
  outline: none;
  transition: border-color 0.2s;
  font-family: inherit;
}
.select-filter:focus, .input-filter:focus, .date-input:focus { border-color: #111; }
.btn-search {
  padding: 0.6rem 1.4rem;
  background: #111; color: white;
  border: none; border-radius: 8px;
  font-size: 0.9rem; font-weight: 600;
  cursor: pointer; font-family: inherit;
  transition: background 0.2s;
}
.btn-search:hover { background: #333; }

.error-banner {
  background: #fff0f0;
  border-left: 3px solid #e02020;
  color: #e02020;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

.loading-state {
  display: flex; flex-direction: column;
  align-items: center; gap: 1rem;
  padding: 4rem; color: #999;
}
.empty-state { text-align: center; padding: 4rem 2rem; color: #999; }
.empty-icon { font-size: 3rem; margin-bottom: 1rem; }
.empty-state h3 { color: #111; margin: 0 0 0.5rem; }
.empty-state p { margin: 0; font-size: 0.9rem; }

.table-wrapper {
  background: white;
  border-radius: 12px;
  border: 1.5px solid #e8e8e8;
  overflow-x: auto;
}
.audit-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
.audit-table thead tr {
  background: #f7f7f7;
  border-bottom: 2px solid #e8e8e8;
}
.audit-table th {
  text-align: left;
  padding: 0.9rem 1rem;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.6px;
  color: #888;
  white-space: nowrap;
}
.audit-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}
.audit-table tbody tr:hover { background: #fafafa; }
.audit-table td {
  padding: 0.85rem 1rem;
  color: #333;
  white-space: nowrap;
}

.td-id { color: #bbb; font-size: 0.8rem; }
.td-entity { font-weight: 600; color: #111; }
.td-user { font-weight: 500; }
.td-date { color: #888; font-size: 0.85rem; }

.action-badge, .type-badge {
  padding: 0.25rem 0.7rem;
  border-radius: 20px;
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.action-create { background: #f0fdf4; color: #166534; }
.action-update { background: #fffbeb; color: #92400e; }
.action-delete { background: #fff0f0; color: #991b1b; }
.type-order { background: #eff6ff; color: #1d4ed8; }
.type-user  { background: #f5f3ff; color: #6d28d9; }

.btn-detail {
  padding: 0.35rem 0.9rem;
  background: #f5f5f5; border: none;
  border-radius: 6px; font-size: 0.82rem;
  font-weight: 500; cursor: pointer;
  font-family: inherit; transition: all 0.15s;
}
.btn-detail:hover { background: #111; color: white; }

.overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(2px);
  z-index: 100;
}
.drawer {
  position: fixed;
  top: 0; right: 0; bottom: 0;
  width: 460px; background: white;
  z-index: 101; display: flex;
  flex-direction: column;
  box-shadow: -8px 0 40px rgba(0,0,0,0.12);
}
.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
}
.drawer-header h2 {
  font-size: 1.3rem; font-weight: 700;
  color: #111; margin: 0;
}
.btn-close {
  background: #f5f5f5; border: none;
  width: 32px; height: 32px;
  border-radius: 8px; cursor: pointer;
  font-size: 0.9rem; color: #666;
  transition: all 0.15s;
}
.btn-close:hover { background: #111; color: white; }

.drawer-body {
  flex: 1; overflow-y: auto;
  padding: 1.5rem 2rem;
  display: flex; flex-direction: column;
  gap: 0.25rem;
}
.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0;
  border-bottom: 1px solid #f0f0f0;
}
.detail-label {
  font-size: 0.78rem; font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px; color: #888;
}
.detail-value {
  font-size: 0.95rem; font-weight: 500; color: #111;
}

.json-block { display: flex; flex-direction: column; gap: 0.4rem; margin-top: 1rem; }
.json-label {
  font-size: 0.78rem; font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px; color: #888; margin: 0;
}
.json-pre {
  background: #f7f7f7;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 1rem;
  font-size: 0.78rem; color: #333;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
}

.drawer-footer {
  padding: 1.25rem 2rem;
  border-top: 1px solid #f0f0f0;
}
.btn-cancel {
  width: 100%; padding: 0.75rem;
  background: #f5f5f5; border: none;
  border-radius: 8px; font-size: 0.9rem;
  font-weight: 500; color: #666;
  cursor: pointer; font-family: inherit;
  transition: background 0.15s;
}
.btn-cancel:hover { background: #e8e8e8; }

.spinner {
  width: 28px; height: 28px;
  border: 3px solid #e8e8e8;
  border-top-color: #111;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.fade-enter-active, .fade-leave-active { transition: opacity 0.25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-enter-active, .slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-enter-from, .slide-leave-to { transform: translateX(100%); }

@media (max-width: 600px) {
  .drawer { width: 100%; }
  .filter-group { flex-direction: column; }
}
</style>