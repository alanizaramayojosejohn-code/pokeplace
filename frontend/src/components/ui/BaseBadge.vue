<script setup lang="ts">
defineProps<{
  variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'dark'
}>()
</script>

<template>
  <span :class="['badge', `badge-${variant ?? 'default'}`]">
    <slot />
  </span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.25rem 0.7rem;
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.badge-default {
  background: var(--color-surface-muted);
  color: var(--color-text-muted);
}
.badge-primary {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}
.badge-dark {
  background: var(--color-dark);
  color: white;
}
.badge-success {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.badge-warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}
.badge-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
.badge-info {
  background: var(--color-info-soft);
  color: var(--color-info);
}
</style>
