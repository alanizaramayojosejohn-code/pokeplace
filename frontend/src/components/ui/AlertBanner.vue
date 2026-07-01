<script setup lang="ts">
defineProps<{
  variant?: 'danger' | 'warning' | 'info' | 'success'
  message: string
}>()
</script>

<template>
  <div :class="['alert', `alert-${variant ?? 'danger'}`]">
    <span class="alert-icon">⚠</span>
    <span>{{ message }}</span>
  </div>
</template>

<style scoped>
.alert {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.75rem 1rem;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  border-left: 3px solid;
}

.alert-icon {
  flex-shrink: 0;
  font-size: var(--text-md);
}

.alert-danger {
  background: var(--color-primary-soft);
  color: var(--color-danger);
  border-color: var(--color-danger);
}
.alert-warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
  border-color: var(--color-warning);
}
.alert-info {
  background: var(--color-info-soft);
  color: var(--color-info);
  border-color: var(--color-info);
}
.alert-success {
  background: var(--color-success-soft);
  color: var(--color-success);
  border-color: var(--color-success);
}
</style>
