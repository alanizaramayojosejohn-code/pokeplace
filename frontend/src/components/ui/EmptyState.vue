<script setup lang="ts">
defineProps<{
  icon?: string
  title?: string
  message?: string
}>()
</script>

<template>
  <div class="empty-state">
    <div v-if="icon" class="empty-icon">{{ icon }}</div>
    <h3 v-if="title">{{ title }}</h3>
    <p v-if="message">{{ message }}</p>
    <div v-if="$slots.action" class="empty-action">
      <slot name="action" />
    </div>
  </div>
</template>

<style scoped>
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: var(--color-text-subtle);
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.6;
}

.empty-state h3 {
  font-family: var(--font-display);
  color: var(--color-text);
  margin: 0 0 0.5rem;
  font-size: var(--text-xl);
}

.empty-state p {
  margin: 0 0 1rem;
  font-size: var(--text-base);
}

.empty-action {
  margin-top: 1rem;
  display: inline-flex;
}
</style>
