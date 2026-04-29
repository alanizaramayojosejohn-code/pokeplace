<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter, RouterLink } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

const isAdmin = computed(() => authStore.userRole === 'ADMIN')
const isCashier = computed(() => authStore.userRole === 'CASHIER')

const navItems = computed(() => [
  { to: '/dashboard', icon: '⊞', label: 'Inicio', show: true },
  { to: '/orders', icon: '🧾', label: 'Órdenes', show: isAdmin.value || isCashier.value },
  { to: '/clients', icon: '👥', label: 'Clientes', show: isAdmin.value || isCashier.value },
  { to: '/products', icon: '📦', label: 'Productos', show: isAdmin.value },
  { to: '/categories', icon: '🗂', label: 'Categorías', show: isAdmin.value },
  { to: '/users', icon: '✦', label: 'Usuarios', show: isAdmin.value },
])
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-mark">P</div>
        <span class="brand">Poke<span>Place</span></span>
      </div>

      <nav class="nav">
        <template v-for="item in navItems" :key="item.to">
          <RouterLink v-if="item.show" class="nav-item" :to="item.to">
            <span class="nav-icon">{{ item.icon }}</span> {{ item.label }}
          </RouterLink>
        </template>
      </nav>

      <div class="sidebar-footer">
        <div class="user-card">
          <div class="avatar">{{ authStore.user?.name?.charAt(0) }}</div>
          <div class="user-info">
            <span class="user-name">{{ authStore.user?.name }}</span>
            <span class="user-role">{{ authStore.user?.role }}</span>
          </div>
        </div>
        <button class="logout-btn" title="Cerrar sesión" @click="handleLogout">→</button>
      </div>
    </aside>

    <main class="content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
  background: var(--color-bg);
}

.sidebar {
  width: var(--sidebar-width);
  min-width: var(--sidebar-width);
  background: var(--color-dark);
  display: flex;
  flex-direction: column;
  padding: 1.5rem 1rem;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 2.5rem;
  padding: 0 0.5rem;
}

.logo-mark {
  width: 36px;
  height: 36px;
  background: var(--color-primary);
  color: white;
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.brand {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  color: white;
}

.brand span {
  color: var(--color-primary);
}

.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.7rem 0.9rem;
  border-radius: var(--radius-sm);
  color: #888;
  text-decoration: none;
  font-size: var(--text-base);
  transition: all var(--transition-fast);
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.06);
  color: white;
}
.nav-item.router-link-active {
  background: var(--color-primary);
  color: white;
}
.nav-icon {
  font-size: var(--text-md);
  width: 20px;
  text-align: center;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 0.5rem 0;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 0.5rem;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex: 1;
  min-width: 0;
}

.avatar {
  width: 34px;
  height: 34px;
  background: var(--color-primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: var(--text-sm);
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  color: white;
  font-size: var(--text-sm);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  color: #666;
  font-size: var(--text-xs);
}

.logout-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #888;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.logout-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.content {
  margin-left: var(--sidebar-width);
  flex: 1;
  min-height: 100vh;
  width: calc(100% - var(--sidebar-width));
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    position: relative;
    height: auto;
  }
  .content {
    margin-left: 0;
    width: 100%;
  }
  .layout {
    flex-direction: column;
  }
}
</style>
