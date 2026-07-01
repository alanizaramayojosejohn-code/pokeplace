<script setup lang="ts">
defineProps<{
  modelValue: string | null | undefined
  label?: string
  placeholder?: string
  rows?: number
  disabled?: boolean
  error?: string
}>()
defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="field">
    <label v-if="label">{{ label }}</label>
    <textarea
      :value="modelValue ?? ''"
      :placeholder="placeholder"
      :rows="rows ?? 3"
      :disabled="disabled"
      :class="{ 'has-error': !!error }"
      @input="$emit('update:modelValue', ($event.target as HTMLTextAreaElement).value)"
    />
    <span v-if="error" class="error-msg">{{ error }}</span>
  </div>
</template>

<style scoped>
.field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

label {
  font-size: var(--text-xs);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.8px;
  color: var(--color-text-subtle);
}

textarea {
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  color: var(--color-text);
  outline: none;
  background: var(--color-surface);
  font-family: inherit;
  resize: vertical;
  min-height: 80px;
  transition: border-color var(--transition-fast);
}

textarea:focus {
  border-color: var(--color-dark);
}

textarea.has-error {
  border-color: var(--color-danger);
}

.error-msg {
  font-size: var(--text-xs);
  color: var(--color-danger);
}
</style>
