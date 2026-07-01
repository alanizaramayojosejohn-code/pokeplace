<script setup lang="ts">
defineProps<{
  page: number
  totalPages: number
  totalElements: number
}>()

const emit = defineEmits<{
  (e: 'page-change', page: number): void
}>()
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
      :disabled="page <= 0"
      class="page-btn"
      @click="emit('page-change', page - 1)"
    >
      ← Anterior
    </button>

    <template v-for="p in totalPages" :key="p">
      <button
        v-if="p === 1 || p === totalPages || Math.abs(p - 1 - page) <= 2"
        :class="['page-num', p - 1 === page && 'page-num-active']"
        @click="emit('page-change', p - 1)"
      >
        {{ p }}
      </button>
      <span
        v-else-if="p === 2 && page > 3
          || p === totalPages - 1 && page < totalPages - 4"
        class="page-dots"
      >...</span>
    </template>

    <button
      :disabled="page >= totalPages - 1"
      class="page-btn"
      @click="emit('page-change', page + 1)"
    >
      Siguiente →
    </button>

    <span class="page-info">{{ totalElements }} en total</span>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 1rem 0;
  flex-wrap: wrap;
}

.page-btn,
.page-num {
  padding: 0.4rem 0.75rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-btn:hover:not(:disabled),
.page-num:hover {
  border-color: var(--color-dark);
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-num-active {
  background: var(--color-dark);
  color: white;
  border-color: var(--color-dark);
}

.page-dots {
  padding: 0 0.25rem;
  color: var(--color-text-ghost);
}

.page-info {
  margin-left: auto;
  font-size: var(--text-xs);
  color: var(--color-text-subtle);
}
</style>
