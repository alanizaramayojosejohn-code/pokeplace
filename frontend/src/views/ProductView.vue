<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useProductsStore, type EdiblePayload, type InediblePayload } from '../stores/products'
import { useCategoriesStore } from '../stores/categories'
import type { Product, ProductType } from '../types/api'
import PageHeader from '../components/ui/PageHeader.vue'
import SearchBar from '../components/ui/SearchBar.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseSelect from '../components/ui/BaseSelect.vue'
import BaseDrawer from '../components/ui/BaseDrawer.vue'
import BaseBadge from '../components/ui/BaseBadge.vue'
import EmptyState from '../components/ui/EmptyState.vue'
import LoadingState from '../components/ui/LoadingState.vue'
import AlertBanner from '../components/ui/AlertBanner.vue'

const products = useProductsStore()
const categories = useCategoriesStore()

type ProductFilter = 'ALL' | ProductType
const showDrawer = ref(false)
const editingId = ref<number | null>(null)
const searchQuery = ref('')
const activeTab = ref<ProductFilter>('ALL')

const form = ref<{
  type: ProductType
  name: string
  price: number
  categoryId: number | null
  pokeName: string
  stock: number
  minStock: number
  description: string
  cost: number | null
}>({
  type: 'EDIBLE',
  name: '',
  price: 0,
  categoryId: null,
  pokeName: '',
  stock: 0,
  minStock: 0,
  description: '',
  cost: null,
})

const namePattern = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*$/
const pokeNamePattern = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*$/
const touched = ref<Set<string>>(new Set())

function markTouched(field: string) {
  touched.value.add(field)
}

const fieldErrors = computed(() => ({
  name:
    !form.value.name
      ? 'El nombre es obligatorio'
      : !namePattern.test(form.value.name)
        ? 'Solo letras y espacios'
        : form.value.name.length > 25
          ? 'Máximo 25 caracteres'
          : '',
  price:
    form.value.price <= 0
      ? 'Debe ser un valor positivo'
      : '',
  pokeName:
    form.value.type === 'EDIBLE' && !form.value.pokeName
      ? 'El nombre del Poke es obligatorio'
      : form.value.type === 'EDIBLE' && !pokeNamePattern.test(form.value.pokeName)
        ? 'Solo letras y espacios'
        : form.value.type === 'EDIBLE' && form.value.pokeName.length > 15
          ? 'Máximo 15 caracteres'
          : '',
  stock:
    form.value.type === 'INEDIBLE' && form.value.stock < 1
      ? 'Debe ser un valor positivo'
      : '',
  minStock:
    form.value.type === 'INEDIBLE' && form.value.minStock < 0
      ? 'No puede ser negativo'
      : '',
  description:
    form.value.description.length > 50
      ? 'Máximo 50 caracteres'
      : '',
  cost:
    form.value.cost !== null && form.value.cost !== 0 && form.value.cost < 0.01
      ? 'Debe ser un valor positivo'
      : '',
}))

onMounted(async () => {
  await Promise.all([products.fetchAll(), categories.fetchAll()])
})

const categoryOptions = computed(() =>
  categories.items.map((c) => ({ value: c.id, label: c.name })),
)

const showQuickCategory = ref(false)
const quickCategoryForm = ref({ name: '' })
const quickCategoryLoading = ref(false)

function toggleQuickCategory() {
  showQuickCategory.value = !showQuickCategory.value
  if (showQuickCategory.value) quickCategoryForm.value = { name: '' }
}

async function submitQuickCategory() {
  const name = quickCategoryForm.value.name.trim()
  if (!name) return
  quickCategoryLoading.value = true
  try {
    const ok = await categories.create({ name })
    if (ok) {
      await categories.fetchAll()
      const created = categories.items.find((c) => c.name === name)
      if (created) form.value.categoryId = created.id
      showQuickCategory.value = false
    }
  } finally {
    quickCategoryLoading.value = false
  }
}

const typeOptions = [
  { value: 'EDIBLE' as ProductType, label: 'Comestible' },
  { value: 'INEDIBLE' as ProductType, label: 'No comestible' },
]

const filtered = computed(() =>
  products.items.filter((p) => {
    if (activeTab.value !== 'ALL' && p.type !== activeTab.value) return false
    const q = searchQuery.value.toLowerCase()
    return (
      p.name.toLowerCase().includes(q) ||
      p.pokeName?.toLowerCase().includes(q) ||
      p.categoryName?.toLowerCase().includes(q)
    )
  }),
)

function resetForm() {
  form.value = {
    type: 'EDIBLE',
    name: '',
    price: 0,
    categoryId: categories.items[0]?.id ?? null,
    pokeName: '',
    stock: 0,
    minStock: 0,
    description: '',
    cost: null,
  }
  touched.value = new Set()
}

function openCreate() {
  editingId.value = null
  resetForm()
  products.error = null
  showDrawer.value = true
}

function openEdit(p: Product) {
  editingId.value = p.id
  form.value = {
    type: p.type,
    name: p.name,
    price: p.price,
    categoryId: p.categoryId,
    pokeName: p.pokeName ?? '',
    stock: p.stock ?? 0,
    minStock: p.minStock ?? 0,
    description: p.description ?? '',
    cost: p.cost ?? null,
  }
  touched.value = new Set()
  products.error = null
  showDrawer.value = true
}

const isFormValid = computed(() => {
  return Object.values(fieldErrors.value).every((e) => !e)
})

async function handleSubmit() {
  if (!form.value.categoryId) {
    products.error = 'Selecciona una categoría'
    return
  }
  const base = {
    name: form.value.name,
    price: Number(form.value.price),
    categoryId: Number(form.value.categoryId),
    description: form.value.description || undefined,
    cost: form.value.cost && form.value.cost > 0 ? Number(form.value.cost) : undefined,
  }

  let ok = false
  if (form.value.type === 'EDIBLE') {
    const payload: EdiblePayload = {
      name: base.name,
      price: base.price,
      categoryId: base.categoryId,
      pokeName: form.value.pokeName,
      ...(base.description ? { description: base.description } : {}),
      ...(base.cost !== undefined ? { cost: base.cost } : {}),
    }
    ok = editingId.value
      ? await products.updateEdible(editingId.value, payload)
      : await products.createEdible(payload)
  } else {
    const payload: InediblePayload = {
      name: base.name,
      price: base.price,
      categoryId: base.categoryId,
      stock: Number(form.value.stock),
      minStock: Number(form.value.minStock),
      ...(base.description ? { description: base.description } : {}),
      ...(base.cost !== undefined ? { cost: base.cost } : {}),
    }
    ok = editingId.value
      ? await products.updateInedible(editingId.value, payload)
      : await products.createInedible(payload)
  }
  if (ok) showDrawer.value = false
}

async function handleDelete(id: number) {
  if (confirm('¿Eliminar este producto?')) {
    await products.remove(id)
  }
}

function formatPrice(n: number) {
  return `Bs ${n.toFixed(2)}`
}

function stockStatus(p: Product) {
  if (p.type !== 'INEDIBLE' || p.stock == null || p.minStock == null) return null
  if (p.stock === 0) return { label: 'Sin stock', variant: 'danger' as const }
  if (p.stock <= p.minStock) return { label: `Stock bajo (${p.stock})`, variant: 'warning' as const }
  return { label: `${p.stock} en stock`, variant: 'success' as const }
}
</script>

<template>
  <div class="page">
    <PageHeader
      title="Productos"
      :subtitle="`${products.items.length} producto${products.items.length !== 1 ? 's' : ''} registrado${products.items.length !== 1 ? 's' : ''}`"
    >
      <template #actions>
        <BaseButton variant="dark" @click="openCreate">+ Nuevo Producto</BaseButton>
      </template>
    </PageHeader>

    <div class="filters">
      <div class="tabs">
        <button
          :class="['tab', activeTab === 'ALL' && 'tab-active']"
          @click="activeTab = 'ALL'"
        >
          Todos
        </button>
        <button
          :class="['tab', activeTab === 'EDIBLE' && 'tab-active']"
          @click="activeTab = 'EDIBLE'"
        >
          Poke Bowls
        </button>
        <button
          :class="['tab', activeTab === 'INEDIBLE' && 'tab-active']"
          @click="activeTab = 'INEDIBLE'"
        >
          Con stock
        </button>
      </div>
      <SearchBar v-model="searchQuery" placeholder="Buscar producto..." />
    </div>

    <LoadingState v-if="products.loading && products.items.length === 0" />

    <EmptyState
      v-else-if="filtered.length === 0"
      icon="📦"
      title="Sin productos"
      message="Crea tu primer producto."
    />

    <div v-else class="grid">
      <article v-for="p in filtered" :key="p.id" class="card">
        <div class="card-top">
          <BaseBadge :variant="p.type === 'EDIBLE' ? 'primary' : 'info'">
            {{ p.type === 'EDIBLE' ? 'Poke Bowl' : 'Con stock' }}
          </BaseBadge>
          <span class="price">{{ formatPrice(p.price) }}</span>
        </div>
        <div class="card-body">
          <h3>{{ p.name }}</h3>
          <p v-if="p.pokeName" class="subtitle">✦ {{ p.pokeName }}</p>
          <p class="category">{{ p.categoryName }}</p>
          <BaseBadge v-if="stockStatus(p)" :variant="stockStatus(p)!.variant">
            {{ stockStatus(p)!.label }}
          </BaseBadge>
        </div>
        <div class="card-actions">
          <BaseButton size="sm" variant="secondary" @click="openEdit(p)">Editar</BaseButton>
          <BaseButton size="sm" variant="danger" @click="handleDelete(p.id)">Eliminar</BaseButton>
        </div>
      </article>
    </div>

    <BaseDrawer v-model="showDrawer" :title="editingId ? 'Editar Producto' : 'Nuevo Producto'">
      <BaseSelect
        v-model="form.type"
        label="Tipo"
        :options="typeOptions"
        :disabled="!!editingId"
        required
      />
      <BaseInput
        v-model="form.name"
        label="Nombre"
        placeholder="Ej: Poke Salmón"
        maxlength="25"
        :error="touched.has('name') ? fieldErrors.name : ''"
        required
        @input="markTouched('name')"
      />
      <div class="form-row">
        <BaseInput
          v-model="form.price"
          label="Precio (Bs)"
          type="number"
          step="0.5"
          :min="0.01"
          placeholder="45.00"
          :error="touched.has('price') ? fieldErrors.price : ''"
          required
          @input="markTouched('price')"
        />
        <BaseInput
          v-model="form.cost"
          label="Costo (Bs)"
          type="number"
          step="0.5"
          :min="0.01"
          placeholder="20.00"
          :error="touched.has('cost') ? fieldErrors.cost : ''"
          hint="Costo de producci&oacute;n o adquisici&oacute;n"
          @input="markTouched('cost')"
        />
      </div>
      <BaseInput
        v-model="form.description"
        label="Descripci&oacute;n"
        placeholder="Breve descripci&oacute;n del producto"
        maxlength="50"
        :error="touched.has('description') ? fieldErrors.description : ''"
        @input="markTouched('description')"
      />
        <div class="category-field-row">
          <BaseSelect
            v-model="form.categoryId"
            label="Categor&iacute;a"
            :options="categoryOptions"
            placeholder="Selecciona una categor&iacute;a"
            required
            class="category-select"
          />
          <button class="btn-add-category" type="button" title="Nueva categor&iacute;a" @click="toggleQuickCategory">
            <svg viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M8 3v10M3 8h10"/></svg>
          </button>
        </div>
        <div v-if="showQuickCategory" class="quick-category-form">
          <BaseInput v-model="quickCategoryForm.name" placeholder="Nombre de la categor&iacute;a" required />
          <BaseButton size="sm" variant="primary" block :loading="quickCategoryLoading" @click="submitQuickCategory">
            {{ quickCategoryLoading ? 'Guardando...' : 'Guardar Categor&iacute;a' }}
          </BaseButton>
        </div>

      <template v-if="form.type === 'EDIBLE'">
        <BaseInput
          v-model="form.pokeName"
          label="Nombre del Poke"
          placeholder="Ej: Tsunami Roll"
          maxlength="15"
          :error="touched.has('pokeName') ? fieldErrors.pokeName : ''"
          hint="Nombre creativo del poke bowl"
          required
          @input="markTouched('pokeName')"
        />
      </template>

      <template v-else>
        <div class="form-row">
          <BaseInput
            v-model="form.stock"
            label="Stock inicial"
            type="number"
            :min="1"
            placeholder="10"
            :error="touched.has('stock') ? fieldErrors.stock : ''"
            required
            @input="markTouched('stock')"
          />
          <BaseInput
            v-model="form.minStock"
            label="Stock m&iacute;nimo"
            type="number"
            :min="0"
            placeholder="2"
            :error="touched.has('minStock') ? fieldErrors.minStock : ''"
            hint="Alerta bajo este nivel"
            required
            @input="markTouched('minStock')"
          />
        </div>
      </template>

      <AlertBanner v-if="products.error" :message="products.error" />

      <template #footer>
        <BaseButton variant="secondary" block @click="showDrawer = false">Cancelar</BaseButton>
        <BaseButton variant="primary" block :loading="products.loading" @click="handleSubmit">
          {{ editingId ? 'Guardar Cambios' : 'Crear Producto' }}
        </BaseButton>
      </template>
    </BaseDrawer>
  </div>
</template>

<style scoped>
.page {
  padding: 2rem;
  min-height: 100vh;
  background: var(--color-bg);
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.filters {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.tabs {
  display: flex;
  gap: 0.25rem;
  background: var(--color-surface);
  padding: 0.3rem;
  border-radius: var(--radius-lg);
  border: 1.5px solid var(--color-border);
}

.tab {
  padding: 0.5rem 0.9rem;
  border: none;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: var(--text-sm);
  font-weight: 500;
  transition: all var(--transition-fast);
}

.tab:hover {
  color: var(--color-text);
}

.tab-active {
  background: var(--color-dark);
  color: white;
}

.filters > .search-bar,
.filters > :last-child {
  flex: 1;
  min-width: 240px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1rem;
}

.card {
  background: var(--color-surface);
  border-radius: var(--radius-lg);
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border: 1.5px solid transparent;
  transition: all var(--transition-fast);
}

.card:hover {
  border-color: var(--color-border);
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: var(--text-md);
  color: var(--color-primary);
}

.card-body h3 {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 700;
  margin: 0 0 0.25rem;
}

.subtitle {
  color: var(--color-primary);
  font-size: var(--text-sm);
  margin: 0 0 0.3rem;
  font-style: italic;
}

.category {
  color: var(--color-text-subtle);
  font-size: var(--text-sm);
  margin: 0 0 0.5rem;
}

.card-actions {
  display: flex;
  gap: 0.5rem;
  padding-top: 0.75rem;
  border-top: 1px solid var(--color-border);
}

.card-actions > * {
  flex: 1;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.category-field-row {
  display: flex;
  gap: 0.5rem;
  align-items: flex-end;
}
.category-field-row .category-select {
  flex: 1;
}

.btn-add-category {
  width: 42px;
  height: 42px;
  border: 1.5px solid var(--color-border);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  color: var(--color-text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  flex-shrink: 0;
}
.btn-add-category:hover {
  border-color: var(--color-dark);
  color: var(--color-dark);
}
.btn-add-category svg {
  width: 18px;
  height: 18px;
}

.quick-category-form {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem;
  background: var(--color-bg);
  border-radius: var(--radius-md);
  border: 1.5px solid var(--color-border);
  animation: fadeInUp 0.2s ease-out;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
