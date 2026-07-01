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
import PaginationBar from '../components/ui/PaginationBar.vue'

const store = useClientsStore()

const showDrawer = ref(false)
const editingId = ref<number | null>(null)
const searchQuery = ref('')
const form = ref({ nit: '', name: '', ci: '', phone: '', email: '' })

onMounted(() => store.fetchPage())

const touched = ref<Set<string>>(new Set())

function markTouched(field: string) {
  touched.value.add(field)
}

const namePattern = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/
const phonePattern = /^[0-9]{8,15}$/
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const digitsOnly = /^[0-9]+$/
const nitPattern = /^[0-9]{7,15}$/
const ciPattern = /^[0-9]{7,10}$/

const fieldErrors = computed(() => ({
  name: !form.value.name ? 'El nombre es obligatorio'
    : !namePattern.test(form.value.name) ? 'Solo letras y espacios' : '',
  nit: !form.value.nit ? 'El NIT es obligatorio'
    : !digitsOnly.test(form.value.nit) ? 'Solo números'
    : !nitPattern.test(form.value.nit) ? 'Debe tener 7-15 dígitos' : '',
  ci: !form.value.ci ? 'El CI es obligatorio'
    : !digitsOnly.test(form.value.ci) ? 'Solo números'
    : !ciPattern.test(form.value.ci) ? 'Debe tener 7-10 dígitos' : '',
  phone: !form.value.phone ? 'El celular es obligatorio'
    : !phonePattern.test(form.value.phone) ? 'Debe tener 8-15 dígitos' : '',
  email: form.value.email && !emailPattern.test(form.value.email) ? 'Email inválido' : '',
}))

const filtered = computed(() =>
  store.items.filter((c) =>
    `${c.name} ${c.nit} ${c.ci ?? ''}`.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
)

function goToPage(p: number) {
  store.fetchPage(p)
}

function openCreate() {
  editingId.value = null
  form.value = { nit: '', name: '', ci: '', phone: '', email: '' }
  touched.value = new Set()
  store.error = null
  showDrawer.value = true
}

function openEdit(c: ClientViewClient) {
  editingId.value = c.id
  form.value = { nit: c.nit, name: c.name, ci: c.ci ?? '', phone: c.phone, email: c.email ?? '' }
  touched.value = new Set()
  store.error = null
  showDrawer.value = true
}

interface ClientViewClient {
  id: number
  nit: string
  name: string
  ci?: string
  phone: string
  email?: string
}

function touchAll() {
  ;['name', 'nit', 'ci', 'phone', 'email'].forEach((f) => touched.value.add(f))
}

async function handleSubmit() {
  touchAll()
  if (!Object.values(fieldErrors.value).every((e) => !e)) return
  const payload = {
    nit: form.value.nit,
    name: form.value.name,
    ci: form.value.ci,
    phone: form.value.phone,
    email: form.value.email || undefined,
  }
  const created = editingId.value
    ? await store.update(editingId.value, payload)
    : await store.create(payload)
  if (created) showDrawer.value = false
}

async function handleDelete(id: number) {
  if (confirm('¿Eliminar este cliente?')) {
    const ok = await store.remove(id)
    if (ok) store.fetchPage(store.page)
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
      :subtitle="`${store.totalElements} cliente${store.totalElements !== 1 ? 's' : ''} registrado${store.totalElements !== 1 ? 's' : ''}`"
    >
      <template #actions>
        <BaseButton variant="dark" @click="openCreate">+ Nuevo Cliente</BaseButton>
      </template>
    </PageHeader>

    <SearchBar v-model="searchQuery" placeholder="Buscar por nombre o NIT..." />

    <LoadingState v-if="store.loading && store.items.length === 0" />

    <EmptyState
      v-else-if="filtered.length === 0"
      icon="👥"
      title="Sin clientes"
      message="Registra clientes para asociarlos a las órdenes."
    />

    <template v-else>
      <div class="grid">
        <article v-for="c in filtered" :key="c.id" class="card">
          <div class="card-top">
            <div class="avatar">{{ initial(c.name) }}</div>
          </div>
          <div class="card-body">
            <h3>{{ c.name }}</h3>
            <p class="detail-line">🪪 NIT: {{ c.nit }}</p>
            <p class="detail-line">🆔 CI: {{ c.ci }}</p>
            <p class="detail-line">📞 {{ c.phone }}</p>
            <p v-if="c.email" class="detail-line">✉️ {{ c.email }}</p>
          </div>
          <div class="card-actions">
            <BaseButton size="sm" variant="secondary" @click="openEdit(c)">Editar</BaseButton>
            <BaseButton size="sm" variant="danger" @click="handleDelete(c.id)">Eliminar</BaseButton>
          </div>
        </article>
      </div>

      <PaginationBar
        :page="store.page"
        :total-pages="store.totalPages"
        :total-elements="store.totalElements"
        @page-change="goToPage"
      />
    </template>

    <BaseDrawer v-model="showDrawer" :title="editingId ? 'Editar Cliente' : 'Nuevo Cliente'">
      <BaseInput v-model="form.name" label="Nombre completo" placeholder="Juan Pérez"
        :error="touched.has('name') ? fieldErrors.name : ''"
        @blur="markTouched('name')" />
      <BaseInput v-model="form.nit" label="NIT" placeholder="1234567890"
        :error="touched.has('nit') ? fieldErrors.nit : ''"
        @blur="markTouched('nit')" />
      <BaseInput v-model="form.ci" label="CI" placeholder="1234567"
        :error="touched.has('ci') ? fieldErrors.ci : ''"
        @blur="markTouched('ci')" />
      <BaseInput v-model="form.phone" label="Celular" type="tel" placeholder="71234567"
        :error="touched.has('phone') ? fieldErrors.phone : ''"
        @blur="markTouched('phone')" />
      <BaseInput v-model="form.email" label="Correo electrónico" type="email" placeholder="cliente@ejemplo.com"
        :error="touched.has('email') ? fieldErrors.email : ''"
        @blur="markTouched('email')" />
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

.detail-line {
  color: var(--color-text-subtle);
  font-size: var(--text-sm);
  margin: 0.15rem 0;
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
