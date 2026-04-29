<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useUsersStore } from '../stores/user'

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

const filteredUsers = computed(() =>
  store.users.filter((u) =>
    `${u.name} ${u.lastname} ${u.email}`.toLowerCase().includes(searchQuery.value.toLowerCase()),
  ),
)

function openCreate() {
  editingId.value = null
  form.value = { ...emptyForm }
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
  showDrawer.value = true
}

function closeDrawer() {
  showDrawer.value = false
  editingId.value = null
  form.value = { ...emptyForm }
}

async function handleSubmit() {
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

const roleConfig: Record<string, { label: string; class: string }> = {
  ADMIN: { label: 'Administrador', class: 'role-admin' },
  CASHIER: { label: 'Caja', class: 'role-cashier' },
  KITCHEN: { label: 'Cocina', class: 'role-kitchen' },
}
</script>

<template>
  <div class="page">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1>Usuarios</h1>
        <p>
          {{ store.users.length }} usuario{{ store.users.length !== 1 ? 's' : '' }} registrado{{
            store.users.length !== 1 ? 's' : ''
          }}
        </p>
      </div>
      <button class="btn-new" @click="openCreate">
        <span class="btn-icon">+</span>
        Nuevo Usuario
      </button>
    </div>

    <!-- Search -->
    <div class="search-bar">
      <span class="search-icon">⌕</span>
      <input v-model="searchQuery" type="text" placeholder="Buscar por nombre o email..." />
    </div>

    <!-- Loading -->
    <div v-if="store.loading && !showDrawer" class="loading-state">
      <div class="spinner"></div>
      <span>Cargando usuarios...</span>
    </div>

    <!-- Empty -->
    <div v-else-if="filteredUsers.length === 0" class="empty-state">
      <div class="empty-icon">👤</div>
      <h3>Sin resultados</h3>
      <p>No se encontraron usuarios con esa búsqueda.</p>
    </div>

    <!-- User Cards Grid -->
    <div v-else class="users-grid">
      <div v-for="user in filteredUsers" :key="user.id" class="user-card">
        <div class="card-top">
          <div class="avatar">{{ getInitials(user.name, user.lastname) }}</div>
          <span :class="['role-badge', roleConfig[user.role]?.class]">
            {{ roleConfig[user.role]?.label }}
          </span>
        </div>

        <div class="card-body">
          <h3 class="user-name">{{ user.name }} {{ user.lastname }}</h3>
          <p class="user-email">{{ user.email }}</p>
          <div class="user-meta">
            <span v-if="user.phone">📞 {{ user.phone }}</span>
            <span v-if="user.ci">🪪 {{ user.ci }}</span>
          </div>
        </div>

        <div class="card-actions">
          <button class="btn-edit" @click="openEdit(user)">Editar</button>
          <button class="btn-delete" @click="handleDelete(user.id)">Eliminar</button>
        </div>
      </div>
    </div>

    <!-- Drawer Overlay -->
    <Transition name="fade">
      <div v-if="showDrawer" class="overlay" @click="closeDrawer" />
    </Transition>

    <!-- Drawer -->
    <Transition name="slide">
      <div v-if="showDrawer" class="drawer">
        <div class="drawer-header">
          <h2>{{ editingId ? 'Editar Usuario' : 'Nuevo Usuario' }}</h2>
          <button class="btn-close" @click="closeDrawer">✕</button>
        </div>

        <div class="drawer-body">
          <div class="form-row">
            <div class="field">
              <label>Nombre</label>
              <input v-model="form.name" type="text" placeholder="Juan" />
            </div>
            <div class="field">
              <label>Apellido</label>
              <input v-model="form.lastname" type="text" placeholder="Pérez" />
            </div>
          </div>

          <div class="field">
            <label>Correo electrónico</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="juan@pokeplace.com"
              :disabled="!!editingId"
            />
          </div>

          <div class="field">
            <label>Contraseña {{ editingId ? '(dejar vacío para no cambiar)' : '' }}</label>
            <input v-model="form.password" type="password" placeholder="••••••••" />
          </div>

          <div class="form-row">
            <div class="field">
              <label>Teléfono</label>
              <input v-model="form.phone" type="text" placeholder="+591 70000000" />
            </div>
            <div class="field">
              <label>Carnet de identidad</label>
              <input v-model="form.ci" type="number" placeholder="12345678" />
            </div>
          </div>

          <div class="field">
            <label>Rol</label>
            <div class="role-selector">
              <button
                v-for="(config, key) in roleConfig"
                :key="key"
                :class="['role-option', form.role === key ? 'role-selected' : '']"
                type="button"
                @click="form.role = key"
              >
                {{ config.label }}
              </button>
            </div>
          </div>

          <p v-if="store.error" class="error">⚠ {{ store.error }}</p>
        </div>

        <div class="drawer-footer">
          <button class="btn-cancel" @click="closeDrawer">Cancelar</button>
          <button class="btn-save" :disabled="store.loading" @click="handleSubmit">
            <span v-if="store.loading" class="spinner-sm"></span>
            {{ store.loading ? 'Guardando...' : editingId ? 'Guardar Cambios' : 'Crear Usuario' }}
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.page {
  padding: 2rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  min-height: 100vh;
  background: #f7f7f7;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1.5rem;
}

.header-left h1 {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 2rem;
  font-weight: 900;
  color: #111;
  margin: 0 0 0.2rem;
  letter-spacing: -0.5px;
}

.header-left p {
  color: #999;
  font-size: 0.9rem;
  margin: 0;
}

.btn-new {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #111;
  color: white;
  border: none;
  padding: 0.75rem 1.4rem;
  border-radius: 8px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition:
    background 0.2s,
    transform 0.1s;
}

.btn-new:hover {
  background: #e02020;
}
.btn-new:active {
  transform: scale(0.98);
}

.btn-icon {
  font-size: 1.2rem;
  line-height: 1;
  font-weight: 300;
}

/* Search */
.search-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: white;
  border: 1.5px solid #e8e8e8;
  border-radius: 10px;
  padding: 0.7rem 1rem;
  margin-bottom: 1.5rem;
  transition: border-color 0.2s;
}

.search-bar:focus-within {
  border-color: #111;
}

.search-icon {
  font-size: 1.2rem;
  color: #999;
}

.search-bar input {
  border: none;
  outline: none;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 0.95rem;
  color: #111;
  width: 100%;
  background: transparent;
}

.search-bar input::placeholder {
  color: #bbb;
}

/* States */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 4rem;
  color: #999;
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #999;
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}
.empty-state h3 {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #111;
  margin: 0 0 0.5rem;
}
.empty-state p {
  margin: 0;
  font-size: 0.9rem;
}

/* Grid */
.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}

/* User Card */
.user-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border: 1.5px solid transparent;
  transition:
    border-color 0.2s,
    box-shadow 0.2s,
    transform 0.15s;
}

.user-card:hover {
  border-color: #e8e8e8;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transform: translateY(-2px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.avatar {
  width: 48px;
  height: 48px;
  background: #111;
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1rem;
  letter-spacing: 0.5px;
}

.role-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.72rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-admin {
  background: #111;
  color: white;
}
.role-cashier {
  background: #fee2e2;
  color: #991b1b;
}
.role-kitchen {
  background: #f0fdf4;
  color: #166534;
}

.card-body {
  flex: 1;
}

.user-name {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: #111;
  margin: 0 0 0.3rem;
}

.user-email {
  color: #888;
  font-size: 0.85rem;
  margin: 0 0 0.5rem;
}

.user-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.user-meta span {
  font-size: 0.8rem;
  color: #666;
  background: #f5f5f5;
  padding: 0.2rem 0.6rem;
  border-radius: 6px;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid #f0f0f0;
}

.btn-edit,
.btn-delete {
  flex: 1;
  padding: 0.5rem;
  border-radius: 7px;
  font-size: 0.85rem;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-edit {
  background: #f5f5f5;
  border: none;
  color: #111;
}

.btn-edit:hover {
  background: #111;
  color: white;
}

.btn-delete {
  background: transparent;
  border: 1.5px solid #fee2e2;
  color: #e02020;
}

.btn-delete:hover {
  background: #e02020;
  color: white;
  border-color: #e02020;
}

/* Overlay */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  z-index: 100;
}

/* Drawer */
.drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: 420px;
  background: white;
  z-index: 101;
  display: flex;
  flex-direction: column;
  box-shadow: -8px 0 40px rgba(0, 0, 0, 0.12);
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-header h2 {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 1.3rem;
  font-weight: 700;
  color: #111;
  margin: 0;
}

.btn-close {
  background: #f5f5f5;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #666;
  transition: all 0.15s;
}

.btn-close:hover {
  background: #111;
  color: white;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
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
  font-size: 0.78rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: #888;
}

input {
  padding: 0.75rem 1rem;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 0.95rem;
  color: #111;
  outline: none;
  transition: border-color 0.2s;
  background: white;
}

input:focus {
  border-color: #111;
}
input:disabled {
  background: #f7f7f7;
  color: #aaa;
  cursor: not-allowed;
}

/* Role Selector */
.role-selector {
  display: flex;
  gap: 0.5rem;
}

.role-option {
  flex: 1;
  padding: 0.6rem 0.5rem;
  border: 1.5px solid #e8e8e8;
  border-radius: 8px;
  background: white;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 0.82rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.15s;
  text-align: center;
}

.role-option:hover {
  border-color: #111;
  color: #111;
}
.role-selected {
  background: #111;
  border-color: #111;
  color: white !important;
}

.error {
  background: #fff0f0;
  border-left: 3px solid #e02020;
  color: #e02020;
  padding: 0.75rem 1rem;
  border-radius: 6px;
  font-size: 0.85rem;
}

.drawer-footer {
  display: flex;
  gap: 0.75rem;
  padding: 1.25rem 2rem;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel {
  flex: 1;
  padding: 0.75rem;
  background: #f5f5f5;
  border: none;
  border-radius: 8px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 0.9rem;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: background 0.15s;
}

.btn-cancel:hover {
  background: #e8e8e8;
}

.btn-save {
  flex: 2;
  padding: 0.75rem;
  background: #e02020;
  border: none;
  border-radius: 8px;
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: background 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-save:hover:not(:disabled) {
  background: #c01010;
}
.btn-save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Spinner */
.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #e8e8e8;
  border-top-color: #111;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.spinner-sm {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

/* Responsive */
@media (max-width: 600px) {
  .drawer {
    width: 100%;
  }
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
