<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useRouter, RouterLink } from 'vue-router'
const logoSrc = new URL('../assets/Logo.webp', import.meta.url).href

const authStore = useAuthStore()
const router = useRouter()
const menuOpen = ref(false)

function closeMenu() {
  menuOpen.value = false
}

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}

const isAdmin = computed(() => authStore.userRole === 'ADMIN')
const isCashier = computed(() => authStore.userRole === 'CASHIER')

const icons: Record<string, string> = {
  dashboard: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M2 10l8-8 8 8"/><path d="M4 8v8a1 1 0 001 1h3v-4h4v4h3a1 1 0 001-1V8"/></svg>',
  orders: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><rect x="3" y="2" width="14" height="16" rx="2"/><path d="M7 7h6M7 11h6M7 15h4"/></svg>',
  clients: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="10" cy="7" r="3"/><path d="M4 17c0-3.3 2.7-6 6-6s6 2.7 6 6"/></svg>',
  products: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M3 7l7-4 7 4v6l-7 4-7-4V7z"/><path d="M10 11L3 7"/><path d="M17 7l-7 4"/><path d="M10 11v6"/></svg>',
  categories: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 4h5l2 2h5a1 1 0 011 1v7a1 1 0 01-1 1H4a1 1 0 01-1-1V5a1 1 0 011-1z"/></svg>',
  users: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="10" cy="6" r="2.5"/><path d="M4 17c0-3.3 2.7-6 6-6s6 2.7 6 6"/></svg>',
  reports: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 4h12v12H4z"/><path d="M8 8v4M12 10v2M10 6v6"/></svg>',
  closing: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M4 14l4-4 3 3 5-5"/><path d="M16 8V4h-4"/><path d="M4 4h12"/></svg>',
  audit: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="9" cy="9" r="4.5"/><path d="M14 14l3 3"/></svg>',
  stock: '<svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M5 7h10v7a2 2 0 01-2 2H7a2 2 0 01-2-2V7z"/><path d="M7 7V5a3 3 0 016 0v2"/><path d="M10 11v2"/></svg>',
}

interface NavGroup {
  label: string
  items: { to: string; icon: string; label: string }[]
}

const navGroups = computed<NavGroup[]>(() => {
  const g: NavGroup[] = []

  const general = { to: '/dashboard', icon: icons.dashboard, label: 'Inicio' }
  const ops = isAdmin.value || isCashier.value ? [
    { to: '/orders', icon: icons.orders, label: 'Órdenes' },
    { to: '/clients', icon: icons.clients, label: 'Clientes' },
  ] : []
  const admin = [
    { to: '/products', icon: icons.products, label: 'Productos' },
    { to: '/categories', icon: icons.categories, label: 'Categorías' },
    { to: '/users', icon: icons.users, label: 'Usuarios' },
  ]
  const reports = [
    { to: '/reports', icon: icons.reports, label: 'Reporte Ventas' },
    { to: '/management-closing', icon: icons.closing, label: 'Cierre Gestión' },
    { to: '/audit', icon: icons.audit, label: 'Auditoría' },
    { to: '/stock-notes', icon: icons.stock, label: 'Reposición' },
  ]

  g.push({ label: 'General', items: [general] })
  if (ops.length) g.push({ label: 'Operaciones', items: ops })
  if (isAdmin.value) g.push({ label: 'Admin', items: admin })
  if (isAdmin.value) g.push({ label: 'Informes', items: reports })
  return g
})
</script>

<template>
  <div class="layout">
    <header class="topbar">
      <div class="topbar-brand">
        <img class="logo-img" :src="logoSrc" alt="PokePlace" />
        <span class="brand">Poke<span>Place</span></span>
      </div>
      <button
        class="hamburger"
        :class="{ 'hamburger-open': menuOpen }"
        :aria-expanded="menuOpen"
        aria-label="Menú"
        @click="menuOpen = !menuOpen"
      >
        <span />
        <span />
        <span />
      </button>
    </header>

    <Transition name="fade-overlay">
      <div v-if="menuOpen" class="mobile-overlay" @click="closeMenu" />
    </Transition>

    <aside class="sidebar" :class="{ 'sidebar-open': menuOpen }">
      <div class="sidebar-header">
        <img class="logo-img" :src="logoSrc" alt="PokePlace" />
        <div class="brand-text">
          <span class="brand">Poke</span>
          <span class="brand-accent">Place</span>
        </div>
      </div>

      <nav class="nav">
        <template v-for="group in navGroups" :key="group.label">
          <div class="nav-divider">
            <span class="nav-divider-label">{{ group.label }}</span>
          </div>
          <RouterLink
            v-for="item in group.items"
            :key="item.to"
            class="nav-item"
            :to="item.to"
            @click="closeMenu"
          >
            <span class="nav-icon" v-html="item.icon" />
            <span class="nav-text">{{ item.label }}</span>
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
        <button class="logout-btn" title="Cerrar sesión" @click="handleLogout">
          <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M7 15H4a1 1 0 01-1-1V4a1 1 0 011-1h3" />
            <path d="M12 12l3-3-3-3" />
            <path d="M15 9H7" />
          </svg>
        </button>
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

.topbar {
  display: none;
}

.sidebar {
  width: var(--sidebar-width);
  min-width: var(--sidebar-width);
  background: linear-gradient(165deg, #141517 0%, #1a1b1e 100%);
  display: flex;
  flex-direction: column;
  padding: 1.5rem 0.75rem;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 300;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0 0.5rem 1.5rem;
  margin: 0 0.25rem 0.5rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-img {
  width: 34px;
  height: 34px;
  object-fit: contain;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.brand {
  font-family: var(--font-display);
  font-size: var(--text-md);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: -0.01em;
}

.brand-accent {
  font-family: var(--font-display);
  font-size: var(--text-md);
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.01em;
}

.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  padding: 0.5rem 0;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.08) transparent;
}

.nav::-webkit-scrollbar {
  width: 4px;
}

.nav::-webkit-scrollbar-track {
  background: transparent;
}

.nav::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.08);
  border-radius: 4px;
}

.nav::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.15);
}

.nav-divider {
  display: flex;
  align-items: center;
  padding: 0.75rem 0.75rem 0.375rem;
  margin-top: 0.375rem;
}

.nav-divider:first-child {
  margin-top: 0;
}

.nav-divider-label {
  font-size: 0.65rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.2);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.55rem 0.75rem;
  border-radius: var(--radius-sm);
  color: rgba(255, 255, 255, 0.45);
  text-decoration: none;
  font-size: var(--text-sm);
  position: relative;
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s cubic-bezier(0.22, 1, 0.36, 1);
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.85);
  transform: translateX(2px);
}

.nav-item.router-link-active {
  color: white;
  background: rgba(224, 32, 32, 0.12);
}

.nav-item.router-link-active::before {
  content: '';
  position: absolute;
  left: -0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  background: var(--color-primary);
  border-radius: 0 3px 3px 0;
}

.nav-icon {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.nav-icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.nav-text {
  font-weight: 470;
  letter-spacing: -0.01em;
}

.sidebar-footer {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem 0.5rem 0;
  margin: 0.25rem 0.25rem 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.user-card {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  flex: 1;
  min-width: 0;
}

.avatar {
  width: 32px;
  height: 32px;
  background: rgba(224, 32, 32, 0.2);
  color: var(--color-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--text-xs);
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  color: rgba(255, 255, 255, 0.8);
  font-size: var(--text-sm);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  color: rgba(255, 255, 255, 0.25);
  font-size: 0.65rem;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.logout-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.2);
  width: 30px;
  height: 30px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: color 0.2s ease, background 0.2s ease;
}

.logout-btn:hover {
  color: var(--color-primary);
  background: rgba(224, 32, 32, 0.1);
}

.logout-btn svg {
  width: 16px;
  height: 16px;
}

.content {
  margin-left: var(--sidebar-width);
  flex: 1;
  min-height: 100vh;
  width: calc(100% - var(--sidebar-width));
}

.mobile-overlay {
  display: none;
}

@media (max-width: 768px) {
  .layout {
    flex-direction: column;
  }

  .topbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 56px;
    background: #141517;
    padding: 0 1rem;
    z-index: 200;
  }

  .topbar-brand {
    display: flex;
    align-items: center;
    gap: 0.6rem;
  }

  .topbar .logo-img {
    width: 30px;
    height: 30px;
  }

  .topbar .brand {
    font-family: var(--font-display);
    font-size: var(--text-lg);
    font-weight: 600;
    color: rgba(255, 255, 255, 0.9);
  }

  .topbar .brand span {
    color: var(--color-primary);
    font-weight: 700;
  }

  .hamburger {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 5px;
    width: 36px;
    height: 36px;
    background: transparent;
    border: none;
    cursor: pointer;
    padding: 4px;
    border-radius: var(--radius-sm);
  }

  .hamburger span {
    display: block;
    height: 2px;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 2px;
    transform-origin: center;
    transition: transform 0.25s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.2s ease;
  }

  .hamburger-open span:nth-child(1) {
    transform: translateY(7px) rotate(45deg);
  }
  .hamburger-open span:nth-child(2) {
    opacity: 0;
    transform: scaleX(0);
  }
  .hamburger-open span:nth-child(3) {
    transform: translateY(-7px) rotate(-45deg);
  }

  .sidebar {
    width: 280px;
    min-width: 0;
    transform: translateX(-100%);
    transition: transform 0.35s cubic-bezier(0.22, 1, 0.36, 1);
    top: 0;
    padding-top: 1.5rem;
  }

  .sidebar-open {
    transform: translateX(0);
  }

  .mobile-overlay {
    display: block;
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 250;
    backdrop-filter: blur(4px);
  }

  .content {
    margin-left: 0;
    width: 100%;
    padding-top: 56px;
    min-height: calc(100vh - 56px);
  }
}

.fade-overlay-enter-active,
.fade-overlay-leave-active {
  transition: opacity 0.3s ease;
}
.fade-overlay-enter-from,
.fade-overlay-leave-to {
  opacity: 0;
}
</style>
