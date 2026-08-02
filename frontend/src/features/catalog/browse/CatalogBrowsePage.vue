<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import ImportDialog from '@/features/catalog/import/ImportDialog.vue'
import SensitiveId from './components/SensitiveId.vue'
import {
  fillUntilScrollable,
  isNearScrollBottom,
  LIST_SCROLL_THRESHOLD,
  useCatalogBrowse,
} from './composables/useCatalogBrowse'
import { useCatalogImportEntry } from './composables/useCatalogImportEntry'
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
const { importOpen, productImportVisible, openImport, onImportClosed } =
  useCatalogImportEntry(role)

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

/** 列表 pane：滚动触底 + 未溢出自动续载度量（FIND-WSC-305-R1-001） */
const listScrollEl = ref<HTMLElement | null>(null)
let fillInFlight = false
let listResizeObserver: ResizeObserver | null = null

async function ensureListFilled() {
  if (fillInFlight) return
  const el = listScrollEl.value
  if (!el) return
  fillInFlight = true
  try {
    await fillUntilScrollable({
      getMetrics: () => ({
        scrollHeight: el.scrollHeight,
        clientHeight: el.clientHeight,
      }),
      hasMore: () => hasMore.value,
      loadingMore: () => loadingMore.value,
      listReady: () => listState.value === 'ready',
      loadMore: async () => {
        await loadMore()
        await nextTick()
      },
      getLoadedCount: () => products.value.length,
      threshold: LIST_SCROLL_THRESHOLD,
    })
  } finally {
    fillInFlight = false
  }
}

onMounted(() => {
  void init().then(() => ensureListFilled())
  void nextTick(() => {
    const el = listScrollEl.value
    if (!el || typeof ResizeObserver === 'undefined') return
    listResizeObserver = new ResizeObserver(() => {
      void ensureListFilled()
    })
    listResizeObserver.observe(el)
  })
})

onBeforeUnmount(() => {
  listResizeObserver?.disconnect()
  listResizeObserver = null
})

watch(
  () => [listState.value, products.value.length, hasMore.value] as const,
  () => {
    void ensureListFilled()
  },
)

function onListScroll(e: Event) {
  const el = e.target as HTMLElement
  if (!el || loadingMore.value || !hasMore.value || listState.value !== 'ready') return
  if (isNearScrollBottom(el)) {
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

function padNo(n: number) {
  return String(n).padStart(2, '0')
}
</script>

<template>
  <div class="catalog-browse" data-testid="catalog-browse">
    <header class="page-header wsc-surface">
      <div class="header-main">
        <h1>数据目录</h1>
        <p class="subtitle">按空间 / 行业浏览、子类分组预览数据产品</p>
      </div>
      <button
        v-if="productImportVisible"
        type="button"
        class="btn primary import-btn"
        data-testid="catalog-open-import"
        @click="openImport"
      >
        批量导入产品
      </button>
    </header>

    <ImportDialog v-model="importOpen" @closed="onImportClosed" />

    <div class="nav-card wsc-surface">
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

      <div class="tag-bar industry" data-testid="catalog-industry-tags">
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
    </div>

    <div class="filter-bar wsc-surface" data-testid="catalog-filters">
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-industry">行业分类</label>
        <select id="catalog-filter-industry" v-model="filters.industryFilterId" aria-label="行业分类（二级或三级）">
          <option value="">全部</option>
          <option v-for="o in industryFilterOptions" :key="o.id" :value="o.id">
            {{ o.label }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-source">数据来源</label>
        <select id="catalog-filter-source" v-model="filters.dataSource" aria-label="数据来源">
          <option v-for="o in DATA_SOURCE_OPTIONS" :key="o.value" :value="o.value">
            {{ o.label }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-type">产品类型</label>
        <select id="catalog-filter-type" v-model="filters.productType" aria-label="产品类型">
          <option v-for="o in PRODUCT_TYPE_OPTIONS" :key="o.value" :value="o.value">
            {{ o.label }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-public">是否涉及公共数据</label>
        <select id="catalog-filter-public" v-model="filters.involvesPublicData" aria-label="是否涉及公共数据">
          <option v-for="o in PUBLIC_DATA_OPTIONS" :key="o.value" :value="o.value">
            {{ o.label }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label class="filter-label" for="catalog-filter-delivery">交付方式</label>
        <select id="catalog-filter-delivery" v-model="filters.deliveryMethod" aria-label="交付方式">
          <option v-for="o in DELIVERY_OPTIONS" :key="o.value" :value="o.value">
            {{ o.label }}
          </option>
        </select>
      </div>
      <div class="filter-group search-group">
        <label class="filter-label" for="catalog-search">搜索</label>
        <input
          id="catalog-search"
          v-model="filters.q"
          type="search"
          class="search"
          placeholder="产品名、产品编码"
          aria-label="搜索产品名或编码"
          data-testid="catalog-search"
          @keyup.enter="applyFilters"
        />
      </div>
      <div class="filter-actions">
        <button type="button" class="btn primary" data-testid="catalog-search-btn" @click="applyFilters">
          搜索
        </button>
        <button type="button" class="btn" @click="resetFilters">重置</button>
      </div>
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
        class="pane list wsc-surface"
        data-testid="catalog-list"
        @scroll="onListScroll"
      >
        <div class="list-head">
          <div>
            <h2>产品列表</h2>
            <p class="list-subtitle">已加载 {{ products.length }} / 共 {{ total }} 条</p>
          </div>
        </div>

        <div v-if="listState === 'loading'" class="state loading" data-testid="catalog-loading">
          <span class="spinner" aria-hidden="true" />
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
              <h3 class="section-title">{{ sec.title }}</h3>
              <span class="section-count">{{ sec.count }} 项</span>
            </div>
            <div class="product-list">
              <div
                v-for="(p, idx) in sec.products"
                :key="p.id"
                class="product-item"
                :class="{ selected: selectedProductId === p.id }"
                data-testid="catalog-row"
                role="button"
                tabindex="0"
                @click="selectProduct(p.id)"
                @keyup.enter="selectProduct(p.id)"
              >
                <span class="product-no">{{
                  padNo(
                    rowIndex(
                      sections.slice(0, sIdx).reduce((n, s) => n + s.products.length, 0),
                      idx,
                    ),
                  )
                }}</span>
                <span class="product-code mono">{{ p.productCode }}</span>
                <span class="product-name">{{ p.productName }}</span>
                <span class="product-chain" title="上链次数">{{ p.chainCount }} 次</span>
                <span class="type-tag">{{ productTypeLabel(p.productType) }}</span>
              </div>
            </div>
          </div>
          <div
            v-if="loadingMore"
            class="state load-more"
            data-testid="catalog-load-more"
          >
            <span class="spinner" aria-hidden="true" />
            加载中…
          </div>
          <div v-else-if="!hasMore" class="state end">已加载全部</div>
        </template>
      </section>

      <aside class="pane preview wsc-surface" data-testid="catalog-preview">
        <h2 class="preview-heading">产品预览</h2>
        <div v-if="previewState === 'loading'" class="state loading">
          <span class="spinner" aria-hidden="true" />
          加载预览…
        </div>
        <div v-else-if="previewState === 'error'" class="state error">
          {{ previewError || '预览加载失败' }}
        </div>
        <div v-else-if="!preview" class="state muted preview-empty">请从列表中选择产品</div>
        <template v-else>
          <h3 class="preview-title">{{ preview.productName }}</h3>
          <dl class="preview-fields">
            <div class="preview-meta-item">
              <dt>编码</dt>
              <dd class="mono">{{ preview.productCode }}</dd>
            </div>
            <div class="preview-meta-item">
              <dt>三级分类路径</dt>
              <dd data-testid="catalog-preview-path">{{ preview.categoryPath || '—' }}</dd>
            </div>
            <div class="preview-meta-item">
              <dt>上链次数</dt>
              <dd>{{ preview.chainCount }}</dd>
            </div>
            <div class="preview-meta-item">
              <dt>产品类型</dt>
              <dd>{{ productTypeLabel(preview.productType) }}</dd>
            </div>
          </dl>
          <div class="preview-description">
            <div class="preview-description-label">概述</div>
            <p>{{ preview.summary || '—' }}</p>
          </div>
          <div v-if="preview.supplierCreditCode" class="preview-meta-item sensitive">
            <dt>供应商信用代码</dt>
            <dd>
              <SensitiveId
                :value="String(preview.supplierCreditCode)"
                label=""
              />
            </dd>
          </div>
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
  /* 约束页面高度，使 .pane.list 自身滚动（REQ-UX-004 触底分页） */
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: calc(100vh - 2 * var(--main-padding, 24px));
  max-height: calc(100vh - 2 * var(--main-padding, 24px));
  min-height: 0;
  overflow: hidden;
  padding: 0 0 16px;
  box-sizing: border-box;
  color: var(--text-primary);
  font-family: var(--font-family-sans);
  font-size: var(--font-size-base);
}

.page-header,
.nav-card,
.filter-bar,
.banner {
  flex-shrink: 0;
}

.wsc-surface {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
}

.page-header {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  padding: 16px 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.subtitle {
  margin: 4px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.nav-card {
  padding: 12px 20px;
}

.tag-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tag-bar.industry {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid var(--border-color);
}

.tag-label {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-right: 4px;
  white-space: nowrap;
  min-width: 2.5em;
}

.tag {
  padding: 6px 14px;
  border: 1px solid transparent;
  border-radius: 4px;
  background: #f9fafb;
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  color: var(--text-secondary);
  white-space: nowrap;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
}

.tag:hover {
  background: #f3f4f6;
  color: var(--text-primary);
}

.tag.active {
  background: var(--blue-light);
  color: var(--blue);
  border-color: #bfdbfe;
  font-weight: 600;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
  padding: 14px 20px;
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

.filter-bar select,
.filter-bar .search {
  height: 34px;
  padding: 0 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  font: inherit;
  font-size: 13px;
  color: var(--text-primary);
  outline: none;
}

.filter-bar select:focus,
.filter-bar .search:focus {
  border-color: var(--blue);
}

.filter-bar .search {
  width: 100%;
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

.import-btn {
  border-radius: var(--radius-sm);
  font-weight: 500;
}

.banner {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
}

.banner.error,
.state.error {
  color: #b42318;
  background: var(--red-light);
}

.bi-pane {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  grid-template-rows: minmax(0, 1fr);
  gap: 16px;
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}

.pane {
  padding: 20px;
  overflow: auto;
  min-height: 0;
  max-height: 100%;
  overscroll-behavior: contain;
}

.pane h2,
.preview-heading {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.list-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: sticky;
  top: 0;
  background: var(--card-bg);
  z-index: 1;
  padding-bottom: 12px;
  margin-bottom: 4px;
}

.list-subtitle {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.section {
  margin-bottom: 16px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 4px 0 8px;
  gap: 8px;
}

.section-title {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.section-title::before {
  content: '';
  display: inline-block;
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: var(--blue);
  flex-shrink: 0;
}

.section-count {
  font-size: 12px;
  color: var(--text-tertiary);
  white-space: nowrap;
}

.product-list {
  padding-left: 14px;
  border-left: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-menu);
  cursor: pointer;
  position: relative;
  transition: background 0.15s;
}

.product-item:hover {
  background: #f9fafb;
}

.product-item.selected {
  background: var(--blue-light);
}

.product-item.selected::before {
  content: '';
  position: absolute;
  left: -15px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: var(--blue);
}

.product-no {
  font-size: 12px;
  color: var(--text-tertiary);
  width: 22px;
  flex-shrink: 0;
}

.product-code {
  font-size: 12px;
  color: var(--text-secondary);
  width: 90px;
  flex-shrink: 0;
}

.product-name {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-chain {
  display: inline-flex;
  align-items: center;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 4px;
  background: #f3f4f6;
  color: var(--text-secondary);
  font-weight: 500;
  flex-shrink: 0;
}

.product-item.selected .product-chain {
  background: rgba(255, 255, 255, 0.6);
  color: #4b5563;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
}

.type-tag {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 4px;
  background: var(--blue-light);
  color: var(--blue);
  font-size: 11px;
  font-weight: 500;
  flex-shrink: 0;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 8px;
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
  min-height: 120px;
}

.state.loading {
  color: var(--text-secondary);
}

.state.empty {
  color: var(--text-secondary);
  font-size: 13px;
}

.state.load-more {
  min-height: 0;
  flex-direction: row;
  padding: 12px 0;
  color: var(--text-tertiary);
}

.state.end {
  min-height: 0;
  padding: 8px 0;
  color: var(--text-tertiary);
}

.state.muted,
.preview-empty {
  color: var(--text-tertiary);
  font-size: 14px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border-color);
  border-top-color: var(--blue);
  border-radius: 50%;
  animation: catalog-spin 0.7s linear infinite;
  flex-shrink: 0;
}

@keyframes catalog-spin {
  to {
    transform: rotate(360deg);
  }
}

.preview-title {
  margin: 12px 0 14px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.3;
}

.preview-fields {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-meta-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
}

.preview-meta-item.sensitive {
  margin-top: 10px;
}

.preview-meta-item dt {
  color: var(--text-tertiary);
  margin: 0;
}

.preview-meta-item dd {
  margin: 0;
  color: var(--text-primary);
  font-weight: 500;
  text-align: right;
}

.preview-description {
  margin-top: 12px;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: 12px;
  background: #f9fafb;
  border-radius: var(--radius-menu);
  border: 1px solid var(--border-color);
}

.preview-description-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-tertiary);
  margin-bottom: 6px;
}

.preview-description p {
  margin: 0;
}

.preview-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.preview-actions .btn {
  flex: 1;
  min-width: 72px;
  border-radius: var(--radius-menu);
}

@media (max-width: 1100px) {
  .bi-pane {
    grid-template-columns: minmax(0, 1fr) 300px;
  }
}

@media (max-width: 900px) {
  .bi-pane {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(240px, 1fr) minmax(200px, 1fr);
  }
}
</style>
