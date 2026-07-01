<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUsersStore } from '../stores/user'
import PageHeader from '@/components/ui/PageHeader.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import BaseDrawer from '@/components/ui/BaseDrawer.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseBadge from '@/components/ui/BaseBadge.vue'
import AlertBanner from '@/components/ui/AlertBanner.vue'
import LoadingState from '@/components/ui/LoadingState.vue'
import EmptyState from '@/components/ui/EmptyState.vue'
import SearchBar from '@/components/ui/SearchBar.vue'

const store = useUsersStore()

const showDrawer = ref(false)
const editingId = ref<number | null>(null)
const searchQuery = ref('')

const emptyForm = {
  name: '',
  lastname: '',
  email: '',
  password: '',
  phone: '',
  ci: 0,
  role: 'CASHIER',
}

const form = ref({ ...emptyForm })

onMounted(() => {
  store.fetchUsers()
})

const touched = ref<Set<string>>(new Set())

function markTouched(field: string) {
  touched.value.add(field)
}

const namePattern = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/
const phonePattern = /^[0-9]{8,15}$/
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

const fieldErrors = computed(() => ({
  name: !form.value.name ? 'El nombre es obligatorio'
    : !namePattern.test(form.value.name) ? 'Solo letras y espacios' : '',
  lastname: form.value.lastname && !namePattern.test(form.value.lastname) ? 'Solo letras y espacios' : '',
  email: !form.value.email ? 'El email es obligatorio'
    : !emailPattern.test(form.value.email) ? 'Email inválido' : '',
  password: !editingId.value && !form.value.password ? 'La contraseña es obligatoria' : '',
  phone: form.value.phone && !phonePattern.test(form.value.phone) ? 'Debe tener 8-15 dígitos' : '',
  ci: form.value.ci <= 0 ? 'Debe ser un valor positivo' : '',
}))

const filteredUsers = computed(() =>
  store.users.filter((u) =>
    `${u.name} ${u.lastname} ${u.email}`.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
)

function openCreate() {
  editingId.value = null
  form.value = { ...emptyForm }
  touched.value = new Set()
  showDrawer.value = true
}

function openEdit(user: any) {
  editingId.value = user.id
  form.value = {
    name: user.name,
    lastname: user.lastname ?? '',
    email: user.email,
    password: '',
    phone: user.phone ?? '',
    ci: user.ci ?? 0,
    role: user.role,
  }
  touched.value = new Set()
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
  editingId.value = null
  form.value = { ...emptyForm }
}

function touchAll() {
  ;['name', 'lastname', 'email', 'password', 'phone', 'ci'].forEach((f) => touched.value.add(f))
}

async function handleSubmit() {
  touchAll()
  if (!Object.values(fieldErrors.value).every((e) => !e)) return
  if (editingId.value) {
    await store.updateUser(editingId.value, form.value)
  } else {
    await store.createUser(form.value)
  }
  if (!store.error) closeDrawer()
}

async function handleDelete(id: number) {
  if (confirm('¿Estás seguro de que deseas eliminar este usuario?')) {
    await store.deleteUser(id)
  }
}

function getInitials(name: string, lastname: string) {
  return `${name?.charAt(0) ?? ''}${lastname?.charAt(0) ?? ''}`.toUpperCase()
}

const roleConfig: Record<string, { label: string; variant: 'dark' | 'danger' | 'success' }> = {
  ADMIN: { label: 'Administrador', variant: 'dark' },
  CASHIER: { label: 'Caja', variant: 'danger' },
  KITCHEN: { label: 'Cocina', variant: 'success' },
}
</script>

<template>
  <div class="page">
    <PageHeader :title="'Usuarios'" :subtitle="`${store.users.length} usuario${store.users.length !== 1 ? 's' : ''} registrado${store.users.length !== 1 ? 's' : ''}`">
      <template #actions>
        <BaseButton variant="dark" size="sm" @click="openCreate">
          <template #default>
            <svg width="14" height="14" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M8 3v10M3 8h10" />
            </svg>
            Nuevo Usuario
          </template>
        </BaseButton>
      </template>
    </PageHeader>

    <SearchBar v-model="searchQuery" placeholder="Buscar por nombre o email..." />

    <LoadingState v-if="store.loading && !showDrawer" message="Cargando usuarios..." />

    <EmptyState
      v-else-if="filteredUsers.length === 0 && !store.loading"
      icon="👤"
      title="Sin resultados"
      message="No se encontraron usuarios con esa búsqueda."
    />

    <div v-else class="users-grid">
      <div v-for="user in filteredUsers" :key="user.id" class="user-card">
        <div class="card-top">
          <div class="avatar">{{ getInitials(user.name, user.lastname) }}</div>
          <BaseBadge :variant="roleConfig[user.role]?.variant ?? 'default'">
            {{ roleConfig[user.role]?.label }}
          </BaseBadge>
        </div>

        <div class="card-body">
          <h3 class="user-name">{{ user.name }} {{ user.lastname }}</h3>
          <p class="user-email">{{ user.email }}</p>
          <div class="user-meta">
            <span v-if="user.phone">
              <svg width="12" height="12" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 2l2 2 1 3L4 9l3 3 2-2 3 1 2 2v3a1 1 0 01-1 1C8 17 0 11 2 3a1 1 0 011-1z"/></svg>
              {{ user.phone }}
            </span>
            <span v-if="user.ci">
              <svg width="12" height="12" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="2" y="3" width="12" height="10" rx="1"/><path d="M6 7h4M6 9h2"/></svg>
              {{ user.ci }}
            </span>
          </div>
        </div>

        <div class="card-actions">
          <BaseButton variant="secondary" size="sm" @click="openEdit(user)">Editar</BaseButton>
          <BaseButton variant="danger" size="sm" @click="handleDelete(user.id)">Eliminar</BaseButton>
        </div>
      </div>
    </div>

    <BaseDrawer v-model="showDrawer" :title="editingId ? 'Editar Usuario' : 'Nuevo Usuario'">
      <div class="form-row">
        <BaseInput v-model="form.name" label="Nombre" placeholder="Juan"
          :error="touched.has('name') ? fieldErrors.name : ''"
          @blur="markTouched('name')" />
        <BaseInput v-model="form.lastname" label="Apellido" placeholder="Pérez"
          :error="touched.has('lastname') ? fieldErrors.lastname : ''"
          @blur="markTouched('lastname')" />
      </div>

      <BaseInput
        v-model="form.email"
        label="Correo electrónico"
        placeholder="juan@pokeplace.com"
        type="email"
        :disabled="!!editingId"
        :error="touched.has('email') ? fieldErrors.email : ''"
        @blur="markTouched('email')"
      />

      <BaseInput
        v-model="form.password"
        :label="editingId ? 'Contraseña (dejar vacío para no cambiar)' : 'Contraseña'"
        type="password"
        placeholder="••••••••"
        :error="touched.has('password') ? fieldErrors.password : ''"
        @blur="markTouched('password')"
      />

      <div class="form-row">
        <BaseInput v-model="form.phone" label="Teléfono" placeholder="+591 70000000"
          :error="touched.has('phone') ? fieldErrors.phone : ''"
          @blur="markTouched('phone')" />
        <BaseInput v-model="form.ci" label="Carnet de identidad" type="number" placeholder="12345678"
          :error="touched.has('ci') ? fieldErrors.ci : ''"
          @blur="markTouched('ci')" />
      </div>

      <div class="field">
        <label>Rol</label>
        <div class="role-selector">
          <button
            v-for="(config, key) in roleConfig"
            :key="key"
            :class="['role-option', { 'role-selected': form.role === key }]"
            type="button"
            @click="form.role = key"
          >
            {{ config.label }}
          </button>
        </div>
      </div>

      <AlertBanner v-if="store.error" variant="danger" :message="store.error" />

      <template #footer>
        <BaseButton variant="secondary" block @click="closeDrawer">Cancelar</BaseButton>
        <BaseButton variant="primary" block :loading="store.loading" @click="handleSubmit">
          {{ editingId ? 'Guardar Cambios' : 'Crear Usuario' }}
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

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

.user-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border: 1.5px solid transparent;
  transition: border-color var(--transition-fast), box-shadow var(--transition-base), transform 0.2s cubic-bezier(0.22, 1, 0.36, 1);
}

.user-card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.avatar {
  width: 46px;
  height: 46px;
  background: var(--color-dark);
  color: white;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: var(--text-md);
  letter-spacing: 0.5px;
}

.card-body {
  flex: 1;
}

.user-name {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  color: var(--color-text);
  margin: 0 0 0.25rem;
}

.user-email {
  color: var(--color-text-subtle);
  font-size: var(--text-sm);
  margin: 0 0 0.5rem;
}

.user-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.user-meta span {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  background: var(--color-surface-muted);
  padding: 0.25rem 0.6rem;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
}

.user-meta svg {
  opacity: 0.5;
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

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

label {
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--color-text-subtle);
}

.role-selector {
  display: flex;
  gap: 0.5rem;
}

.role-option {
  flex: 1;
  padding: 0.6rem 0.5rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  font-family: var(--font-body);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  text-align: center;
}

.role-option:hover {
  border-color: var(--color-dark);
  color: var(--color-text);
}

.role-selected {
  background: var(--color-dark);
  border-color: var(--color-dark);
  color: white !important;
}

@media (max-width: 600px) {
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
