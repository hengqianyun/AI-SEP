<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import SensitiveId from './components/SensitiveId.vue'
import { useCatalogBrowse } from './composables/useCatalogBrowse'
import {
  CATALOG_EMPTY_MESSAGE,
  DATA_SOURCE_OPTIONS,
  DELIVERY_OPTIONS,
  PRODUCT_TYPE_OPTIONS,
  PUBLIC_DATA_OPTIONS,
  productTypeLabel,
} from './utils/labels'

const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { productWriteVisible } = useCanWrite(role)

const browse = useCatalogBrowse()
const {
  products,
  selectedProductId,
  preview,
  listState,
  categoriesState,
  previewState,
  listError,
  categoriesError,
  previewError,
  filters,
  total,
  l1Categories,
  l2Categories,
  selectProduct,
  selectL1,
  applyFilters,
  resetFilters,
  loadProducts,
  loadCategories,
  init,
} = browse

onMounted(() => {
  void init()
})

function goDetail() {
  if (!selectedProductId.value) return
  void router.push(`/catalog/products/${selectedProductId.value}`)
}

function goChain() {
  if (!selectedProductId.value) return
  void router.push(`/chain/products/${selectedProductId.value}`)
}

function goEdit() {
  if (!selectedProductId.value || !productWriteVisible.value) return
  void router.push(`/catalog/products/${selectedProductId.value}/edit`)
}
</script>

<template>
  <div class="catalog-browse" data-testid="catalog-browse">
    <header class="page-header">
      <h1>数据目录</h1>
      <p class="subtitle">按行业浏览、筛选并预览数据产品</p>
    </header>

    <div class="filter-bar" data-testid="catalog-filters">
      <select v-model="filters.l2CategoryId" aria-label="二级行业">
        <option value="">全部二级分类</option>
        <option v-for="c in l2Categories" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <select v-model="filters.dataSource" aria-label="数据来源">
        <option v-for="o in DATA_SOURCE_OPTIONS" :key="o.value" :value="o.value">
          {{ o.label }}
        </option>
      </select>
      <select v-model="filters.productType" aria-label="产品类型">
        <option v-for="o in PRODUCT_TYPE_OPTIONS" :key="o.value" :value="o.value">
          {{ o.label }}
        </option>
      </select>
      <select v-model="filters.involvesPublicData" aria-label="是否涉及公共数据">
        <option v-for="o in PUBLIC_DATA_OPTIONS" :key="o.value" :value="o.value">
          {{ o.label }}
        </option>
      </select>
      <select v-model="filters.deliveryMethod" aria-label="交付方式">
        <option v-for="o in DELIVERY_OPTIONS" :key="o.value" :value="o.value">
          {{ o.label }}
        </option>
      </select>
      <input
        v-model="filters.q"
        type="search"
        class="search"
        placeholder="搜索产品名 / 编码"
        aria-label="搜索产品名或编码"
        data-testid="catalog-search"
        @keyup.enter="applyFilters"
      />
      <button type="button" class="btn primary" data-testid="catalog-search-btn" @click="applyFilters">
        搜索
      </button>
      <button type="button" class="btn" @click="resetFilters">重置</button>
    </div>

    <div
      v-if="categoriesState === 'error'"
      class="banner error"
      data-testid="catalog-categories-error"
    >
      {{ categoriesError || '加载行业分类失败' }}
      <button type="button" class="btn" @click="loadCategories">重试</button>
    </div>

    <div class="tri-pane">
      <aside class="pane l1" data-testid="catalog-l1">
        <h2>一级行业</h2>
        <button
          type="button"
          class="l1-item"
          :class="{ active: !filters.l1CategoryId }"
          @click="selectL1('')"
        >
          全部行业
        </button>
        <button
          v-for="c in l1Categories"
          :key="c.id"
          type="button"
          class="l1-item"
          :class="{ active: filters.l1CategoryId === c.id }"
          @click="selectL1(c.id)"
        >
          {{ c.name }}
        </button>
      </aside>

      <section class="pane list" data-testid="catalog-list">
        <div class="list-head">
          <h2>产品列表</h2>
          <span class="muted">共 {{ total }} 条</span>
        </div>

        <div v-if="listState === 'loading'" class="state" data-testid="catalog-loading">
          正在加载…
        </div>
        <div
          v-else-if="listState === 'error'"
          class="state error"
          data-testid="catalog-error"
        >
          <p>{{ listError || '筛选失败，请重试' }}</p>
          <button type="button" class="btn" @click="loadProducts">重试</button>
        </div>
        <div
          v-else-if="listState === 'empty'"
          class="state empty"
          data-testid="catalog-empty"
        >
          {{ CATALOG_EMPTY_MESSAGE }}
        </div>
        <table v-else-if="listState === 'ready'" class="product-table">
          <thead>
            <tr>
              <th>序号</th>
              <th>产品编码</th>
              <th>产品名称</th>
              <th>上链次数</th>
              <th>产品类型</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(p, idx) in products"
              :key="p.id"
              :class="{ selected: selectedProductId === p.id }"
              data-testid="catalog-row"
              @click="selectProduct(p.id)"
            >
              <td>{{ idx + 1 }}</td>
              <td class="mono">{{ p.productCode }}</td>
              <td>{{ p.productName }}</td>
              <td>{{ p.chainCount }}</td>
              <td>
                <span class="tag">{{ productTypeLabel(p.productType) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <aside class="pane preview" data-testid="catalog-preview">
        <h2>产品预览</h2>
        <div v-if="previewState === 'loading'" class="state">加载预览…</div>
        <div v-else-if="previewState === 'error'" class="state error">
          {{ previewError || '预览加载失败' }}
        </div>
        <div v-else-if="!preview" class="state muted">请从列表中选择产品</div>
        <template v-else>
          <dl class="preview-fields">
            <div>
              <dt>名称</dt>
              <dd>{{ preview.productName }}</dd>
            </div>
            <div>
              <dt>编码</dt>
              <dd class="mono">{{ preview.productCode }}</dd>
            </div>
            <div>
              <dt>分类路径</dt>
              <dd>{{ preview.categoryPath || '—' }}</dd>
            </div>
            <div>
              <dt>上链次数</dt>
              <dd>{{ preview.chainCount }}</dd>
            </div>
            <div>
              <dt>产品类型</dt>
              <dd>{{ productTypeLabel(preview.productType) }}</dd>
            </div>
            <div>
              <dt>概述</dt>
              <dd>{{ preview.summary || '—' }}</dd>
            </div>
            <div v-if="preview.supplierCreditCode">
              <dt>供应商信用代码</dt>
              <dd>
                <SensitiveId
                  :value="String(preview.supplierCreditCode)"
                  label=""
                />
              </dd>
            </div>
          </dl>
          <div class="preview-actions">
            <button
              type="button"
              class="btn primary"
              data-testid="catalog-go-detail"
              @click="goDetail"
            >
              详情
            </button>
            <button
              type="button"
              class="btn"
              data-testid="catalog-go-chain"
              @click="goChain"
            >
              上链
            </button>
            <button
              v-if="productWriteVisible"
              type="button"
              class="btn"
              data-testid="catalog-go-edit"
              @click="goEdit"
            >
              编辑
            </button>
          </div>
        </template>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.catalog-browse {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: calc(100vh - 48px);
  padding: 4px 4px 16px;
  color: #1b2430;
}
.page-header h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 650;
}
.subtitle {
  margin: 4px 0 0;
  color: #667085;
  font-size: 13px;
}
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e4e7ec;
  border-radius: 6px;
}
.filter-bar select,
.filter-bar .search {
  height: 32px;
  padding: 0 8px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  background: #fff;
  font: inherit;
}
.filter-bar .search {
  min-width: 180px;
  flex: 1;
}
.btn {
  height: 32px;
  padding: 0 12px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  background: #fff;
  font: inherit;
  cursor: pointer;
}
.btn.primary {
  border-color: #1f4b7a;
  background: #1f4b7a;
  color: #fff;
}
.btn:hover {
  filter: brightness(0.98);
}
.banner {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
}
.banner.error,
.state.error {
  color: #b42318;
  background: #fef3f2;
}
.tri-pane {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr) 300px;
  gap: 10px;
  flex: 1;
  min-height: 420px;
}
.pane {
  background: #fff;
  border: 1px solid #e4e7ec;
  border-radius: 6px;
  padding: 12px;
  overflow: auto;
}
.pane h2 {
  margin: 0 0 10px;
  font-size: 14px;
}
.l1-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 8px 10px;
  margin-bottom: 4px;
  border: none;
  border-radius: 4px;
  background: transparent;
  font: inherit;
  cursor: pointer;
  color: inherit;
}
.l1-item:hover {
  background: #f2f4f7;
}
.l1-item.active {
  background: #e8eef5;
  color: #1f4b7a;
  font-weight: 600;
}
.list-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.muted {
  color: #667085;
  font-size: 12px;
}
.state {
  padding: 24px 8px;
  text-align: center;
  color: #667085;
  font-size: 13px;
}
.state.empty {
  color: #475467;
}
.product-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.product-table th,
.product-table td {
  padding: 8px 10px;
  border-bottom: 1px solid #eef0f3;
  text-align: left;
}
.product-table th {
  color: #667085;
  font-weight: 500;
  background: #fafbfc;
}
.product-table tbody tr {
  cursor: pointer;
}
.product-table tbody tr:hover {
  background: #f7f9fc;
}
.product-table tbody tr.selected {
  background: #eef4fb;
}
.mono {
  font-family: ui-monospace, 'Cascadia Code', Consolas, monospace;
  font-size: 12px;
}
.tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 4px;
  background: #eef2f6;
  font-size: 12px;
}
.preview-fields {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.preview-fields dt {
  font-size: 12px;
  color: #667085;
}
.preview-fields dd {
  margin: 2px 0 0;
  font-size: 14px;
}
.preview-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}
@media (max-width: 1100px) {
  .tri-pane {
    grid-template-columns: 1fr;
  }
}
</style>
