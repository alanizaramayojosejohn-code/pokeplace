<script setup lang="ts" generic="T extends string | number">
defineProps<{
  modelValue: T | null | undefined
  label?: string
  options: { value: T; label: string }[]
  placeholder?: string
  disabled?: boolean
  required?: boolean
  error?: string
}>()
defineEmits<{
  (e: 'update:modelValue', value: T): void
}>()
</script>

<template>
  <div class="field">
    <label v-if="label">
      {{ label }}
      <span v-if="required" class="required">*</span>
    </label>
    <select
      :value="modelValue ?? ''"
      :disabled="disabled"
      :class="{ 'has-error': !!error }"
      @change="$emit('update:modelValue', ($event.target as HTMLSelectElement).value as T)"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option v-for="opt in options" :key="opt.value" :value="opt.value">
        {{ opt.label }}
      </option>
    </select>
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

select {
  padding: 0.75rem 1rem;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  color: var(--color-text);
  outline: none;
  background: var(--color-surface);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

select:focus {
  border-color: var(--color-dark);
}

select:disabled {
  background: var(--color-surface-muted);
  color: var(--color-text-ghost);
  cursor: not-allowed;
}

select.has-error {
  border-color: var(--color-danger);
}

.error-msg {
  font-size: var(--text-xs);
  color: var(--color-danger);
}
</style>
