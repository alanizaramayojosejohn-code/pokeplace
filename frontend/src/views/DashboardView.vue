<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="dashboard">
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="logo-mark">P</div>
        <span class="brand">Poke<span>Place</span></span>
      </div>

      <nav class="nav">
        <a class="nav-item active" href="#"> <span class="nav-icon">⊞</span> Dashboard </a>
        <a class="nav-item" href="#"> <span class="nav-icon">◈</span> Orders </a>
        <a class="nav-item" href="#"> <span class="nav-icon">☰</span> Menu </a>
        <a v-if="authStore.userRole === 'ADMIN'" class="nav-item" href="#">
          <span class="nav-icon">✦</span> Users
        </a>
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

    <main class="main">
      <div class="main-header">
        <div>
          <h1>Dashboard</h1>
          <p>Welcome back, {{ authStore.user?.name }}!</p>
        </div>
        <div class="role-badge">{{ authStore.user?.role }}</div>
      </div>

      <div class="stats">
        <div class="stat-card">
          <span class="stat-label">Active Orders</span>
          <span class="stat-value">0</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Menu Items</span>
          <span class="stat-value">0</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Today's Revenue</span>
          <span class="stat-value">$0</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">Users</span>
          <span class="stat-value">1</span>
        </div>
      </div>
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

.dashboard {
  display: flex;
  min-height: 100vh;
  font-family: 'DM Sans', sans-serif;
  background: #f5f5f5;
  width: 100vw;
}

/* Sidebar */
.sidebar {
  width: 240px;
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
.nav-item.active {
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

/* Main */
.main {
  margin-left: 240px;
  flex: 1;
  padding: 2rem;
  min-height: 100vh; /* ← agrega esto */
  width: calc(100% - 240px); /* ← agrega esto */
}

.main-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 2rem;
}

.main-header h1 {
  font-family: 'Playfair Display', serif;
  font-size: 1.8rem;
  font-weight: 700;
  color: #111;
  margin-bottom: 0.2rem;
}

.main-header p {
  color: #888;
  font-size: 0.9rem;
}

.role-badge {
  background: #111;
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 4px;
  font-size: 0.75rem;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 1rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  border-left: 3px solid #e02020;
}

.stat-label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: #888;
}

.stat-value {
  font-family: 'Playfair Display', serif;
  font-size: 2rem;
  font-weight: 700;
  color: #111;
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    position: relative;
    height: auto;
  }
  .main {
    margin-left: 0;
  }
  .dashboard {
    flex-direction: column;
  }
}
</style>
