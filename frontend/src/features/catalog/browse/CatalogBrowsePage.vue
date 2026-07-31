<script setup lang="ts">
import { onMounted, ref } from 'vue'
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
  sections,
  selectedProductId,
  preview,
  listState,
  categoriesState,
  previewState,
  loadingMore,
  hasMore,
  listError,
  categoriesError,
  previewError,
  filters,
  total,
  l1Categories,
  l2Categories,
  industryFilterOptions,
  selectProduct,
  selectL1,
  selectL2,
  applyFilters,
  resetFilters,
  loadProducts,
  loadCategories,
  loadMore,
  init,
} = browse

const listScrollEl = ref<HTMLElement | null>(null)

onMounted(() => {
  void init()
})

function onListScroll(e: Event) {
  const el = e.target as HTMLElement
  if (!el || loadingMore.value || !hasMore.value || listState.value !== 'ready') return
  const remain = el.scrollHeight - el.scrollTop - el.clientHeight
  if (remain < 80) {
    void loadMore()
  }
}

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

function rowIndex(sectionOffset: number, idx: number) {
  return sectionOffset + idx + 1
}
</script>

<template>
  <div class="catalog-browse" data-testid="catalog-browse">
    <header class="page-header">
      <h1>数据目录</h1>
      <p class="subtitle">按空间 / 行业浏览、子类分组预览数据产品</p>
    </header>

    <div class="tag-bar" data-testid="catalog-space-tags">
      <span class="tag-label">空间</span>
      <button
        type="button"
        class="tag"
        :class="{ active: !filters.l1CategoryId }"
        data-testid="catalog-space-all"
        @click="selectL1('')"
      >
        全部空间
      </button>
      <button
        v-for="c in l1Categories"
        :key="c.id"
        type="button"
        class="tag"
        :class="{ active: filters.l1CategoryId === c.id }"
        data-testid="catalog-space-tag"
        @click="selectL1(c.id)"
      >
        {{ c.name }}
      </button>
    </div>

    <div class="tag-bar" data-testid="catalog-industry-tags">
      <span class="tag-label">行业</span>
      <button
        type="button"
        class="tag"
        :class="{ active: !filters.l2CategoryId }"
        data-testid="catalog-industry-all"
        @click="selectL2('')"
      >
        全部行业
      </button>
      <button
        v-for="c in l2Categories"
        :key="c.id"
        type="button"
        class="tag"
        :class="{ active: filters.l2CategoryId === c.id }"
        data-testid="catalog-industry-tag"
        @click="selectL2(c.id)"
      >
        {{ c.name }}
      </button>
    </div>

    <div class="filter-bar" data-testid="catalog-filters">
      <select v-model="filters.industryFilterId" aria-label="行业分类（二级或三级）">
        <option value="">行业分类：全部</option>
        <option v-for="o in industryFilterOptions" :key="o.id" :value="o.id">
          {{ o.level === 'L2' ? o.label : o.label }}
        </option>
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
      {{ categoriesError || '加载分类失败' }}
      <button type="button" class="btn" @click="loadCategories">重试</button>
    </div>

    <div class="bi-pane">
      <section
        ref="listScrollEl"
        class="pane list"
        data-testid="catalog-list"
        @scroll="onListScroll"
      >
        <div class="list-head">
          <h2>产品列表</h2>
          <span class="muted">已加载 {{ products.length }} / 共 {{ total }} 条</span>
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
          <button type="button" class="btn" @click="loadProducts()">重试</button>
        </div>
        <div
          v-else-if="listState === 'empty'"
          class="state empty"
          data-testid="catalog-empty"
        >
          {{ CATALOG_EMPTY_MESSAGE }}
        </div>
        <template v-else-if="listState === 'ready'">
          <div
            v-for="(sec, sIdx) in sections"
            :key="sec.l3CategoryId"
            class="section"
            data-testid="catalog-l3-section"
          >
            <div class="section-head">
              <h3>{{ sec.title }}</h3>
              <span class="muted">{{ sec.count }} 条</span>
            </div>
            <table class="product-table">
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
                  v-for="(p, idx) in sec.products"
                  :key="p.id"
                  :class="{ selected: selectedProductId === p.id }"
                  data-testid="catalog-row"
                  @click="selectProduct(p.id)"
                >
                  <td>
                    {{
                      rowIndex(
                        sections.slice(0, sIdx).reduce((n, s) => n + s.products.length, 0),
                        idx,
                      )
                    }}
                  </td>
                  <td class="mono">{{ p.productCode }}</td>
                  <td>{{ p.productName }}</td>
                  <td>{{ p.chainCount }}</td>
                  <td>
                    <span class="type-tag">{{ productTypeLabel(p.productType) }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div
            v-if="loadingMore"
            class="state load-more"
            data-testid="catalog-load-more"
          >
            正在加载更多…
          </div>
          <div v-else-if="!hasMore" class="state muted end">已加载全部</div>
        </template>
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
              <dt>三级分类路径</dt>
              <dd data-testid="catalog-preview-path">{{ preview.categoryPath || '—' }}</dd>
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
  gap: 10px;
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
.tag-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.tag-label {
  font-size: 12px;
  color: #667085;
  min-width: 2.5em;
}
.tag {
  height: 28px;
  padding: 0 12px;
  border: 1px solid #cfd6df;
  border-radius: 999px;
  background: #fff;
  font: inherit;
  font-size: 13px;
  cursor: pointer;
  color: inherit;
}
.tag:hover {
  background: #f2f4f7;
}
.tag.active {
  border-color: #1f4b7a;
  background: #e8eef5;
  color: #1f4b7a;
  font-weight: 600;
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
.bi-pane {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
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
.list-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 1;
  padding-bottom: 6px;
}
.section {
  margin-bottom: 16px;
}
.section-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin: 8px 0;
  padding-bottom: 4px;
  border-bottom: 1px solid #eef0f3;
}
.section-head h3 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: #344054;
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
.state.load-more {
  padding: 12px;
}
.state.end {
  padding: 8px;
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
.type-tag {
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
@media (max-width: 900px) {
  .bi-pane {
    grid-template-columns: 1fr;
  }
}
</style>
