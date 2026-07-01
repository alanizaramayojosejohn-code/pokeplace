<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios'
import { useAuthStore } from '../stores/auth'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseDrawer from '@/components/ui/BaseDrawer.vue'
import BaseBadge from '@/components/ui/BaseBadge.vue'
import AlertBanner from '@/components/ui/AlertBanner.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import SearchBar from '@/components/ui/SearchBar.vue'

const authStore = useAuthStore()
const isKitchen = computed(() => authStore.userRole === 'KITCHEN')

interface StockNote {
  id: number
  message: string
  status: 'PENDING' | 'RESOLVED'
  createdBy: string | null
  createdAt: string | null
  updatedAt: string | null
}

const notes = ref<StockNote[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const submitting = ref(false)

const newMessage = ref('')
const filterStatus = ref<'ALL' | 'PENDING' | 'RESOLVED'>('ALL')
const searchQuery = ref('')

const showDrawer = ref(false)
const selectedNote = ref<StockNote | null>(null)

onMounted(() => fetchNotes())

async function fetchNotes() {
  loading.value = true
  error.value = null
  try {
    const res = await api.get('/stock-notes')
    notes.value = res.data
  } catch {
    error.value = 'No se pudieron cargar los avisos.'
  } finally {
    loading.value = false
  }
}

async function createNote() {
  if (!newMessage.value.trim()) return
  submitting.value = true
  try {
    const res = await api.post('/stock-notes', { message: newMessage.value.trim() })
    notes.value.unshift(res.data)
    newMessage.value = ''
  } catch {
    error.value = 'No se pudo enviar el aviso.'
  } finally {
    submitting.value = false
  }
}

async function resolveNote(id: number) {
  try {
    const res = await api.patch(`/stock-notes/${id}/resolve`)
    const idx = notes.value.findIndex(n => n.id === id)
    if (idx !== -1) notes.value[idx] = res.data
    if (selectedNote.value?.id === id) selectedNote.value = res.data
  } catch {
    error.value = 'No se pudo marcar como resuelto.'
  }
}

async function deleteNote(id: number) {
  try {
    await api.delete(`/stock-notes/${id}`)
    notes.value = notes.value.filter(n => n.id !== id)
    if (selectedNote.value?.id === id) closeDrawer()
  } catch {
    error.value = 'No se pudo eliminar el aviso.'
  }
}

const filtered = computed(() => {
  const search = searchQuery.value.toLowerCase()
  return notes.value.filter(note => {
    if (filterStatus.value !== 'ALL' && note.status !== filterStatus.value) return false
    if (search) {
      const haystack = `${note.message} ${note.createdBy ?? ''}`.toLowerCase()
      if (!haystack.includes(search)) return false
    }
    return true
  })
})

const pendingCount = computed(() => notes.value.filter(n => n.status === 'PENDING').length)

function openDetail(note: StockNote) {
  selectedNote.value = note
  showDrawer.value = true
}
function closeDrawer() {
  showDrawer.value = false
  selectedNote.value = null
}
function formatDate(dateStr: string) {
  return new Date(dateStr).toLocaleString('es-BO')
}
</script>

<template>
  <div class="page">
    <PageHeader :title="'Avisos de Stock'">
      <template #actions>
        <BaseBadge v-if="pendingCount > 0" variant="warning">{{ pendingCount }} pendiente{{ pendingCount !== 1 ? 's' : '' }}</BaseBadge>
      </template>
    </PageHeader>

    <div v-if="isKitchen" class="compose-card">
      <textarea
        v-model="newMessage"
        class="compose-input"
        placeholder="Escribe un aviso de stock... (ej: Se acabó el arroz, queda poco aceite)"
        rows="3"
        @keydown.ctrl.enter="createNote"
      />
      <div class="compose-footer">
        <span class="compose-hint">Ctrl + Enter para enviar</span>
        <BaseButton :disabled="!newMessage.trim() || submitting" size="sm" variant="primary" @click="createNote">
          {{ submitting ? 'Enviando...' : 'Enviar aviso' }}
        </BaseButton>
      </div>
    </div>

    <div class="filters-card">
      <SearchBar v-model="searchQuery" placeholder="Buscar avisos..." />
      <div class="filter-group">
        <button
          v-for="opt in ['ALL', 'PENDING', 'RESOLVED']"
          :key="opt"
          :class="['btn-filter', { active: filterStatus === opt }]"
          @click="filterStatus = opt as any"
        >
          {{ opt === 'ALL' ? 'Todos' : opt === 'PENDING' ? 'Pendientes' : 'Resueltos' }}
        </button>
      </div>
    </div>

    <AlertBanner v-if="error" variant="danger" :message="error" />

    <LoadingState v-if="loading" message="Cargando avisos..." />

    <div v-else-if="filtered.length === 0 && !loading" class="empty-wrapper">
      <span class="empty-icon">📋</span>
      <h3>Sin avisos</h3>
      <p>No hay avisos que coincidan con los filtros.</p>
    </div>

    <div v-else class="table-wrapper">
      <table class="notes-table">
        <thead>
          <tr>
            <th>Estado</th>
            <th>Mensaje</th>
            <th>Creado por</th>
            <th>Fecha</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="note in filtered" :key="note.id">
            <td>
              <BaseBadge :variant="note.status === 'PENDING' ? 'warning' : 'success'">
                {{ note.status === 'PENDING' ? 'Pendiente' : 'Resuelto' }}
              </BaseBadge>
            </td>
            <td class="td-message">{{ note.message }}</td>
            <td class="td-user">{{ note.createdBy ?? '—' }}</td>
            <td class="td-date">{{ note.createdAt ? formatDate(note.createdAt) : '—' }}</td>
            <td>
              <div class="action-btns">
                <BaseButton size="sm" variant="ghost" @click="openDetail(note)">Ver</BaseButton>
                <button
                  v-if="note.status === 'PENDING'"
                  class="btn-icon btn-icon-resolve"
                  title="Marcar resuelto"
                  @click="resolveNote(note.id)"
                >
                  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 8l3 3 7-7"/></svg>
                </button>
                <button class="btn-icon btn-icon-delete" title="Eliminar" @click="deleteNote(note.id)">
                  <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 4l8 8M12 4l-8 8"/></svg>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <BaseDrawer v-model="showDrawer" title="Detalle del Aviso">
      <template v-if="selectedNote">
        <div class="detail-row">
          <span class="detail-label">Estado</span>
          <BaseBadge :variant="selectedNote.status === 'PENDING' ? 'warning' : 'success'">
            {{ selectedNote.status === 'PENDING' ? 'Pendiente' : 'Resuelto' }}
          </BaseBadge>
        </div>
        <div class="detail-row">
          <span class="detail-label">Creado por</span>
          <span class="detail-value">{{ selectedNote.createdBy ?? '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Fecha</span>
          <span class="detail-value">{{ selectedNote.createdAt ? formatDate(selectedNote.createdAt) : '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Actualizado</span>
          <span class="detail-value">{{ selectedNote.updatedAt ? formatDate(selectedNote.updatedAt) : '—' }}</span>
        </div>
        <div class="message-block">
          <p class="detail-label">Mensaje</p>
          <p class="message-text">{{ selectedNote.message }}</p>
        </div>
      </template>

      <template #footer>
        <BaseButton
          v-if="selectedNote?.status === 'PENDING'"
          variant="primary"
          block
          @click="resolveNote(selectedNote!.id)"
        >Marcar como resuelto</BaseButton>
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

.compose-card {
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 1rem;
}

.compose-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: var(--text-base);
  color: var(--color-text);
  font-family: inherit;
  resize: vertical;
  background: transparent;
}

.compose-input::placeholder { color: var(--color-text-ghost); }

.compose-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}

.compose-hint {
  font-size: var(--text-xs);
  color: var(--color-text-ghost);
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

.filter-group {
  display: flex;
  gap: 0.5rem;
}

.btn-filter {
  padding: 0.5rem 1rem;
  background: transparent;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  font-family: inherit;
  transition: all var(--transition-fast);
}

.btn-filter:hover {
  border-color: var(--color-dark);
  color: var(--color-text);
}

.btn-filter.active {
  background: var(--color-dark);
  border-color: var(--color-dark);
  color: white;
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

.notes-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
}

.notes-table thead tr {
  border-bottom: 2px solid var(--color-border);
}

.notes-table th {
  text-align: left;
  padding: 0.85rem 1rem;
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-subtle);
  white-space: nowrap;
}

.notes-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background var(--transition-fast);
}

.notes-table tbody tr:hover {
  background: var(--color-bg);
}

.notes-table td {
  padding: 0.75rem 1rem;
  color: var(--color-text);
}

.td-message {
  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.td-user {
  font-weight: 500;
  white-space: nowrap;
}

.td-date {
  color: var(--color-text-subtle);
  font-size: var(--text-xs);
  white-space: nowrap;
}

.action-btns {
  display: flex;
  gap: 0.35rem;
  align-items: center;
}

.btn-icon {
  width: 30px;
  height: 30px;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  font-family: inherit;
}

.btn-icon svg {
  width: 14px;
  height: 14px;
}

.btn-icon-resolve {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.btn-icon-resolve:hover {
  background: var(--color-success);
  color: white;
}

.btn-icon-delete {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.btn-icon-delete:hover {
  background: var(--color-danger);
  color: white;
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

.message-block {
  margin-top: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.message-text {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 1rem;
  font-size: var(--text-base);
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

@media (max-width: 600px) {
  .filter-group {
    flex-wrap: wrap;
  }
}
</style>
