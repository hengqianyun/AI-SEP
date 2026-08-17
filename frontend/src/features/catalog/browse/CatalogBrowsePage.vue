<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { canSeeMyProducts, useCanWrite } from '@/features/auth/composables/useCanWrite'
import ImportDialog from '@/features/catalog/import/ImportDialog.vue'
import BrowseFilterBar from './components/BrowseFilterBar.vue'
import CirculationSeatMap from './components/CirculationSeatMap.vue'
import SensitiveId from './components/SensitiveId.vue'
import {
  fillUntilScrollable,
  isNearScrollBottom,
  LIST_SCROLL_THRESHOLD,
  useCatalogBrowse,
} from './composables/useCatalogBrowse'
import { useCatalogImportEntry } from './composables/useCatalogImportEntry'
import { deleteProduct } from '@/api/catalog'
import { ApiError } from '@/api/client'
import { CATALOG_EMPTY_MESSAGE, MINE_EMPTY_MESSAGE, productTypeLabel } from './utils/labels'

/**
 * 双表面 + 座序图挂载约定（供 604 只读消费）：
 * - mode: 'catalog' | 'mine'（路由 props）
 * - showSeatMap: 显式布尔；未传时 catalog=true、mine=false
 * - 挂载点：`<CirculationSeatMap v-if="seatMapVisible" />`（本任务不实现座序图内部）
 */
const props = withDefaults(
  defineProps<{
    mode?: 'catalog' | 'mine'
    /** 预留：公共目录 true；我的产品 false。604 仅改 CirculationSeatMap.vue */
    showSeatMap?: boolean
  }>(),
  { mode: 'catalog', showSeatMap: undefined },
)

const isMine = computed(() => props.mode === 'mine')
const seatMapVisible = computed(() =>
  props.showSeatMap !== undefined ? props.showSeatMap : !isMine.value,
)
const pageTitle = computed(() => (isMine.value ? '我的数据产品' : '数据目录'))
const pageSubtitle = computed(() =>
  isMine.value
    ? '管理本人创建的数据产品（新增 / 编辑 / 导入）'
    : '按空间 / 行业浏览、子类分组预览数据产品',
)
const emptyMessage = computed(() => (isMine.value ? MINE_EMPTY_MESSAGE : CATALOG_EMPTY_MESSAGE))

const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { productWriteVisible, productImportVisible } = useCanWrite(role)
const writeVisible = computed(() => isMine.value && productWriteVisible.value)
const importVisible = computed(() => isMine.value && productImportVisible.value)
const { importOpen, openImport, onImportClosed } = useCatalogImportEntry(role)

/** 预览卡内联删除二次确认（非全页 modal） */
const deleteConfirmVisible = ref(false)
const deleteBusy = ref(false)
const deleteError = ref<string | null>(null)

/** 传 computed，避免路由复用 /catalog↔/my-products 时 mine 快照陈旧（FIND-WSC-603-R1-001） */
const browse = useCatalogBrowse({ mine: isMine })
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
  allL2Categories,
  selectProduct,
  applyCategoryPath,
  applyFilters,
  resetFilters,
  loadProducts,
  loadCategories,
  loadMore,
  init,
} = browse

watch(selectedProductId, () => {
  deleteConfirmVisible.value = false
  deleteError.value = null
  deleteBusy.value = false
})

/** 列表 pane：滚动触底 + 未溢出自动续载度量（FIND-WSC-305-R1-001） */
const listScrollEl = ref<HTMLElement | null>(null)
const catalogBrowseEl = ref<HTMLElement | null>(null)
const stickyChromeEl = ref<HTMLElement | null>(null)
const seatMapSlotEl = ref<HTMLElement | null>(null)
/** 公共目录座序图滚动折叠（scheme A）；仅 seatMapVisible 时生效 */
const seatMapCollapsed = ref(false)

const COLLAPSE_SCROLL_TOP = 24

let fillInFlight = false
let listResizeObserver: ResizeObserver | null = null
let stickyChromeResizeObserver: ResizeObserver | null = null
/** WorkbenchLayout `.main` 或文档滚动容器 */
let mainScrollTarget: HTMLElement | Window | null = null

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

/**
 * 定位主区滚动容器：自 `.catalog-browse` 向上找 overflow auto/scroll；
 * 否则回退 `.workbench > .main`，再否则为 document 滚动。
 */
function findMainScrollContainer(from: HTMLElement): HTMLElement | Window {
  let node: HTMLElement | null = from.parentElement
  while (node && node !== document.documentElement) {
    const { overflowY } = getComputedStyle(node)
    if (overflowY === 'auto' || overflowY === 'scroll' || overflowY === 'overlay') {
      return node
    }
    node = node.parentElement
  }
  const main = document.querySelector('.workbench > .main') as HTMLElement | null
  if (main) {
    const { overflowY } = getComputedStyle(main)
    if (overflowY === 'auto' || overflowY === 'scroll' || overflowY === 'overlay') {
      return main
    }
  }
  return window
}

function getScrollTop(target: HTMLElement | Window): number {
  if (target === window) {
    return window.scrollY || document.documentElement.scrollTop || 0
  }
  return (target as HTMLElement).scrollTop
}

/**
 * 座序图折叠滞后：下滚收起。
 * 收起后进入 pinned（筛栏+列表视口分区），不再用 sticky，避免列表滚到筛栏下重叠。
 * 展开：列表已滚到顶时继续上滑（onCatalogWheel）。
 */
function updateSeatMapCollapse() {
  if (!seatMapVisible.value) {
    seatMapCollapsed.value = false
    return
  }
  if (!mainScrollTarget) return
  const scrollTop = getScrollTop(mainScrollTarget)

  if (!seatMapCollapsed.value) {
    let geometryCollapse = false
    const slot = seatMapSlotEl.value
    if (slot) {
      const rect = slot.getBoundingClientRect()
      geometryCollapse = rect.top < 0 || rect.bottom <= 0
    }
    if (scrollTop > COLLAPSE_SCROLL_TOP || geometryCollapse) {
      seatMapCollapsed.value = true
      void nextTick(() => {
        // 钉住目录顶，使 workspace 吃满视口且不与页面滚动叠层
        catalogBrowseEl.value?.scrollIntoView({ block: 'start' })
        updateStickyChromeHeight()
        void ensureListFilled()
      })
    }
  }
  // 展开仅由 onCatalogWheel（列表顶上滑）触发；collapse 后 scrollIntoView 会使 scrollTop≈0，不可据此自动展开
}

function onMainScroll() {
  updateSeatMapCollapse()
}

/** 折叠钉住后：列表已在顶且继续上滑时展开座序图 */
function onCatalogWheel(e: WheelEvent) {
  if (!seatMapVisible.value || !seatMapCollapsed.value) return
  if (e.deltaY >= 0) return
  const list = listScrollEl.value
  if (list && list.scrollTop > 2) return
  seatMapCollapsed.value = false
  void nextTick(() => {
    updateStickyChromeHeight()
  })
}

function updateStickyChromeHeight() {
  const chrome = stickyChromeEl.value
  const root = catalogBrowseEl.value
  if (!chrome || !root) return
  const h = Math.ceil(chrome.getBoundingClientRect().height)
  root.style.setProperty('--sticky-chrome-h', `${h}px`)
}

function bindMainScroll() {
  unbindMainScroll()
  const root = catalogBrowseEl.value
  if (!root || !seatMapVisible.value) return
  mainScrollTarget = findMainScrollContainer(root)
  mainScrollTarget.addEventListener('scroll', onMainScroll, { passive: true })
  updateSeatMapCollapse()
}

function unbindMainScroll() {
  if (mainScrollTarget) {
    mainScrollTarget.removeEventListener('scroll', onMainScroll)
    mainScrollTarget = null
  }
}

onMounted(() => {
  if (isMine.value && !canSeeMyProducts(role.value)) {
    void router.replace('/catalog')
    return
  }
  void init().then(() => ensureListFilled())
  void nextTick(() => {
    const el = listScrollEl.value
    if (el && typeof ResizeObserver !== 'undefined') {
      listResizeObserver = new ResizeObserver(() => {
        void ensureListFilled()
      })
      listResizeObserver.observe(el)
    }
    if (stickyChromeEl.value && typeof ResizeObserver !== 'undefined') {
      stickyChromeResizeObserver = new ResizeObserver(() => {
        updateStickyChromeHeight()
      })
      stickyChromeResizeObserver.observe(stickyChromeEl.value)
      updateStickyChromeHeight()
    }
    bindMainScroll()
  })
})

onBeforeUnmount(() => {
  listResizeObserver?.disconnect()
  listResizeObserver = null
  stickyChromeResizeObserver?.disconnect()
  stickyChromeResizeObserver = null
  unbindMainScroll()
})

watch(
  () => [listState.value, products.value.length, hasMore.value] as const,
  () => {
    void ensureListFilled()
  },
)

watch(seatMapVisible, (visible) => {
  if (visible) {
    void nextTick(() => bindMainScroll())
  } else {
    seatMapCollapsed.value = false
    unbindMainScroll()
  }
})

function onListScroll(e: Event) {
  const el = e.target as HTMLElement
  if (!el || loadingMore.value || !hasMore.value || listState.value !== 'ready') return
  if (isNearScrollBottom(el)) {
    void loadMore()
  }
}

function mineQuery() {
  return isMine.value ? { from: 'mine' as const } : undefined
}

function goDetail() {
  if (!selectedProductId.value) return
  void router.push({
    path: `/catalog/products/${selectedProductId.value}`,
    query: mineQuery(),
  })
}

function goChain() {
  if (!selectedProductId.value) return
  void router.push(`/chain/products/${selectedProductId.value}`)
}

function goEdit() {
  if (!selectedProductId.value || !writeVisible.value) return
  void router.push({
    path: `/catalog/products/${selectedProductId.value}/edit`,
    query: { from: 'mine' },
  })
}

function goCreate() {
  if (!writeVisible.value) return
  void router.push({ path: '/catalog/products/new', query: { from: 'mine' } })
}

function askDelete() {
  if (!selectedProductId.value || !writeVisible.value) return
  deleteError.value = null
  deleteConfirmVisible.value = true
}

function cancelDelete() {
  deleteConfirmVisible.value = false
  deleteError.value = null
}

async function confirmDelete() {
  const id = selectedProductId.value
  if (!id || !writeVisible.value || deleteBusy.value) return
  deleteBusy.value = true
  deleteError.value = null
  try {
    await deleteProduct(id)
    deleteConfirmVisible.value = false
    selectedProductId.value = null
    preview.value = null
    previewState.value = 'idle'
    await loadProducts()
  } catch (e) {
    deleteError.value = e instanceof ApiError ? e.message : '删除失败，请稍后重试'
  } finally {
    deleteBusy.value = false
  }
}

function rowIndex(sectionOffset: number, idx: number) {
  return sectionOffset + idx + 1
}

function padNo(n: number) {
  return String(n).padStart(2, '0')
}

/** 审计展示：显示名优先，回退 userId；只读。 */
function auditActorLabel(name?: string | null, userId?: string | null): string {
  const shown = (name && name.trim()) || (userId && userId.trim()) || ''
  return shown || '—'
}
</script>

<template>
  <div
    ref="catalogBrowseEl"
    class="catalog-browse"
    :class="{ 'catalog-browse--pinned': seatMapVisible && seatMapCollapsed }"
    data-testid="catalog-browse"
    @wheel="onCatalogWheel"
  >
    <!-- 方案 A：公共目录不渲染标题卡；我的产品保留标题/副标题与写入口 -->
    <header v-if="isMine" class="page-header wsc-surface">
      <div class="header-main">
        <h1>{{ pageTitle }}</h1>
        <p class="subtitle">{{ pageSubtitle }}</p>
      </div>
      <div class="header-actions">
        <button
          v-if="writeVisible"
          type="button"
          class="btn primary"
          data-testid="catalog-open-create"
          @click="goCreate"
        >
          新增产品
        </button>
        <button
          v-if="importVisible"
          type="button"
          class="btn primary import-btn"
          data-testid="catalog-open-import"
          @click="openImport"
        >
          批量导入产品
        </button>
      </div>
    </header>

    <ImportDialog v-if="importVisible" v-model="importOpen" @closed="onImportClosed" />

    <div
      v-if="seatMapVisible"
      ref="seatMapSlotEl"
      class="seat-map-slot"
      :class="{ 'seat-map-slot--collapsed': seatMapCollapsed }"
      data-testid="catalog-seat-map-slot"
    >
      <div class="seat-map-slot-inner">
        <CirculationSeatMap />
      </div>
    </div>

    <!-- 筛栏与列表同壳：不用 sticky 盖住列表；折叠后 pinned flex 分区 -->
    <div class="workspace-shell">
    <div ref="stickyChromeEl" class="sticky-chrome">
      <BrowseFilterBar
        v-model:filters="filters"
        :l1-categories="l1Categories"
        :all-l2-categories="allL2Categories"
        :categories-state="categoriesState"
        :categories-error="categoriesError"
        @apply="applyFilters"
        @reset="resetFilters"
        @category-change="applyCategoryPath"
      />
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
          {{ emptyMessage }}
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
            <div class="preview-meta-item">
              <dt>创建人</dt>
              <dd data-testid="catalog-preview-create-by">{{ auditActorLabel(preview.createByName, preview.createBy) }}</dd>
            </div>
            <div class="preview-meta-item">
              <dt>操作人</dt>
              <dd data-testid="catalog-preview-update-by">{{ auditActorLabel(preview.updateByName, preview.updateBy) }}</dd>
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
          <div v-if="deleteError" class="state error preview-delete-error" data-testid="catalog-delete-error">
            {{ deleteError }}
          </div>
          <div class="preview-actions">
            <template v-if="deleteConfirmVisible">
              <div class="preview-delete-confirm" data-testid="catalog-delete-confirm-strip">
                <p class="preview-delete-msg">确定删除该产品？删除后不可恢复</p>
                <div class="preview-delete-btns">
                  <button
                    type="button"
                    class="btn"
                    data-testid="catalog-delete-cancel"
                    :disabled="deleteBusy"
                    @click="cancelDelete"
                  >
                    取消
                  </button>
                  <button
                    type="button"
                    class="btn danger"
                    data-testid="catalog-delete-confirm"
                    :disabled="deleteBusy"
                    @click="confirmDelete"
                  >
                    {{ deleteBusy ? '删除中…' : '确认删除' }}
                  </button>
                </div>
              </div>
            </template>
            <template v-else>
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
                v-if="writeVisible"
                type="button"
                class="btn"
                data-testid="catalog-go-edit"
                @click="goEdit"
              >
                编辑
              </button>
              <button
                v-if="writeVisible"
                type="button"
                class="btn danger"
                data-testid="catalog-go-delete"
                @click="askDelete"
              >
                删除
              </button>
            </template>
          </div>
        </template>
      </aside>
    </div>
    </div>
  </div>
</template>

<style scoped>
.catalog-browse {
  /*
   * 页面可自然增高并由主区滚动（FIX-WSC-606：座序图+筛栏超出视口时不再被 overflow:hidden 锁死）。
   * 列表触底分页仍由 .bi-pane / .pane.list 的固定高度 + overflow:auto 承担。
   * scheme A：--list-min-h 保证 ≥5 行；--sticky-chrome-h 由 ResizeObserver 写入。
   */
  --product-row-h: 48px;
  --list-head-h: 56px;
  --list-pad-bottom: 20px;
  --min-visible-rows: 5;
  --list-min-h: calc(
    var(--min-visible-rows) * var(--product-row-h) + var(--list-head-h) + var(--list-pad-bottom)
  );
  --sticky-chrome-h: 0px;
  --main-pad-budget: calc(2 * var(--main-padding, 24px));
  /* 列表区额外高度：略增高双栏可视区（约两行） */
  --list-viewport-bonus: 88px;

  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  height: auto;
  max-height: none;
  overflow: visible;
  padding: 0;
  box-sizing: border-box;
  color: var(--text-primary);
  font-family: var(--font-family-sans);
  font-size: var(--font-size-base);
}

.page-header,
.workspace-shell,
.sticky-chrome,
.banner,
.seat-map-slot {
  flex-shrink: 0;
}

.catalog-browse--pinned .workspace-shell {
  flex-shrink: 1;
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

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

/*
 * 座序图折叠：CSS grid 0fr/1fr 动画（勿用 display:none，否则无法过渡）。
 * 高分屏不设 max-height，完整展示；短屏才限制展开高度并允许内滚。
 */
.seat-map-slot {
  display: grid;
  grid-template-rows: 1fr;
  opacity: 1;
  overflow: hidden;
  transition:
    grid-template-rows 0.4s cubic-bezier(0.4, 0, 0.2, 1),
    opacity 0.28s ease,
    margin 0.4s ease;
}

.seat-map-slot--collapsed {
  grid-template-rows: 0fr;
  opacity: 0;
  /* 抵消 .catalog-browse gap，折叠后不留空隙 */
  margin-bottom: -12px;
  pointer-events: none;
}

.seat-map-slot-inner {
  min-height: 0;
  overflow: hidden;
}

@media (max-height: 800px) {
  .seat-map-slot:not(.seat-map-slot--collapsed) .seat-map-slot-inner {
    max-height: min(36vh, 280px);
    overflow-y: auto;
  }
}

@media (prefers-reduced-motion: reduce) {
  .seat-map-slot {
    transition-duration: 0.01ms;
  }
}

/*
 * 筛栏不再 sticky（sticky 会使下方列表滚到筛栏下形成重叠）。
 * 折叠后 catalog-browse--pinned：视口内 flex 分区，列表只在 bi-pane 内滚。
 */
.workspace-shell {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  flex: 1 1 auto;
}

.sticky-chrome {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--main-bg);
}

.catalog-browse--pinned {
  height: calc(100dvh - var(--main-pad-budget));
  max-height: calc(100dvh - var(--main-pad-budget));
  overflow: hidden;
}

.catalog-browse--pinned .workspace-shell {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}

.catalog-browse--pinned .bi-pane {
  flex: 1 1 auto;
  min-height: 0;
  height: auto;
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

.btn.danger {
  color: #a33;
  border-color: #fecaca;
}

.btn.danger:hover {
  background: #fef2f2;
  color: #991b1b;
  border-color: #fecaca;
}

.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
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
  /* 宽屏下列表 + 预览按比例；min 高度保证 ≥5 行可见 */
  grid-template-columns: minmax(0, 1.55fr) minmax(380px, 1fr);
  grid-template-rows: minmax(0, 1fr);
  gap: 16px;
  flex: 0 0 auto;
  min-height: calc(var(--list-min-h) + var(--product-row-h));
  height: max(
    calc(var(--list-min-h) + var(--product-row-h)),
    calc(
      100dvh - var(--sticky-chrome-h) - var(--main-pad-budget) + var(--list-viewport-bonus)
    )
  );
  overflow: hidden;
}

.pane {
  overflow: auto;
  min-height: 0;
  max-height: 100%;
  overscroll-behavior: contain;
}

.pane.list {
  /* 顶 padding 移入 sticky .list-head，避免 top:0 露缝 */
  padding: 0 20px 20px;
}

.pane.preview {
  padding: 20px;
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
  z-index: 2;
  background: var(--card-bg);
  padding: 20px 0 12px;
  margin: 0 0 4px;
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

.preview-delete-error {
  margin-top: 12px;
  font-size: 13px;
}

.preview-delete-confirm {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #fecaca;
  border-radius: var(--radius-menu);
  background: #fef2f2;
}

.preview-delete-msg {
  margin: 0;
  font-size: 13px;
  color: #991b1b;
  line-height: 1.4;
}

.preview-delete-btns {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-delete-btns .btn {
  flex: 1;
  min-width: 72px;
}

@media (max-width: 1100px) {
  .bi-pane {
    grid-template-columns: minmax(0, 1fr) minmax(280px, 320px);
  }
}

@media (max-width: 900px) {
  .bi-pane {
    grid-template-columns: 1fr;
    grid-template-rows: minmax(240px, 1fr) minmax(200px, 1fr);
  }
}
</style>
