<script setup lang="ts">
defineProps<{
  lines?: number
  height?: string
}>()
</script>

<template>
  <div class="skeleton-card" :style="{ height }">
    <div class="skeleton-line skeleton-label"></div>
    <div class="skeleton-line skeleton-value"></div>
    <div v-for="i in (lines ?? 0)" :key="i" class="skeleton-line skeleton-sub"></div>
  </div>
</template>

<style scoped>
.skeleton-card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem 1.5rem;
  border: 1.5px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  animation: pulse 1.5s ease-in-out infinite;
}

.skeleton-line {
  height: 14px;
  border-radius: 4px;
  background: var(--color-border);
  opacity: 0.5;
}

.skeleton-label { width: 40%; }
.skeleton-value {
  width: 70%;
  height: 28px;
}
.skeleton-sub { width: 55%; height: 10px; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
