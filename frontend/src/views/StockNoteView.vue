<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios'
import { useAuthStore } from '../stores/auth'

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
  return new Date(dateStr).toLocaleString()
}
</script>

<template>
  <div class="page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1>Avisos de Stock</h1>
        <p>{{ filtered.length }} aviso{{ filtered.length !== 1 ? 's' : '' }} encontrado{{ filtered.length !== 1 ? 's' : '' }}</p>
      </div>
      <div v-if="pendingCount > 0" class="pending-badge">
        {{ pendingCount }} pendiente{{ pendingCount !== 1 ? 's' : '' }}
      </div>
    </div>
    <div v-if="isKitchen" class="compose-box">
    <!-- New note form -->
    <div class="compose-box">
      <textarea
        v-model="newMessage"
        class="compose-input"
        placeholder="Escribe un aviso de stock... (ej: Se acabó el arroz, queda poco aceite)"
        rows="3"
        @keydown.ctrl.enter="createNote"
      />
      <div class="compose-footer">
        <span class="compose-hint">Ctrl + Enter para enviar</span>
        <button
          class="btn-send"
          :disabled="!newMessage.trim() || submitting"
          @click="createNote"
        >
          {{ submitting ? 'Enviando...' : 'Enviar aviso' }}
        </button>
      </div>
    </div>
</div>
    <!-- Filters -->
    <div class="filters-bar">
      <div class="search-bar">
        <span class="search-icon">⌕</span>
        <input v-model="searchQuery" type="text" placeholder="Buscar avisos..." />
      </div>
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

    <!-- Error -->
    <div v-if="error" class="error-banner">⚠ {{ error }}</div>

    <!-- Loading -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <span>Cargando avisos...</span>
    </div>

    <!-- Empty -->
    <div v-else-if="filtered.length === 0" class="empty-state">
      <div class="empty-icon">📋</div>
      <h3>Sin avisos</h3>
      <p>No hay avisos que coincidan con los filtros.</p>
    </div>

    <!-- Table -->
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
              <span :class="['status-badge', note.status === 'PENDING' ? 'status-pending' : 'status-resolved']">
                {{ note.status === 'PENDING' ? 'Pendiente' : 'Resuelto' }}
              </span>
            </td>
            <td class="td-message">{{ note.message }}</td>
            <td class="td-user">{{ note.createdBy ?? '—' }}</td>
            <td class="td-date">{{ note.createdAt ? formatDate(note.createdAt) : '—' }}</td>
            <td>
              <div class="action-btns">
                <button class="btn-detail" @click="openDetail(note)">Ver</button>
                <button
                  v-if="note.status === 'PENDING'"
                  class="btn-resolve"
                  @click="resolveNote(note.id)"
                >✓</button>
                <button class="btn-delete" @click="deleteNote(note.id)">✕</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Overlay -->
    <Transition name="fade">
      <div v-if="showDrawer" class="overlay" @click="closeDrawer" />
    </Transition>

    <!-- Drawer -->
    <Transition name="slide">
      <div v-if="showDrawer && selectedNote" class="drawer">
        <div class="drawer-header">
          <h2>Aviso #{{ selectedNote.id }}</h2>
          <button class="btn-close" @click="closeDrawer">✕</button>
        </div>

        <div class="drawer-body">
          <div class="detail-row">
            <span class="detail-label">Estado</span>
            <span :class="['status-badge', selectedNote.status === 'PENDING' ? 'status-pending' : 'status-resolved']">
              {{ selectedNote.status === 'PENDING' ? 'Pendiente' : 'Resuelto' }}
            </span>
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
        </div>

        <div class="drawer-footer">
          <button
            v-if="selectedNote.status === 'PENDING'"
            class="btn-resolve-full"
            @click="resolveNote(selectedNote.id)"
          >Marcar como resuelto</button>
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

.pending-badge {
  background: #fff7ed;
  color: #c2410c;
  border: 1.5px solid #fed7aa;
  border-radius: 20px;
  padding: 0.35rem 0.9rem;
  font-size: 0.82rem;
  font-weight: 700;
}

/* Compose */
.compose-box {
  background: white;
  border: 1.5px solid #e8e8e8;
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1.5rem;
}
.compose-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 0.95rem;
  color: #111;
  font-family: inherit;
  resize: vertical;
  background: transparent;
}
.compose-input::placeholder { color: #bbb; }
.compose-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.75rem;
  padding-top: 0.75rem;
  border-top: 1px solid #f0f0f0;
}
.compose-hint { font-size: 0.78rem; color: #bbb; }
.btn-send {
  padding: 0.55rem 1.4rem;
  background: #111;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.2s;
}
.btn-send:hover:not(:disabled) { background: #333; }
.btn-send:disabled { background: #ccc; cursor: not-allowed; }

/* Filters */
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
.filter-group { display: flex; gap: 0.5rem; }
.btn-filter {
  padding: 0.55rem 1.1rem;
  background: white;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}
.btn-filter:hover { border-color: #111; color: #111; }
.btn-filter.active { background: #111; border-color: #111; color: white; }

/* Error */
.error-banner {
  background: #fff0f0;
  border-left: 3px solid #e02020;
  color: #e02020;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.85rem;
  margin-bottom: 1rem;
}

/* Loading */
.loading-state {
  display: flex; flex-direction: column;
  align-items: center; gap: 1rem;
  padding: 4rem; color: #999;
}
.empty-state { text-align: center; padding: 4rem 2rem; color: #999; }
.empty-icon { font-size: 3rem; margin-bottom: 1rem; }
.empty-state h3 { color: #111; margin: 0 0 0.5rem; }
.empty-state p { margin: 0; font-size: 0.9rem; }

/* Table */
.table-wrapper {
  background: white;
  border-radius: 12px;
  border: 1.5px solid #e8e8e8;
  overflow-x: auto;
}
.notes-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}
.notes-table thead tr {
  background: #f7f7f7;
  border-bottom: 2px solid #e8e8e8;
}
.notes-table th {
  text-align: left;
  padding: 0.9rem 1rem;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.6px;
  color: #888;
  white-space: nowrap;
}
.notes-table tbody tr {
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.15s;
}
.notes-table tbody tr:hover { background: #fafafa; }
.notes-table td {
  padding: 0.85rem 1rem;
  color: #333;
}

.td-message {
  max-width: 320px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
  color: #111;
}
.td-user { font-weight: 500; white-space: nowrap; }
.td-date { color: #888; font-size: 0.85rem; white-space: nowrap; }

.status-badge {
  padding: 0.25rem 0.7rem;
  border-radius: 20px;
  font-size: 0.72rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}
.status-pending  { background: #fff7ed; color: #c2410c; }
.status-resolved { background: #f0fdf4; color: #166534; }

.action-btns { display: flex; gap: 0.4rem; align-items: center; }
.btn-detail {
  padding: 0.35rem 0.9rem;
  background: #f5f5f5; border: none;
  border-radius: 6px; font-size: 0.82rem;
  font-weight: 500; cursor: pointer;
  font-family: inherit; transition: all 0.15s;
}
.btn-detail:hover { background: #111; color: white; }

.btn-resolve {
  padding: 0.35rem 0.6rem;
  background: #f0fdf4;
  border: none;
  border-radius: 6px;
  font-size: 0.82rem;
  font-weight: 700;
  color: #166534;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-resolve:hover { background: #166534; color: white; }

.btn-delete {
  padding: 0.35rem 0.6rem;
  background: #fff0f0;
  border: none;
  border-radius: 6px;
  font-size: 0.82rem;
  font-weight: 700;
  color: #991b1b;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-delete:hover { background: #991b1b; color: white; }

/* Overlay & Drawer */
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
.message-block {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.message-text {
  background: #f7f7f7;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  padding: 1rem;
  font-size: 0.95rem;
  color: #111;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.drawer-footer {
  padding: 1.25rem 2rem;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.btn-resolve-full {
  width: 100%; padding: 0.75rem;
  background: #111; border: none;
  border-radius: 8px; font-size: 0.9rem;
  font-weight: 600; color: white;
  cursor: pointer; font-family: inherit;
  transition: background 0.15s;
}
.btn-resolve-full:hover { background: #333; }
.btn-cancel {
  width: 100%; padding: 0.75rem;
  background: #f5f5f5; border: none;
  border-radius: 8px; font-size: 0.9rem;
  font-weight: 500; color: #666;
  cursor: pointer; font-family: inherit;
  transition: background 0.15s;
}
.btn-cancel:hover { background: #e8e8e8; }

/* Spinner */
.spinner {
  width: 28px; height: 28px;
  border: 3px solid #e8e8e8;
  border-top-color: #111;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Transitions */
.fade-enter-active, .fade-leave-active { transition: opacity 0.25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.slide-enter-active, .slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-enter-from, .slide-leave-to { transform: translateX(100%); }

@media (max-width: 600px) {
  .drawer { width: 100%; }
  .filter-group { flex-wrap: wrap; }
}
</style>