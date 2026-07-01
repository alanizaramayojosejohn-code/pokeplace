<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'dark'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  type?: 'button' | 'submit' | 'reset'
  block?: boolean
}>()
</script>

<template>
  <button
    :type="type ?? 'button'"
    :disabled="disabled || loading"
    :class="[
      'btn',
      `btn-${variant ?? 'primary'}`,
      `btn-${size ?? 'md'}`,
      block && 'btn-block',
    ]"
  >
    <span v-if="loading" class="btn-spinner" />
    <slot />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  border: none;
  border-radius: var(--radius-md);
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all var(--transition-fast);
  white-space: nowrap;
}

.btn-block {
  width: 100%;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn-sm {
  padding: 0.45rem 0.85rem;
  font-size: var(--text-sm);
}
.btn-md {
  padding: 0.7rem 1.25rem;
  font-size: var(--text-base);
}
.btn-lg {
  padding: 0.9rem 1.5rem;
  font-size: var(--text-md);
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}
.btn-primary:hover:not(:disabled) {
  background: var(--color-primary-hover);
}

.btn-dark {
  background: var(--color-dark);
  color: white;
}
.btn-dark:hover:not(:disabled) {
  background: var(--color-primary);
}

.btn-secondary {
  background: var(--color-surface-muted);
  color: var(--color-text);
}
.btn-secondary:hover:not(:disabled) {
  background: var(--color-border);
}

.btn-danger {
  background: transparent;
  color: var(--color-danger);
  border: 1.5px solid var(--color-danger-soft);
}
.btn-danger:hover:not(:disabled) {
  background: var(--color-danger);
  color: white;
  border-color: var(--color-danger);
}

.btn-ghost {
  background: transparent;
  color: var(--color-text-muted);
}
.btn-ghost:hover:not(:disabled) {
  background: var(--color-surface-muted);
  color: var(--color-text);
}

.btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: currentColor;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
</style>
