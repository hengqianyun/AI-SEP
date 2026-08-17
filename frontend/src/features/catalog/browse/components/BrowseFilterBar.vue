<script setup lang="ts">
/**
 * 浏览高级筛选条：Cascader l1/l2 + ant-design-vue 控件 + §1.5 Ant→token 映射（REQ-CAT-014 / TASK-WSC-901）。
 */
import { computed } from 'vue'
import { Cascader, ConfigProvider, Input, Select } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'
import type { Category } from '@/api/catalog'
import type { CatalogFilters, LoadState } from '../composables/useCatalogBrowse'
import { categoryPathFromFilters } from '../composables/useCatalogBrowse'
import {
  BROWSE_CATEGORY_CASCADER_LABEL,
  BROWSE_CATEGORY_CASCADER_PLACEHOLDER,
  BROWSE_SUPPLIER_NAME_LABEL,
  BROWSE_SUPPLIER_NAME_PLACEHOLDER,
  DATA_SOURCE_OPTIONS,
  DELIVERY_OPTIONS,
  PRODUCT_TYPE_OPTIONS,
  PUBLIC_DATA_OPTIONS,
} from '../utils/labels'

const filters = defineModel<CatalogFilters>('filters', { required: true })

const props = defineProps<{
  l1Categories: Category[]
  /** 全量 L2（勿传 filters 过滤后的 l2Categories，否则 Cascader 树不完整） */
  allL2Categories: Category[]
  categoriesState: LoadState
  categoriesError?: string | null
}>()

const emit = defineEmits<{
  apply: []
  reset: []
  categoryChange: [path: string[]]
}>()

/**
 * §1.5 冻结 Ant→token 最小映射。
 */
const BROWSE_FILTER_THEME: ThemeConfig = {
  token: {
    colorPrimary: '#3b82f6',
    colorBorder: '#e5e7eb',
    colorBgContainer: '#ffffff',
    borderRadius: 6,
    fontSize: 14,
  },
}

const dataSourceOptions = [...DATA_SOURCE_OPTIONS]
const productTypeOptions = [...PRODUCT_TYPE_OPTIONS]
const publicDataOptions = [...PUBLIC_DATA_OPTIONS]
const deliveryOptions = [...DELIVERY_OPTIONS]

const categoryCascaderOptions = computed(() =>
  props.l1Categories.map((l1) => ({
    value: l1.id,
    label: l1.name,
    children: props.allL2Categories
      .filter((l2) => l2.parentId === l1.id)
      .map((l2) => ({ value: l2.id, label: l2.name })),
  })),
)

const categoryCascaderValue = computed({
  get: () => categoryPathFromFilters(filters.value.l1CategoryId, filters.value.l2CategoryId),
  set: (path: string[]) => {
    filters.value.l1CategoryId = path[0] ?? ''
    filters.value.l2CategoryId = path[1] ?? ''
  },
})

const categoriesLoading = computed(() => props.categoriesState === 'loading')
const categoriesEmpty = computed(
  () => props.categoriesState === 'empty' || props.l1Categories.length === 0,
)

function onCategoryChange(value: (string | number)[] | (string | number)[][] | undefined) {
  const raw = value ?? []
  const path = (Array.isArray(raw[0]) ? raw[0] : raw).map(String)
  categoryCascaderValue.value = path
  emit('categoryChange', path)
}

function onSearchEnter() {
  emit('apply')
}
</script>

<template>
  <ConfigProvider :theme="BROWSE_FILTER_THEME">
    <div
      class="filter-bar wsc-surface"
      data-testid="catalog-filters"
      data-browse-filter-theme="ant-token-mapped"
    >
      <div class="filter-group category-group">
        <label class="filter-label" for="catalog-filter-category">{{
          BROWSE_CATEGORY_CASCADER_LABEL
        }}</label>
        <Cascader
          id="catalog-filter-category"
          v-model:value="categoryCascaderValue"
          class="filter-ant-cascader"
          :options="categoryCascaderOptions"
          :placeholder="BROWSE_CATEGORY_CASCADER_PLACEHOLDER"
          :loading="categoriesLoading"
          :disabled="categoriesState === 'error'"
          change-on-select
          allow-clear
          :aria-label="BROWSE_CATEGORY_CASCADER_LABEL"
          data-testid="catalog-filter-category-cascader"
          @change="onCategoryChange"
        />
        <p
          v-if="categoriesState === 'error'"
          class="filter-hint error"
          data-testid="catalog-filter-category-error"
        >
          {{ categoriesError || '分类加载失败' }}
        </p>
        <p
          v-else-if="categoriesEmpty && categoriesState === 'ready'"
          class="filter-hint"
          data-testid="catalog-filter-category-empty"
        >
          暂无业务视图选项
        </p>
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-source">数据来源</label>
        <Select
          id="catalog-filter-source"
          v-model:value="filters.dataSource"
          class="filter-ant-select"
          :options="dataSourceOptions"
          aria-label="数据来源"
          data-testid="catalog-filter-source"
        />
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-type">产品类型</label>
        <Select
          id="catalog-filter-type"
          v-model:value="filters.productType"
          class="filter-ant-select"
          :options="productTypeOptions"
          aria-label="产品类型"
          data-testid="catalog-filter-type"
        />
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-public">是否涉及公共数据</label>
        <Select
          id="catalog-filter-public"
          v-model:value="filters.involvesPublicData"
          class="filter-ant-select"
          :options="publicDataOptions"
          aria-label="是否涉及公共数据"
          data-testid="catalog-filter-public"
        />
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-delivery">交付方式</label>
        <Select
          id="catalog-filter-delivery"
          v-model:value="filters.deliveryMethod"
          class="filter-ant-select"
          :options="deliveryOptions"
          aria-label="交付方式"
          data-testid="catalog-filter-delivery"
        />
      </div>
      <div class="filter-group search-group">
        <label class="filter-label" for="catalog-search">搜索</label>
        <Input
          id="catalog-search"
          v-model:value="filters.q"
          allow-clear
          class="filter-ant-input"
          placeholder="产品名、产品编码"
          aria-label="搜索产品名或编码"
          data-testid="catalog-search"
          @press-enter="onSearchEnter"
        />
      </div>
      <div class="filter-group supplier-group">
        <label class="filter-label" for="catalog-filter-supplier">{{
          BROWSE_SUPPLIER_NAME_LABEL
        }}</label>
        <Input
          id="catalog-filter-supplier"
          v-model:value="filters.supplierName"
          allow-clear
          class="filter-ant-input"
          :placeholder="BROWSE_SUPPLIER_NAME_PLACEHOLDER"
          :aria-label="BROWSE_SUPPLIER_NAME_LABEL"
          data-testid="catalog-filter-supplier"
          @press-enter="onSearchEnter"
        />
      </div>
      <div class="filter-actions">
        <button type="button" class="btn primary" data-testid="catalog-search-btn" @click="emit('apply')">
          搜索
        </button>
        <button type="button" class="btn" data-testid="catalog-reset-btn" @click="emit('reset')">
          重置
        </button>
      </div>
    </div>
  </ConfigProvider>
</template>

<style scoped>
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
  padding: 14px 20px;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
  font-size: var(--font-size-base);
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 140px;
}

.filter-group.category-group {
  min-width: 220px;
}

.filter-group.search-group,
.filter-group.supplier-group {
  flex: 1;
  min-width: 180px;
}

.filter-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.filter-hint {
  margin: 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.filter-hint.error {
  color: #b42318;
}

.filter-ant-select,
.filter-ant-input,
.filter-ant-cascader {
  width: 100%;
}

.filter-bar :deep(.ant-select-selector),
.filter-bar :deep(.ant-input-affix-wrapper),
.filter-bar :deep(.ant-cascader .ant-select-selector) {
  border-color: var(--border-color) !important;
  background: var(--card-bg) !important;
  border-radius: var(--radius-sm) !important;
  font-size: 14px !important;
  color: var(--text-primary);
  height: 34px;
  line-height: 32px;
  padding: 0 11px;
  box-sizing: border-box;
}

.filter-bar :deep(.ant-input) {
  border: none !important;
  box-shadow: none !important;
  background: transparent !important;
  font-size: 14px !important;
  color: var(--text-primary);
  height: 32px;
  line-height: 32px;
  padding: 0;
}

.filter-bar :deep(.ant-input::placeholder) {
  color: var(--text-secondary);
}

.filter-bar :deep(.ant-select-focused .ant-select-selector),
.filter-bar :deep(.ant-input-affix-wrapper-focused),
.filter-bar :deep(.ant-cascader-focused .ant-select-selector) {
  border-color: var(--blue) !important;
  box-shadow: 0 0 0 2px var(--blue-light) !important;
}

.filter-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  padding-bottom: 1px;
}

.btn {
  height: 34px;
  padding: 0 14px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-primary);
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

.btn:hover {
  background: var(--blue-light);
  color: var(--blue);
  border-color: transparent;
}

.btn.primary {
  border-color: var(--blue);
  background: var(--blue);
  color: #fff;
}

.btn.primary:hover {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}
</style>
