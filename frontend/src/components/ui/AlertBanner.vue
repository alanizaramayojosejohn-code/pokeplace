<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  variant?: 'danger' | 'warning' | 'info' | 'success'
  message: string
}>()

const iconSvg = computed(() => {
  if (props.variant === 'success') {
    return '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 8l3 3 7-7"/></svg>'
  }
  return '<svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="1.5"><circle cx="8" cy="8" r="6"/><path d="M8 5v3M8 10.5v.5"/></svg>'
})
</script>

<template>
  <div :class="['alert', `alert-${variant ?? 'danger'}`]">
    <span class="alert-icon" v-html="iconSvg" />
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
}

.alert-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.alert-icon :deep(svg) {
  width: 16px;
  height: 16px;
}

.alert-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
.alert-warning {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}
.alert-info {
  background: var(--color-info-soft);
  color: var(--color-info);
}
.alert-success {
  background: var(--color-success-soft);
  color: var(--color-success);
}
</style>
