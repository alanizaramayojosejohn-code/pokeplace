<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useClientsStore } from '../stores/clients'
import PageHeader from '../components/ui/PageHeader.vue'
import SearchBar from '../components/ui/SearchBar.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseDrawer from '../components/ui/BaseDrawer.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import AlertBanner from '../components/ui/AlertBanner.vue'

const store = useClientsStore()

const showDrawer = ref(false)
const editingId = ref<number | null>(null)
const searchQuery = ref('')
const form = ref({ ci: 0, name: '' })

onMounted(() => store.fetchAll())

const filtered = computed(() =>
  store.items.filter((c) =>
    `${c.name} ${c.ci}`.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
)

function openCreate() {
  editingId.value = null
  form.value = { ci: 0, name: '' }
  store.error = null
  showDrawer.value = true
}

function openEdit(c: { id: number; ci: number; name: string }) {
  editingId.value = c.id
  form.value = { ci: c.ci, name: c.name }
  store.error = null
  showDrawer.value = true
}

async function handleSubmit() {
  const payload = { ci: Number(form.value.ci), name: form.value.name }
  const ok = editingId.value
    ? await store.update(editingId.value, payload)
    : await store.create(payload)
  if (ok) showDrawer.value = false
}

async function handleDelete(id: number) {
  if (confirm('¿Eliminar este cliente?')) {
    await store.remove(id)
  }
}

function initial(name: string) {
  return name?.charAt(0)?.toUpperCase() ?? '?'
}
</script>

<template>
  <div class="page">
    <PageHeader
      title="Clientes"
      :subtitle="`${store.items.length} cliente${store.items.length !== 1 ? 's' : ''} registrado${store.items.length !== 1 ? 's' : ''}`"
    >
      <template #actions>
        <BaseButton variant="dark" @click="openCreate">+ Nuevo Cliente</BaseButton>
      </template>
    </PageHeader>

    <SearchBar v-model="searchQuery" placeholder="Buscar por nombre o CI..." />

    <LoadingState v-if="store.loading && store.items.length === 0" />

    <EmptyState
      v-else-if="filtered.length === 0"
      icon="👥"
      title="Sin clientes"
      message="Registra clientes para asociarlos a las órdenes."
    />

    <div v-else class="grid">
      <article v-for="c in filtered" :key="c.id" class="card">
        <div class="card-top">
          <div class="avatar">{{ initial(c.name) }}</div>
        </div>
        <div class="card-body">
          <h3>{{ c.name }}</h3>
          <p class="ci">🪪 CI: {{ c.ci }}</p>
        </div>
        <div class="card-actions">
          <BaseButton size="sm" variant="secondary" @click="openEdit(c)">Editar</BaseButton>
          <BaseButton size="sm" variant="danger" @click="handleDelete(c.id)">Eliminar</BaseButton>
        </div>
      </article>
    </div>

    <BaseDrawer v-model="showDrawer" :title="editingId ? 'Editar Cliente' : 'Nuevo Cliente'">
      <BaseInput v-model="form.name" label="Nombre completo" placeholder="Juan Pérez" required />
      <BaseInput v-model="form.ci" label="Carnet de Identidad" type="number" placeholder="12345678" required />
      <AlertBanner v-if="store.error" :message="store.error" />

      <template #footer>
        <BaseButton variant="secondary" block @click="showDrawer = false">Cancelar</BaseButton>
        <BaseButton variant="primary" block :loading="store.loading" @click="handleSubmit">
          {{ editingId ? 'Guardar Cambios' : 'Crear Cliente' }}
        </BaseButton>
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

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}

.card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border: 1.5px solid transparent;
  transition: all var(--transition-fast);
}

.card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.avatar {
  width: 48px;
  height: 48px;
  background: var(--color-dark);
  color: white;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: var(--text-md);
}

.card-body h3 {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  margin: 0 0 0.3rem;
}

.ci {
  color: var(--color-text-subtle);
  font-size: var(--text-sm);
  margin: 0;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}

.card-actions > * {
  flex: 1;
}
</style>
