<script setup lang="ts">
defineProps<{
  message?: string
}>()
</script>

<template>
  <div class="loading-state">
    <div class="spinner" />
    <span>{{ message ?? 'Cargando...' }}</span>
  </div>
</template>

<style scoped>
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 4rem;
  color: var(--color-text-subtle);
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-dark);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
</style>
