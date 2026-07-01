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
    <span class="search-icon">
      <svg viewBox="0 0 18 18" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="8" cy="8" r="5.5" />
        <path d="M12 12l3.5 3.5" />
      </svg>
    </span>
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
  display: flex;
  align-items: center;
  color: var(--color-text-ghost);
}

.search-icon svg {
  width: 18px;
  height: 18px;
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
