<script setup lang="ts">
defineProps<{
  modelValue: boolean
  title?: string
  wide?: boolean
}>()
defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()
</script>

<template>
  <Transition name="fade">
    <div v-if="modelValue" class="overlay" @click="$emit('update:modelValue', false)" />
  </Transition>

  <Transition name="slide">
    <aside v-if="modelValue" class="drawer" :class="{ 'drawer-wide': wide }">
      <header class="drawer-header">
        <h2>{{ title }}</h2>
        <button class="btn-close" @click="$emit('update:modelValue', false)">
          <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4l8 8M12 4l-8 8"/></svg>
        </button>
      </header>

      <div class="drawer-body">
        <slot />
      </div>

      <footer v-if="$slots.footer" class="drawer-footer">
        <slot name="footer" />
      </footer>
    </aside>
  </Transition>
</template>

<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  z-index: 100;
}

.drawer {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  width: var(--drawer-width);
  background: var(--color-surface);
  z-index: 101;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-drawer);
}

.drawer-wide {
  width: var(--drawer-width-wide);
  max-width: 95vw;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--color-border);
}

.drawer-header h2 {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  margin: 0;
}

.btn-close {
  background: var(--color-surface-muted);
  border: none;
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  transition: all var(--transition-fast);
}

.btn-close:hover {
  background: var(--color-dark);
  color: white;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.drawer-footer {
  display: flex;
  gap: 0.75rem;
  padding: 1.25rem 2rem;
  border-top: 1px solid var(--color-border);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: transform var(--transition-slow);
}
.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}

@media (max-width: 600px) {
  .drawer {
    width: 100%;
  }

  .drawer-header {
    padding: 1rem 1.25rem;
  }

  .drawer-body {
    padding: 1rem 1.25rem;
    gap: 0.875rem;
  }

  .drawer-footer {
    padding: 1rem 1.25rem;
    gap: 0.5rem;
  }
}
</style>
