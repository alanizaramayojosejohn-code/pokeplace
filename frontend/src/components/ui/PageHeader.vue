<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<template>
  <div class="page-header">
    <div class="header-left">
      <h1>{{ title }}</h1>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="header-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 1rem;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
}

.header-left h1 {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: 900;
  color: var(--color-text);
  margin: 0 0 0.2rem;
  letter-spacing: -0.5px;
}

.header-left p {
  color: var(--color-text-subtle);
  font-size: var(--text-base);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}
</style>
