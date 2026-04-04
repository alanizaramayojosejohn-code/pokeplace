<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useUsersStore } from '../stores/user'

const store = useUsersStore()

// Estado del formulario
const showForm = ref(false)
const editingId = ref<number | null>(null)

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

// Abre el formulario para editar cargando los datos del usuario
function handleEdit(user: any) {
  editingId.value = user.id
  form.value = {
    name: user.name,
    lastname: user.lastname,
    email: user.email,
    password: '',
    phone: user.phone,
    ci: user.ci,
    role: user.role,
  }
  showForm.value = true
}

function handleCancel() {
  showForm.value = false
  editingId.value = null
  form.value = { ...emptyForm }
}

async function handleSubmit() {
  if (editingId.value) {
    await store.updateUser(editingId.value, form.value)
  } else {
    await store.createUser(form.value)
  }
  if (!store.error) {
    handleCancel()
  }
}

async function handleDelete(id: number) {
  if (confirm('Are you sure you want to delete this user?')) {
    await store.deleteUser(id)
  }
}

const roleColors: Record<string, string> = {
  ADMIN: 'role-admin',
  CASHIER: 'role-cashier',
  KITCHEN: 'role-kitchen',
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>Users</h1>
        <p>Manage system users and roles</p>
      </div>
      <button class="btn-primary" @click="showForm ? handleCancel() : (showForm = true)">
        {{ showForm ? 'Cancel' : '+ New User' }}
      </button>
    </div>

    <!-- Formulario crear/editar -->
    <div v-if="showForm" class="form-card">
      <h2>{{ editingId ? 'Edit User' : 'New User' }}</h2>
      <div class="form-grid">
        <div class="field">
          <label>Name</label>
          <input v-model="form.name" type="text" placeholder="John" />
        </div>
        <div class="field">
          <label>Lastname</label>
          <input v-model="form.lastname" type="text" placeholder="Doe" />
        </div>
        <div class="field">
          <label>Email</label>
          <input
            v-model="form.email"
            type="email"
            placeholder="john@pokeplace.com"
            :disabled="!!editingId"
          />
        </div>
        <div class="field">
          <label>Password {{ editingId ? '(leave blank to keep)' : '' }}</label>
          <input v-model="form.password" type="password" placeholder="••••••••" />
        </div>
        <div class="field">
          <label>Phone</label>
          <input v-model="form.phone" type="text" placeholder="+591 70000000" />
        </div>
        <div class="field">
          <label>CI</label>
          <input v-model="form.ci" type="number" placeholder="12345678" />
        </div>
        <div class="field">
          <label>Role</label>
          <select v-model="form.role">
            <option value="ADMIN">Admin</option>
            <option value="CASHIER">Cashier</option>
            <option value="KITCHEN">Kitchen</option>
          </select>
        </div>
      </div>
      <p v-if="store.error" class="error">{{ store.error }}</p>
      <button class="btn-primary" :disabled="store.loading" @click="handleSubmit">
        {{ store.loading ? 'Saving...' : editingId ? 'Save Changes' : 'Create User' }}
      </button>
    </div>

    <!-- Tabla -->
    <div class="table-card">
      <div v-if="store.loading && !showForm" class="loading">Loading...</div>
      <table v-else>
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>CI</th>
            <th>Role</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="store.users.length === 0">
            <td colspan="6" class="empty">No users found</td>
          </tr>
          <tr v-for="user in store.users" :key="user.id">
            <td>{{ user.name }} {{ user.lastname }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.phone || '—' }}</td>
            <td>{{ user.ci || '—' }}</td>
            <td>
              <span :class="['role-badge', roleColors[user.role]]">
                {{ user.role }}
              </span>
            </td>
            <td class="actions">
              <button class="btn-edit" @click="handleEdit(user)">Edit</button>
              <button class="btn-delete" @click="handleDelete(user.id)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>
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

.btn-primary {
  background: #e02020;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #c01010;
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-card {
  background: white;
  padding: 1.5rem;
  border-radius: 6px;
  margin-bottom: 1.5rem;
  border-left: 3px solid #e02020;
}

.form-card h2 {
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  margin: 0 0 1rem;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

label {
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #111;
}

input,
select {
  padding: 0.6rem 0.8rem;
  border: 1.5px solid #e0e0e0;
  border-radius: 4px;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  outline: none;
  transition: border-color 0.2s;
}

input:focus,
select:focus {
  border-color: #e02020;
}
input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.table-card {
  background: white;
  border-radius: 6px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}
thead {
  background: #111;
  color: white;
}

th {
  padding: 0.9rem 1rem;
  text-align: left;
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: 500;
}

td {
  padding: 0.9rem 1rem;
  border-bottom: 1px solid #f0f0f0;
  font-size: 0.9rem;
  color: #333;
}

tr:last-child td {
  border-bottom: none;
}
tr:hover td {
  background: #fafafa;
}

.role-badge {
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.role-admin {
  background: #111;
  color: white;
}
.role-cashier {
  background: #e02020;
  color: white;
}
.role-kitchen {
  background: #f0f0f0;
  color: #333;
}

.actions {
  display: flex;
  gap: 0.5rem;
}

.btn-edit {
  background: transparent;
  border: 1px solid #111;
  color: #111;
  padding: 0.3rem 0.8rem;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-edit:hover {
  background: #111;
  color: white;
}

.btn-delete {
  background: transparent;
  border: 1px solid #e02020;
  color: #e02020;
  padding: 0.3rem 0.8rem;
  border-radius: 4px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-delete:hover {
  background: #e02020;
  color: white;
}

.empty {
  text-align: center;
  color: #888;
  padding: 2rem !important;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #888;
}

.error {
  color: #e02020;
  font-size: 0.85rem;
  margin: 0.5rem 0;
}
</style>
