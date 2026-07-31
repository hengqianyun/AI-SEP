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
const { catalogMaintenanceVisible } = useCanWrite(role)

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
</script>

<template>
  <div class="maintenance" data-testid="catalog-maintenance">
    <header class="page-header">
      <button type="button" class="link" @click="router.push('/catalog')">← 返回目录</button>
      <h1>目录维护</h1>
      <p class="subtitle">全部 / 已维护 / 待关联 · 单条与批量关联三级分类</p>
    </header>

    <p v-if="feedback" class="feedback" data-testid="maintenance-feedback">{{ feedback }}</p>

    <div class="toolbar">
      <div class="status-tabs" role="tablist" aria-label="维护状态">
        <button
          type="button"
          role="tab"
          :class="{ active: statusTab === 'ALL' }"
          data-testid="tab-all"
          @click="setStatusTab('ALL')"
        >
          全部
        </button>
        <button
          type="button"
          role="tab"
          :class="{ active: statusTab === 'MAINTAINED' }"
          data-testid="tab-maintained"
          @click="setStatusTab('MAINTAINED')"
        >
          已维护
        </button>
        <button
          type="button"
          role="tab"
          :class="{ active: statusTab === 'PENDING' }"
          data-testid="tab-pending"
          @click="setStatusTab('PENDING')"
        >
          待关联
        </button>
      </div>
      <div class="filters">
        <select
          :value="filterL1Id"
          data-testid="filter-l1"
          @change="setFilterL1(($event.target as HTMLSelectElement).value)"
        >
          <option value="">一级分类</option>
          <option v-for="c in l1Options" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <select
          :value="filterL2Id"
          data-testid="filter-l2"
          @change="setFilterL2(($event.target as HTMLSelectElement).value)"
        >
          <option value="">二级分类</option>
          <option v-for="c in l2Options" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <select
          :value="filterL3Id"
          data-testid="filter-l3"
          @change="setFilterL3(($event.target as HTMLSelectElement).value)"
        >
          <option value="">三级分类</option>
          <option v-for="c in l3Options" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </div>
    </div>

    <div
      class="batch-bar"
      :class="{ active: selectedCount > 0 }"
      data-testid="batch-bar"
    >
      <span data-testid="batch-count">已选择 {{ selectedCount }} 条</span>
      <div class="batch-actions">
        <select
          :value="batchL1Id"
          data-testid="batch-l1"
          @change="onBatchL1Change(($event.target as HTMLSelectElement).value)"
        >
          <option value="">一级分类</option>
          <option v-for="c in l1Options" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <select
          :value="batchL2Id"
          data-testid="batch-l2"
          @change="onBatchL2Change(($event.target as HTMLSelectElement).value)"
        >
          <option value="">二级分类</option>
          <option v-for="c in batchL2Options" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <select
          v-model="batchL3Id"
          data-testid="batch-l3"
        >
          <option value="">三级分类</option>
          <option v-for="c in batchL3Options" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <button
          type="button"
          class="primary"
          data-testid="batch-save"
          :disabled="selectedCount === 0 || state === 'saving'"
          @click="saveBatch"
        >
          批量保存
        </button>
      </div>
    </div>

    <div v-if="state === 'loading'" class="hint">加载中…</div>
    <div v-else-if="state === 'error' && !entries.length" class="hint error">{{ feedback }}</div>
    <div v-else-if="!entries.length" class="hint" data-testid="empty">暂无条目</div>

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
            :checked="selectedIds.has(entry.id)"
            :data-testid="`check-${entry.id}`"
            @change="toggleSelect(entry.id, ($event.target as HTMLInputElement).checked)"
          />
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
                :value="editL1Id"
                data-testid="edit-l1"
                @change="onEditL1Change(($event.target as HTMLSelectElement).value)"
              >
                <option value="">选择空间</option>
                <option v-for="c in l1Options" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <select
                :value="editL2Id"
                data-testid="edit-l2"
                @change="onEditL2Change(($event.target as HTMLSelectElement).value)"
              >
                <option value="">选择行业</option>
                <option v-for="c in editL2Options" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <select v-model="editL3Id" data-testid="edit-l3">
                <option value="">选择子类</option>
                <option v-for="c in editL3Options" :key="c.id" :value="c.id">{{ c.name }}</option>
              </select>
              <button type="button" class="primary" data-testid="edit-save" @click="saveEdit">
                保存
              </button>
              <button type="button" data-testid="edit-cancel" @click="cancelEdit">取消</button>
            </div>
          </template>
          <template v-else>
            <button type="button" data-testid="edit-start" @click="startEdit(entry)">维护</button>
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
      <button type="button" :disabled="page <= 1" @click="goPage(page - 1)">上一页</button>
      <button
        v-for="p in totalPages"
        :key="p"
        type="button"
        :class="{ active: p === page }"
        @click="goPage(p)"
      >
        {{ p }}
      </button>
      <button type="button" :disabled="page >= totalPages" @click="goPage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<style scoped>
.maintenance {
  padding: 20px 24px 40px;
  max-width: 1100px;
}
.page-header h1 {
  margin: 8px 0 4px;
  font-size: 22px;
}
.subtitle {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}
.link {
  border: none;
  background: none;
  color: #2563eb;
  cursor: pointer;
  padding: 0;
}
.feedback {
  margin: 12px 0;
  padding: 8px 12px;
  background: #f1f5f9;
  border-radius: 6px;
  font-size: 13px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0;
}
.status-tabs {
  display: flex;
  gap: 4px;
}
.status-tabs button {
  border: 1px solid #e2e8f0;
  background: #fff;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
}
.status-tabs button.active {
  background: #0f172a;
  color: #fff;
  border-color: #0f172a;
}
.filters,
.batch-actions,
.edit-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
select,
button {
  font: inherit;
}
select {
  padding: 6px 8px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #fff;
}
.batch-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  margin-bottom: 16px;
  opacity: 0.55;
}
.batch-bar.active {
  opacity: 1;
  border-style: solid;
  background: #f8fafc;
}
.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.item {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
}
.item.editing {
  border-color: #93c5fd;
  background: #f8fbff;
}
.item-left {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.name {
  font-weight: 600;
}
.meta {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}
.item-right {
  display: flex;
  gap: 10px;
  align-items: center;
}
.status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
}
.status.maintained {
  background: #dcfce7;
  color: #166534;
}
.status.pending {
  background: #ffedd5;
  color: #9a3412;
}
.primary {
  background: #0f172a;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}
.primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.hint {
  color: #64748b;
  padding: 24px 0;
}
.hint.error {
  color: #b91c1c;
}
.pagination {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  margin-top: 16px;
}
.pagination button {
  border: 1px solid #e2e8f0;
  background: #fff;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
}
.pagination button.active {
  background: #0f172a;
  color: #fff;
}
.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.total {
  color: #64748b;
  margin-right: 8px;
  font-size: 13px;
}
</style>
