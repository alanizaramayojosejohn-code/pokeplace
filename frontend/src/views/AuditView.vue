<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseBadge from '@/components/ui/BaseBadge.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseDrawer from '@/components/ui/BaseDrawer.vue'
import BaseSelect from '@/components/ui/BaseSelect.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import AlertBanner from '@/components/ui/AlertBanner.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import SearchBar from '@/components/ui/SearchBar.vue'

interface AuditEntry {
  id: number
  entityType: string
  entityId: number | null
  action: 'CREATE' | 'UPDATE' | 'DELETE' | 'LOGIN' | 'LOGOUT'
  previousValue: string | null
  newValue: string | null
  performedBy: string
  performedAt: string
  ipAddress: string | null
}

interface PageResponse {
  content: AuditEntry[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}

const logs = ref<AuditEntry[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const totalElements = ref(0)
const totalPages = ref(0)
const currentPage = ref(0)

const searchQuery = ref('')
const filterType = ref('ALL')
const filterAction = ref('ALL')
const filterUser = ref('')
const startDate = ref(
  new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0]
)
const endDate = ref(new Date().toISOString().split('T')[0])

const showDrawer = ref(false)
const selectedLog = ref<AuditEntry | null>(null)

onMounted(() => fetchLogs())

async function fetchLogs(page = 0) {
  loading.value = true
  error.value = null
  currentPage.value = page
  try {
    const params: Record<string, string | number> = { page, size: 50 }
    if (filterType.value !== 'ALL') params.entityType = filterType.value
    if (filterAction.value !== 'ALL') params.action = filterAction.value
    if (filterUser.value.trim()) params.performedBy = filterUser.value.trim()
    if (startDate.value) params.startDate = startDate.value
    if (endDate.value) params.endDate = endDate.value

    const res = await api.get<PageResponse>('/audit', { params })
    const data = res.data
    logs.value = Array.isArray(data.content) ? data.content : Array.isArray(data as any) ? (data as any) : []
    totalElements.value = data.totalElements ?? logs.value.length
    totalPages.value = data.totalPages ?? 1
  } catch {
    error.value = 'No se pudieron cargar los registros de auditoría.'
  } finally {
    loading.value = false
  }
}

function openDetail(log: AuditEntry) {
  selectedLog.value = log
  showDrawer.value = true
}
function closeDrawer() {
  showDrawer.value = false
  selectedLog.value = null
}
function formatDate(d: string) {
  return new Date(d).toLocaleString('es-BO')
}
function prettyJson(raw: string | null) {
  if (!raw) return null
  try { return JSON.stringify(JSON.parse(raw), null, 2) } catch { return raw }
}

const typeConfig: Record<string, { label: string; variant: 'info' | 'dark' | 'warning' | 'success' | 'default' }> = {
  ORDER:   { label: 'Pedido',    variant: 'info'    },
  USER:    { label: 'Usuario',   variant: 'dark'    },
  PRODUCT: { label: 'Producto',  variant: 'warning' },
  CLIENT:  { label: 'Cliente',   variant: 'success' },
}
const actionConfig: Record<string, { label: string; variant: 'success' | 'warning' | 'danger' | 'info' | 'default' }> = {
  CREATE:  { label: 'Creación',  variant: 'success' },
  UPDATE:  { label: 'Edición',   variant: 'warning' },
  DELETE:  { label: 'Borrado',   variant: 'danger'  },
  LOGIN:   { label: 'Ingreso',   variant: 'info'    },
  LOGOUT:  { label: 'Salida',    variant: 'default' },
}

const typeOptions = [
  { value: 'ALL', label: 'Todos los tipos' },
  { value: 'ORDER', label: 'Pedidos' },
  { value: 'USER', label: 'Usuarios' },
  { value: 'PRODUCT', label: 'Productos' },
  { value: 'CLIENT', label: 'Clientes' },
]
const actionOptions = [
  { value: 'ALL', label: 'Todas las acciones' },
  { value: 'CREATE', label: 'Creación' },
  { value: 'UPDATE', label: 'Edición' },
  { value: 'DELETE', label: 'Borrado' },
  { value: 'LOGIN', label: 'Ingreso' },
  { value: 'LOGOUT', label: 'Salida' },
]
</script>

<template>
  <div class="page">
    <PageHeader :title="'Auditoría'" :subtitle="`${totalElements} registro${totalElements !== 1 ? 's' : ''} en total`" />

    <div class="filters-card">
      <SearchBar v-model="searchQuery" placeholder="Buscar por usuario, entidad, acción..." />
      <div class="filter-row">
        <BaseSelect v-model="filterType" :options="typeOptions" />
        <BaseSelect v-model="filterAction" :options="actionOptions" />
        <BaseInput v-model="filterUser" placeholder="Filtrar por usuario..." />
        <input type="date" v-model="startDate" class="date-input" />
        <input type="date" v-model="endDate" class="date-input" />
        <BaseButton size="sm" variant="primary" @click="fetchLogs(0)">Buscar</BaseButton>
      </div>
    </div>

    <AlertBanner v-if="error" variant="danger" :message="error" />

    <LoadingState v-if="loading" />

    <div v-else-if="logs.length === 0 && !loading" class="empty-wrapper">
      <span class="empty-icon">🔍</span>
      <h3>Sin resultados</h3>
      <p>Prueba ajustando los filtros.</p>
    </div>

    <div v-else class="table-wrapper">
      <table class="audit-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Acción</th>
            <th>Tipo</th>
            <th>ID</th>
            <th>Usuario</th>
            <th>Fecha</th>
            <th>IP</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="log in logs" :key="log.id">
            <td class="td-id">{{ log.id }}</td>
            <td>
              <BaseBadge :variant="actionConfig[log.action]?.variant ?? 'default'">
                {{ actionConfig[log.action]?.label ?? log.action }}
              </BaseBadge>
            </td>
            <td>
              <BaseBadge :variant="typeConfig[log.entityType]?.variant ?? 'default'">
                {{ typeConfig[log.entityType]?.label ?? log.entityType }}
              </BaseBadge>
            </td>
            <td class="td-num">{{ log.entityId ?? '—' }}</td>
            <td class="td-user">{{ log.performedBy }}</td>
            <td class="td-date">{{ formatDate(log.performedAt) }}</td>
            <td class="td-ip">{{ log.ipAddress ?? '—' }}</td>
            <td>
              <BaseButton size="sm" variant="ghost" @click="openDetail(log)">Ver</BaseButton>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="totalPages > 1" class="pagination">
        <BaseButton size="sm" variant="ghost" :disabled="currentPage === 0" @click="fetchLogs(currentPage - 1)">← Anterior</BaseButton>
        <span class="page-info">Página {{ currentPage + 1 }} de {{ totalPages }}</span>
        <BaseButton size="sm" variant="ghost" :disabled="currentPage >= totalPages - 1" @click="fetchLogs(currentPage + 1)">Siguiente →</BaseButton>
      </div>
    </div>

    <BaseDrawer v-model="showDrawer" title="Detalle del Registro">
      <template v-if="selectedLog">
        <div class="detail-row">
          <span class="detail-label">Acción</span>
          <BaseBadge :variant="actionConfig[selectedLog.action]?.variant ?? 'default'">
            {{ actionConfig[selectedLog.action]?.label ?? selectedLog.action }}
          </BaseBadge>
        </div>
        <div class="detail-row">
          <span class="detail-label">Tipo</span>
          <BaseBadge :variant="typeConfig[selectedLog.entityType]?.variant ?? 'default'">
            {{ typeConfig[selectedLog.entityType]?.label ?? selectedLog.entityType }}
          </BaseBadge>
        </div>
        <div class="detail-row">
          <span class="detail-label">ID Entidad</span>
          <span class="detail-value">{{ selectedLog.entityId ?? '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Usuario</span>
          <span class="detail-value">{{ selectedLog.performedBy }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Fecha</span>
          <span class="detail-value">{{ formatDate(selectedLog.performedAt) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">IP</span>
          <span class="detail-value">{{ selectedLog.ipAddress ?? '—' }}</span>
        </div>

        <template v-if="selectedLog.previousValue || selectedLog.newValue">
          <div class="diff-section">
            <div v-if="selectedLog.previousValue" class="diff-block diff-before">
              <p class="diff-label">Antes</p>
              <pre class="json-pre">{{ prettyJson(selectedLog.previousValue) }}</pre>
            </div>
            <div v-if="selectedLog.newValue" class="diff-block diff-after">
              <p class="diff-label">Después</p>
              <pre class="json-pre">{{ prettyJson(selectedLog.newValue) }}</pre>
            </div>
          </div>
        </template>
      </template>

      <template #footer>
        <BaseButton variant="secondary" block @click="closeDrawer">Cerrar</BaseButton>
      </template>
    </BaseDrawer>
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

.filters-card {
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.filter-row {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
  align-items: end;
}

.filter-row > * {
  min-width: 140px;
  flex: 1;
}

.date-input {
  padding: 0.7rem 1rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  color: var(--color-text);
  outline: none;
  background: var(--color-surface);
  font-family: inherit;
  transition: border-color var(--transition-fast);
  min-width: 130px;
}

.date-input:focus {
  border-color: var(--color-dark);
}

.empty-wrapper {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text-subtle);
}

.empty-wrapper h3 {
  font-family: var(--font-display);
  color: var(--color-text);
  margin: 0 0 0.5rem;
  font-size: var(--text-xl);
}

.empty-wrapper p {
  margin: 0;
  font-size: var(--text-base);
}

.empty-icon {
  font-size: 3rem;
  display: block;
  margin-bottom: 1rem;
  opacity: 0.6;
}

.table-wrapper {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  border: 1.5px solid var(--color-border);
  overflow-x: auto;
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
}

.audit-table thead tr {
  border-bottom: 2px solid var(--color-border);
}

.audit-table th {
  text-align: left;
  padding: 0.85rem 1rem;
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-subtle);
  white-space: nowrap;
}

.audit-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition-fast);
}

.audit-table tbody tr:hover {
  background: var(--color-bg);
}

.audit-table td {
  padding: 0.75rem 1rem;
  color: var(--color-text);
}

.td-id { color: var(--color-text-ghost); font-size: var(--text-xs); }
.td-user { font-weight: 500; }
.td-date { color: var(--color-text-subtle); font-size: var(--text-xs); }
.td-ip { color: var(--color-text-ghost); font-size: var(--text-xs); font-family: monospace; }
.td-num { font-weight: 500; }

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 1rem;
  border-top: 1px solid var(--color-border);
}

.page-info {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.6rem 0;
  border-bottom: 1px solid var(--color-border);
}

.detail-label {
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--color-text-subtle);
}

.detail-value {
  font-size: var(--text-base);
  font-weight: 500;
  color: var(--color-text);
}

.diff-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1.25rem;
}

.diff-block {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.diff-label {
  font-size: var(--text-xs);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  margin: 0;
}

.diff-before .diff-label { color: var(--color-danger); }
.diff-after .diff-label { color: var(--color-success); }

.json-pre {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 1rem;
  font-size: var(--text-xs);
  color: var(--color-text);
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  max-height: 220px;
  overflow-y: auto;
}

.diff-before .json-pre {
  background: var(--color-danger-soft);
  border-color: var(--color-danger-soft);
}

.diff-after .json-pre {
  background: var(--color-success-soft);
  border-color: var(--color-success-soft);
}

@media (max-width: 600px) {
  .filter-row {
    flex-direction: column;
  }

  .filter-row > * {
    min-width: unset;
  }
}
</style>
