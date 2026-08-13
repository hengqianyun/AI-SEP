<script setup lang="ts">
/**
 * 浏览高级筛选条：ant-design-vue Input/Select + §1.5 Ant→token 映射（TASK-WSC-705）。
 */
import { computed } from 'vue'
import { ConfigProvider, Input, Select } from 'ant-design-vue'
import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'
import type { CatalogFilters } from '../composables/useCatalogBrowse'
import {
  BROWSE_INDUSTRY_CATEGORY_LABEL,
  DATA_SOURCE_OPTIONS,
  DELIVERY_OPTIONS,
  PRODUCT_TYPE_OPTIONS,
  PUBLIC_DATA_OPTIONS,
} from '../utils/labels'
import { INDUSTRY_CATEGORY_OPTIONS } from '@/api/catalog'

const filters = defineModel<CatalogFilters>('filters', { required: true })

const emit = defineEmits<{
  apply: []
  reset: []
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

const industrySelectOptions = computed(() => [
  { value: '', label: '全部' },
  ...INDUSTRY_CATEGORY_OPTIONS.map((c) => ({ value: c, label: c })),
])

const dataSourceOptions = [...DATA_SOURCE_OPTIONS]
const productTypeOptions = [...PRODUCT_TYPE_OPTIONS]
const publicDataOptions = [...PUBLIC_DATA_OPTIONS]
const deliveryOptions = [...DELIVERY_OPTIONS]

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
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-industry">{{
          BROWSE_INDUSTRY_CATEGORY_LABEL
        }}</label>
        <Select
          id="catalog-filter-industry"
          v-model:value="filters.industryCategory"
          class="filter-ant-select"
          :options="industrySelectOptions"
          :aria-label="`${BROWSE_INDUSTRY_CATEGORY_LABEL}（GB/T 门类）`"
          data-testid="catalog-filter-industry"
        />
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
          placeholder="产品名、产品编码、企业名称"
          aria-label="搜索产品名、产品编码或企业名称"
          data-testid="catalog-search"
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

.filter-group.search-group {
  flex: 1;
  min-width: 180px;
}

.filter-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.filter-ant-select,
.filter-ant-input {
  width: 100%;
}

/* Select 和 Input 统一样式：只在外层设置 border，避免双重边框 */
.filter-bar :deep(.ant-select-selector),
.filter-bar :deep(.ant-input-affix-wrapper) {
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

/* Input 内部元素不设置 border，避免重叠 */
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

/* 聚焦状态：只在外层容器显示蓝色边框 */
.filter-bar :deep(.ant-select-focused .ant-select-selector),
.filter-bar :deep(.ant-input-affix-wrapper-focused) {
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
