<script setup lang="ts">
import { useAuthStore } from '../stores/auth'
import AdminDashboard from '../components/dashboard/AdminDashboard.vue'
import CashierDashboard from '../components/dashboard/CashierDashboard.vue'
import KitchenDashboard from '../components/dashboard/KitchenDashboard.vue'

const authStore = useAuthStore()
const role = authStore.user?.role
</script>

<template>
  <AdminDashboard v-if="role === 'ADMIN'" />
  <CashierDashboard v-else-if="role === 'CASHIER'" />
  <KitchenDashboard v-else-if="role === 'KITCHEN'" />
</template>
