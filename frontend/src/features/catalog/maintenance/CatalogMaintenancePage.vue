<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import { useCatalogMaintenance } from './composables/useCatalogMaintenance'

const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { catalogMaintenanceVisible, categoryMaintainVisible } = useCanWrite(role)

const m = useCatalogMaintenance()
const {
  state,
  feedback,
  entries,
  total,
  page,
  statusTab,
  filterL1Id,
  filterL2Id,
  filterL3Id,
  selectedIds,
  editingId,
  editL1Id,
  editL2Id,
  editL3Id,
  batchL1Id,
  batchL2Id,
  batchL3Id,
  l1Options,
  l2Options,
  l3Options,
  editL2Options,
  editL3Options,
  batchL2Options,
  batchL3Options,
  selectedCount,
  totalPages,
  init,
  setStatusTab,
  setFilterL1,
  setFilterL2,
  setFilterL3,
  goPage,
  toggleSelect,
  startEdit,
  cancelEdit,
  onEditL1Change,
  onEditL2Change,
  onBatchL1Change,
  onBatchL2Change,
  saveEdit,
  saveBatch,
  statusLabel,
} = m

onMounted(() => {
  if (!catalogMaintenanceVisible.value) {
    void router.replace('/catalog')
    return
  }
  void init()
})

function goCategoryAdmin() {
  if (!categoryMaintainVisible.value) return
  void router.push('/catalog/admin/categories')
}
</script>

<template>
  <div class="maintenance" data-testid="catalog-maintenance">
    <header class="page-header wsc-surface">
      <div class="header-main">
        <h1>目录维护</h1>
        <p class="subtitle">全部 / 已维护 / 待关联 · 单条与批量关联三级分类</p>
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

    <div class="maintenance-body wsc-surface">
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
            <select
              class="maint-select"
              :value="filterL1Id"
              data-testid="filter-l1"
              @change="setFilterL1(($event.target as HTMLSelectElement).value)"
            >
              <option value="">一级分类</option>
              <option v-for="c in l1Options" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
            <select
              class="maint-select"
              :value="filterL2Id"
              data-testid="filter-l2"
              @change="setFilterL2(($event.target as HTMLSelectElement).value)"
            >
              <option value="">二级分类</option>
              <option v-for="c in l2Options" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
            <select
              class="maint-select"
              :value="filterL3Id"
              data-testid="filter-l3"
              @change="setFilterL3(($event.target as HTMLSelectElement).value)"
            >
              <option value="">三级分类</option>
              <option v-for="c in l3Options" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
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
        <span data-testid="batch-count">已选择 {{ selectedCount }} 条</span>
        <div class="batch-actions">
          <select
            class="maint-select"
            :value="batchL1Id"
            data-testid="batch-l1"
            @change="onBatchL1Change(($event.target as HTMLSelectElement).value)"
          >
            <option value="">一级分类</option>
            <option v-for="c in l1Options" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
          <select
            class="maint-select"
            :value="batchL2Id"
            data-testid="batch-l2"
            @change="onBatchL2Change(($event.target as HTMLSelectElement).value)"
          >
            <option value="">二级分类</option>
            <option v-for="c in batchL2Options" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
          <select v-model="batchL3Id" class="maint-select" data-testid="batch-l3">
            <option value="">三级分类</option>
            <option v-for="c in batchL3Options" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
          <button
            type="button"
            class="btn primary"
            data-testid="batch-save"
            :disabled="state === 'saving'"
            @click="saveBatch"
          >
            批量保存
          </button>
        </div>
      </div>

      <div v-if="state === 'loading'" class="hint">加载中…</div>
      <div v-else-if="state === 'error' && !entries.length" class="hint error">{{ feedback }}</div>
      <div v-else-if="!entries.length" class="hint empty" data-testid="empty">暂无条目</div>

      <ul v-else class="list" data-testid="maintenance-list">
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
                <select
                  class="maint-select sm"
                  :value="editL1Id"
                  data-testid="edit-l1"
                  @change="onEditL1Change(($event.target as HTMLSelectElement).value)"
                >
                  <option value="">选择空间</option>
                  <option v-for="c in l1Options" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
                <select
                  class="maint-select sm"
                  :value="editL2Id"
                  data-testid="edit-l2"
                  @change="onEditL2Change(($event.target as HTMLSelectElement).value)"
                >
                  <option value="">选择行业</option>
                  <option v-for="c in editL2Options" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
                <select v-model="editL3Id" class="maint-select sm" data-testid="edit-l3">
                  <option value="">选择子类</option>
                  <option v-for="c in editL3Options" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
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
        <button type="button" class="page-btn" :disabled="page <= 1" @click="goPage(page - 1)">
          上一页
        </button>
        <button
          v-for="p in totalPages"
          :key="p"
          type="button"
          class="page-btn"
          :class="{ active: p === page }"
          @click="goPage(p)"
        >
          {{ p }}
        </button>
        <button
          type="button"
          class="page-btn"
          :disabled="page >= totalPages"
          @click="goPage(page + 1)"
        >
          下一页
        </button>
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

.maint-select {
  padding: 7px 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-primary);
  background: var(--card-bg);
  outline: none;
  cursor: pointer;
  font-family: inherit;
}

.maint-select.sm {
  padding: 4px 8px;
  font-size: 12px;
  background: #f9fafb;
}

.maint-select:focus {
  border-color: var(--blue);
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
  gap: 4px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 16px;
  font-size: 13px;
}

.page-btn {
  padding: 5px 11px;
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  border-radius: 4px;
  cursor: pointer;
  color: var(--text-secondary);
  font: inherit;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
}

.page-btn:hover:not(:disabled) {
  border-color: #bfdbfe;
  color: var(--blue);
}

.page-btn.active {
  background: var(--blue);
  color: #fff;
  border-color: var(--blue);
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.total {
  color: var(--text-secondary);
  margin-right: 8px;
}
</style>
