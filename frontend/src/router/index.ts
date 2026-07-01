import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import AppLayout from '../layouts/AppLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/',
      component: AppLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue'),
        },
        {
          path: 'users',
          name: 'users',
          component: () => import('../views/UserView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'categories',
          name: 'categories',
          component: () => import('../views/CategoryView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'products',
          name: 'products',
          component: () => import('../views/ProductView.vue'),
          meta: { requiresAdmin: true },
        },
        {
          path: 'clients',
          name: 'clients',
          component: () => import('../views/ClientView.vue'),
        },
        {
          path: 'orders',
          name: 'orders',
          component: () => import('../views/OrderView.vue'),
        },
        {
          path: 'reports',
          name: 'reports',
          component: () => import('../views/SalesReportView.vue'),
          meta: { requiresAdmin: true },
        },
        {
        path: 'audit',
        name: 'audit',
        component: () => import('../views/AuditView.vue'),
        meta: { requiresAdmin: true },
        },
        {
          path: 'stock-notes',
          name: 'stock-notes',
          component: () => import('../views/StockNoteView.vue'),
        },
        {
          path: 'management-closing',
          name: 'management-closing',
          component: () => import('../views/ManagementClosingView.vue'),
          meta: { requiresAdmin: true },
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login' }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'dashboard' }
  }

  if (to.meta.requiresAdmin && authStore.userRole !== 'ADMIN') {
    return { name: 'dashboard' }
  }
})

export default router
