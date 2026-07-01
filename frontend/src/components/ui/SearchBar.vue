<script setup lang="ts">
defineProps<{
  modelValue: string
  placeholder?: string
}>()
defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="search-bar">
    <span class="search-icon">⌕</span>
    <input
      :value="modelValue"
      type="text"
      :placeholder="placeholder ?? 'Buscar...'"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
  </div>
</template>

<style scoped>
.search-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  background: var(--color-surface);
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-lg);
  padding: 0.7rem 1rem;
  transition: border-color var(--transition-fast);
}

.search-bar:focus-within {
  border-color: var(--color-dark);
}

.search-icon {
  font-size: var(--text-lg);
  color: var(--color-text-ghost);
}

.search-bar input {
  border: none;
  outline: none;
  font-size: var(--text-base);
  color: var(--color-text);
  width: 100%;
  background: transparent;
}

.search-bar input::placeholder {
  color: var(--color-text-ghost);
}
</style>
