<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { Pagination } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import type { MaintenanceScope } from '@/api/catalog'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import MaintenanceCategoryCascader from './components/MaintenanceCategoryCascader.vue'
import {
  PAGE_SELECT_ALL_LABEL,
  PAGE_SIZE_OPTION_LABELS,
  UNIFIED_MAINTAIN_LABEL,
  canEnterMaintenancePage,
  useCatalogMaintenance,
} from './composables/useCatalogMaintenance'

const props = withDefaults(
  defineProps<{
    /** full = 目录维护全平台；myCatalog = 我的目录 */
    scope?: MaintenanceScope
  }>(),
  { scope: 'full' },
)

const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { categoryMaintainVisible } = useCanWrite(role)

/** 传 getter，避免 /catalog/maintenance ↔ /my-catalog 复用实例时 scope 快照陈旧（FIND-WSC-907-001） */
const m = useCatalogMaintenance(() => props.scope)
const {
  state,
  feedback,
  entries,
  total,
  page,
  pageSize,
  statusTab,
  filterPath,
  selectedIds,
  editingId,
  editPath,
  batchPath,
  cascaderOptions,
  selectedCount,
  allPageSelected,
  somePageSelected,
  batchBarCopy,
  init,
  setStatusTab,
  applyFilterPath,
  applyPagination,
  toggleSelect,
  toggleSelectAllOnPage,
  startEdit,
  cancelEdit,
  applyEditPath,
  applyBatchPath,
  saveEdit,
  saveBatch,
  statusLabel,
} = m

const pageTitle = computed(() =>
  props.scope === 'myCatalog' ? '我的目录（本企业/本人）' : '目录维护（全量）',
)
const pageSubtitle = computed(() =>
  props.scope === 'myCatalog'
    ? '本企业 / 本人 · 全部 / 已维护 / 待关联 · 单条与统一维护三级分类'
    : '全平台 · 全部 / 已维护 / 待关联 · 单条与统一维护三级分类',
)

const selectAllRef = computed(() => allPageSelected.value)

onMounted(() => {
  if (!canEnterMaintenancePage(props.scope, role.value)) {
    void router.replace('/catalog')
    return
  }
  void init()
})

watch(
  () => props.scope,
  (next) => {
    if (!canEnterMaintenancePage(next, role.value)) {
      void router.replace('/catalog')
    }
  },
)

watch(somePageSelected, (partial) => {
  const el = document.querySelector<HTMLInputElement>('[data-testid="select-all-page"]')
  if (el) el.indeterminate = partial
})

function goCategoryAdmin() {
  if (!categoryMaintainVisible.value) return
  void router.push('/catalog/admin/categories')
}

/** ant-design-vue Pagination `@change`：(page, pageSize)。size 刚变时 applyPagination 会忽略紧随的旧 current。 */
function onPageChange(nextPage: number, nextSize: number) {
  void applyPagination(nextPage, nextSize)
}

/** size 变化也可能只走 `@showSizeChange`；强制 page=1，与 @change 共用 applyPagination */
function onShowSizeChange(_current: number, size: number) {
  void applyPagination(1, size)
}
</script>

<template>
  <div
    class="maintenance"
    data-testid="catalog-maintenance"
    :data-maintenance-scope="scope"
  >
    <header class="page-header wsc-surface">
      <div class="header-main">
        <h1 data-testid="workbench-page-title">{{ pageTitle }}</h1>
        <p class="subtitle">{{ pageSubtitle }}</p>
      </div>
    </header>

    <p
      v-if="feedback"
      class="feedback"
      :class="{ ok: state === 'success', err: state === 'error' }"
      data-testid="maintenance-feedback"
    >
      {{ feedback }}
    </p>

    <div class="maintenance-body wsc-surface" data-maint-filter-theme="ant-token-mapped">
      <div class="toolbar">
        <div class="filter-row">
          <div class="status-tabs" role="tablist" aria-label="维护状态">
            <button
              type="button"
              role="tab"
              class="status-tab"
              :class="{ active: statusTab === 'ALL' }"
              data-testid="tab-all"
              @click="setStatusTab('ALL')"
            >
              全部
            </button>
            <button
              type="button"
              role="tab"
              class="status-tab"
              :class="{ active: statusTab === 'MAINTAINED' }"
              data-testid="tab-maintained"
              @click="setStatusTab('MAINTAINED')"
            >
              已维护
            </button>
            <button
              type="button"
              role="tab"
              class="status-tab"
              :class="{ active: statusTab === 'PENDING' }"
              data-testid="tab-pending"
              @click="setStatusTab('PENDING')"
            >
              待关联
            </button>
          </div>
          <div class="filter-right">
            <MaintenanceCategoryCascader
              :options="cascaderOptions"
              :path="filterPath"
              placeholder="筛选分类（可选到任意级）"
              change-on-select
              testid="filter-category-cascader"
              @change="applyFilterPath"
            />
            <button
              v-if="categoryMaintainVisible"
              type="button"
              class="btn primary"
              data-testid="maintain-category-btn"
              @click="goCategoryAdmin"
            >
              维护三级分类
            </button>
          </div>
        </div>
      </div>

      <div
        v-if="selectedCount > 0"
        class="batch-bar active"
        data-testid="batch-bar"
      >
        <span data-testid="batch-count">{{ batchBarCopy }}</span>
        <div class="batch-actions">
          <MaintenanceCategoryCascader
            :options="cascaderOptions"
            :path="batchPath"
            placeholder="请选择三级分类"
            testid="batch-category-cascader"
            @change="applyBatchPath"
          />
          <button
            type="button"
            class="btn primary"
            data-testid="batch-save"
            :disabled="state === 'saving'"
            @click="saveBatch"
          >
            {{ UNIFIED_MAINTAIN_LABEL }}
          </button>
        </div>
      </div>

      <div v-if="state === 'loading'" class="hint">加载中…</div>
      <div v-else-if="state === 'error' && !entries.length" class="hint error">{{ feedback }}</div>
      <div v-else-if="!entries.length" class="hint empty" data-testid="empty">暂无条目</div>

      <ul v-else class="list" data-testid="maintenance-list">
        <li class="item select-all-row">
          <label class="select-all">
            <input
              type="checkbox"
              class="item-check"
              :checked="selectAllRef"
              data-testid="select-all-page"
              :aria-label="PAGE_SELECT_ALL_LABEL"
              @change="toggleSelectAllOnPage(($event.target as HTMLInputElement).checked)"
            />
            {{ PAGE_SELECT_ALL_LABEL }}
          </label>
        </li>
        <li
          v-for="entry in entries"
          :key="entry.id"
          class="item"
          :class="{ editing: editingId === entry.id }"
          :data-testid="`entry-${entry.id}`"
        >
          <div class="item-left">
            <input
              type="checkbox"
              class="item-check"
              :checked="selectedIds.has(entry.id)"
              :data-testid="`check-${entry.id}`"
              @change="toggleSelect(entry.id, ($event.target as HTMLInputElement).checked)"
            />
            <div class="item-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
            </div>
            <div class="info">
              <div class="name">{{ entry.productName }}</div>
              <div class="meta">
                {{ entry.productCode }} · {{ entry.categoryPath || '未关联' }}
              </div>
            </div>
          </div>
          <div class="item-right">
            <template v-if="editingId === entry.id">
              <div class="edit-row" data-testid="edit-row">
                <MaintenanceCategoryCascader
                  :options="cascaderOptions"
                  :path="editPath"
                  placeholder="请选择三级分类"
                  testid="edit-category-cascader"
                  @change="applyEditPath"
                />
                <button type="button" class="btn primary sm" data-testid="edit-save" @click="saveEdit">
                  保存
                </button>
                <button type="button" class="btn ghost sm" data-testid="edit-cancel" @click="cancelEdit">
                  取消
                </button>
              </div>
            </template>
            <template v-else>
              <button type="button" class="edit-btn" data-testid="edit-start" @click="startEdit(entry)">
                维护
              </button>
              <span
                class="status"
                :class="entry.maintenanceStatus === 'MAINTAINED' ? 'maintained' : 'pending'"
              >
                {{ statusLabel(entry.maintenanceStatus) }}
              </span>
            </template>
          </div>
        </li>
      </ul>

      <div v-if="total > 0" class="pagination" data-testid="pagination">
        <span class="total">共 {{ total }} 条</span>
        <Pagination
          :current="page"
          :page-size="pageSize"
          :total="total"
          :show-quick-jumper="true"
          :show-size-changer="true"
          :page-size-options="PAGE_SIZE_OPTION_LABELS"
          size="small"
          data-testid="a-pagination"
          @change="onPageChange"
          @showSizeChange="onShowSizeChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.maintenance {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0 0 24px;
  color: var(--text-primary);
  font-family: var(--font-family-sans);
  font-size: var(--font-size-base);
}

.wsc-surface {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow);
}

.page-header {
  padding: 16px 20px;
}

.header-main h1 {
  margin: 0 0 4px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.feedback {
  margin: 0;
  padding: 8px 12px;
  background: var(--blue-light);
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-primary);
}

.feedback.ok {
  background: var(--green-light);
  border-color: #a7f3d0;
  color: #166534;
}

.feedback.err {
  background: var(--red-light);
  border-color: #fecaca;
  color: #b91c1c;
}

.maintenance-body {
  padding: 20px;
  min-height: 420px;
}

.toolbar {
  margin-bottom: 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.status-tabs {
  display: flex;
  gap: 4px;
  background: #f3f4f6;
  border-radius: var(--radius-sm);
  padding: 3px;
}

.status-tab {
  padding: 5px 14px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: 4px;
  border: none;
  background: transparent;
  font: inherit;
  transition: background 0.15s, color 0.15s, box-shadow 0.15s;
}

.status-tab.active {
  background: var(--card-bg);
  color: var(--text-primary);
  box-shadow: var(--shadow-sm), 0 1px 3px rgba(0, 0, 0, 0.08);
}

.filter-right,
.batch-actions,
.edit-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
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

.btn.primary {
  border-color: var(--blue);
  background: var(--blue);
  color: #fff;
}

.btn.primary:hover {
  background: #2563eb;
  border-color: #2563eb;
}

.btn.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.ghost {
  background: #f3f4f6;
  color: var(--text-secondary);
  border-color: #d1d5db;
}

.btn.sm {
  height: auto;
  padding: 4px 12px;
  font-size: 12px;
}

.batch-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  background: var(--blue-light);
  border: 1px solid #bfdbfe;
  border-radius: var(--radius-menu);
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--text-primary);
}

.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-menu);
  background: var(--card-bg);
  transition: border-color 0.15s, box-shadow 0.15s, background 0.15s;
}

.item:hover {
  border-color: #bfdbfe;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.06);
}

.item.editing {
  border-color: #bfdbfe;
  background: #fafcff;
}

.select-all-row {
  padding: 8px 16px;
  background: #f9fafb;
}

.select-all {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.item-check {
  width: 16px;
  height: 16px;
  cursor: pointer;
  flex-shrink: 0;
}

.item-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-menu);
  background: var(--blue-light);
  color: var(--blue);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.info {
  min-width: 0;
}

.name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.meta {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.item-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.edit-btn {
  padding: 5px 16px;
  font-size: 12px;
  font-weight: 500;
  color: var(--blue);
  background: var(--blue-light);
  border: 1px solid #bfdbfe;
  border-radius: 4px;
  cursor: pointer;
  font: inherit;
  transition: background 0.15s;
}

.edit-btn:hover {
  background: #dbeafe;
}

.status {
  font-size: 12px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.status.maintained {
  background: #dcfce7;
  color: #166534;
}

.status.pending {
  background: #fef3c7;
  color: #92400e;
}

.hint {
  color: var(--text-secondary);
  padding: 32px 0;
  text-align: center;
  font-size: 14px;
}

.hint.error {
  color: #b91c1c;
  background: var(--red-light);
  border-radius: var(--radius-sm);
}

.hint.empty {
  color: var(--text-tertiary);
}

.pagination {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 16px;
  font-size: 13px;
}

.total {
  color: var(--text-secondary);
  margin-right: 8px;
}
</style>
