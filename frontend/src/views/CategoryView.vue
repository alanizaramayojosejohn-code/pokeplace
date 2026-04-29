<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useCategoriesStore } from '../stores/categories'
import PageHeader from '../components/ui/PageHeader.vue'
import SearchBar from '../components/ui/SearchBar.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseDrawer from '../components/ui/BaseDrawer.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import AlertBanner from '../components/ui/AlertBanner.vue'

const store = useCategoriesStore()

const showDrawer = ref(false)
const editingId = ref<number | null>(null)
const searchQuery = ref('')
const form = ref({ name: '' })

onMounted(() => store.fetchAll())

const filtered = computed(() =>
  store.items.filter((c) => c.name.toLowerCase().includes(searchQuery.value.toLowerCase())),
)

function openCreate() {
  editingId.value = null
  form.value = { name: '' }
  store.error = null
  showDrawer.value = true
}

function openEdit(cat: { id: number; name: string }) {
  editingId.value = cat.id
  form.value = { name: cat.name }
  store.error = null
  showDrawer.value = true
}

async function handleSubmit() {
  const ok = editingId.value
    ? await store.update(editingId.value, form.value)
    : await store.create(form.value)
  if (ok) showDrawer.value = false
}

async function handleDelete(id: number) {
  if (confirm('¿Eliminar esta categoría?')) {
    await store.remove(id)
  }
}
</script>

<template>
  <div class="page">
    <PageHeader
      title="Categorías"
      :subtitle="`${store.items.length} categoría${store.items.length !== 1 ? 's' : ''} registrada${store.items.length !== 1 ? 's' : ''}`"
    >
      <template #actions>
        <BaseButton variant="dark" @click="openCreate">+ Nueva Categoría</BaseButton>
      </template>
    </PageHeader>

    <SearchBar v-model="searchQuery" placeholder="Buscar categoría..." />

    <LoadingState v-if="store.loading && store.items.length === 0" />

    <EmptyState
      v-else-if="filtered.length === 0"
      icon="🗂"
      title="Sin categorías"
      message="Crea tu primera categoría para empezar a organizar los productos."
    />

    <div v-else class="grid">
      <article v-for="cat in filtered" :key="cat.id" class="card">
        <div class="card-body">
          <span class="card-icon">🗂</span>
          <h3>{{ cat.name }}</h3>
        </div>
        <div class="card-actions">
          <BaseButton size="sm" variant="secondary" @click="openEdit(cat)">Editar</BaseButton>
          <BaseButton size="sm" variant="danger" @click="handleDelete(cat.id)">Eliminar</BaseButton>
        </div>
      </article>
    </div>

    <BaseDrawer
      v-model="showDrawer"
      :title="editingId ? 'Editar Categoría' : 'Nueva Categoría'"
    >
      <BaseInput
        v-model="form.name"
        label="Nombre"
        placeholder="Ej: Poke Bowls Especiales"
        required
      />
      <AlertBanner v-if="store.error" :message="store.error" />

      <template #footer>
        <BaseButton variant="secondary" block @click="showDrawer = false">Cancelar</BaseButton>
        <BaseButton variant="primary" block :loading="store.loading" @click="handleSubmit">
          {{ editingId ? 'Guardar Cambios' : 'Crear Categoría' }}
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
  gap: 1rem;
  border: 1.5px solid transparent;
  transition: all var(--transition-fast);
}

.card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-body {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.card-icon {
  font-size: 1.5rem;
  width: 40px;
  height: 40px;
  background: var(--color-primary-soft);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
}

.card h3 {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
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
