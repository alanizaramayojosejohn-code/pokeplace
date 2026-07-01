<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  icon?: string
  title?: string
  message?: string
}>()

const fallbackIcon = computed(() => {
  if (props.icon) return null
  return '<svg viewBox="0 0 40 40" fill="none" stroke="currentColor" stroke-width="1.5" opacity="0.3"><circle cx="20" cy="16" r="6"/><path d="M8 34c0-6.6 5.4-12 12-12s12 5.4 12 12"/></svg>'
})
</script>

<template>
  <div class="empty-state">
    <div v-if="icon" class="empty-icon">{{ icon }}</div>
    <div v-else class="empty-svg" v-html="fallbackIcon" />
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

.empty-svg {
  margin-bottom: 1rem;
  display: flex;
  justify-content: center;
}

.empty-svg :deep(svg) {
  width: 64px;
  height: 64px;
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
