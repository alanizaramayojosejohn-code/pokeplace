<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter, RouterLink } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-mark">P</div>
        <span class="brand">Poke<span>Place</span></span>
      </div>

      <nav class="nav">
        <RouterLink class="nav-item" to="/dashboard">
          <span class="nav-icon">⊞</span> Inicio
        </RouterLink>
        <RouterLink class="nav-item" to="/orders">
          <span class="nav-icon">◈</span> Ordenes
        </RouterLink>
        <RouterLink v-if="authStore.userRole === 'ADMIN'" class="nav-item" to="/users">
          <span class="nav-icon">✦</span> Usuarios
        </RouterLink>
      </nav>

      <div class="sidebar-footer">
        <div class="user-card">
          <div class="avatar">{{ authStore.user?.name?.charAt(0) }}</div>
          <div class="user-info">
            <span class="user-name">{{ authStore.user?.name }}</span>
            <span class="user-role">{{ authStore.user?.role }}</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">→</button>
      </div>
    </aside>

    <main class="content">
      <!-- Aquí se renderizan todas las vistas -->
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@700;900&family=DM+Sans:wght@400;500&display=swap');

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

.layout {
  display: flex;
  min-height: 100vh;
  width: 100vw;
  font-family: 'DM Sans', sans-serif;
  background: #f5f5f5;
}

.sidebar {
  width: 240px;
  min-width: 240px;
  background: #111;
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
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
}

.logo-mark {
  width: 36px;
  height: 36px;
  background: #e02020;
  color: white;
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  flex-shrink: 0;
}

.brand {
  font-family: 'Playfair Display', serif;
  font-size: 1.2rem;
  font-weight: 700;
  color: white;
}

.brand span {
  color: #e02020;
}

.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.7rem 0.9rem;
  border-radius: 4px;
  color: #888;
  text-decoration: none;
  font-size: 0.9rem;
  transition: all 0.15s;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.07);
  color: white;
}
.nav-item.router-link-active {
  background: #e02020;
  color: white;
}
.nav-icon {
  font-size: 1rem;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
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
  background: #e02020;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.9rem;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  color: white;
  font-size: 0.85rem;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  color: #666;
  font-size: 0.75rem;
}

.logout-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: #888;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s;
}

.logout-btn:hover {
  border-color: #e02020;
  color: #e02020;
}

.content {
  margin-left: 240px;
  flex: 1;
  min-height: 100vh;
  width: calc(100vw - 240px);
}

@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    position: relative;
    height: auto;
  }
  .content {
    margin-left: 0;
    width: 100vw;
  }
  .layout {
    flex-direction: column;
  }
}
</style>
