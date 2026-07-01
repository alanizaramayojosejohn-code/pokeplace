<script setup lang="ts">
defineProps<{
  modelValue: string | number | null | undefined
  label?: string
  placeholder?: string
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'date'
  disabled?: boolean
  required?: boolean
  hint?: string
  error?: string
  min?: number
  step?: number | string
}>()
defineEmits<{
  (e: 'update:modelValue', value: string | number): void
  (e: 'blur'): void
}>()
</script>

<template>
  <div class="field">
    <label v-if="label">
      {{ label }}
      <span v-if="required" class="required">*</span>
    </label>
    <input
      :type="type ?? 'text'"
      :value="modelValue ?? ''"
      :placeholder="placeholder"
      :disabled="disabled"
      :min="min"
      :step="step"
      :class="{ 'has-error': !!error }"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      @blur="$emit('blur')"
    />
    <span v-if="hint && !error" class="hint">{{ hint }}</span>
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

.required {
  color: var(--color-primary);
  margin-left: 2px;
}

input {
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  color: var(--color-text);
  outline: none;
  transition: border-color var(--transition-fast);
  background: var(--color-surface);
  width: 100%;
}

input:focus {
  border-color: var(--color-dark);
}

input:disabled {
  background: var(--color-surface-muted);
  color: var(--color-text-ghost);
  cursor: not-allowed;
}

input.has-error {
  border-color: var(--color-danger);
}

.hint {
  font-size: var(--text-xs);
  color: var(--color-text-ghost);
}

.error-msg {
  font-size: var(--text-xs);
  color: var(--color-danger);
}
</style>
