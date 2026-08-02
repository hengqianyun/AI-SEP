<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'
import { useCategoryAdmin } from './composables/useCategoryAdmin'

const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const { categoryMaintainVisible } = useCanWrite(role)

const admin = useCategoryAdmin()
const {
  state,
  feedback,
  dirty,
  draftL1Name,
  draftL2Name,
  draftL3Name,
  selectedL1Id,
  selectedL2Id,
  renameMap,
  l1List,
  l2List,
  l3List,
  load,
  selectL1,
  selectL2,
  markDirty,
  addL1,
  addL2,
  addL3,
  saveRename,
  remove,
} = admin

onMounted(() => {
  if (!categoryMaintainVisible.value) {
    void router.replace('/catalog')
    return
  }
  void load()
})

function closeShell() {
  void router.push('/catalog')
}

function onFooterSave() {
  if (dirty.value) {
    feedback.value = '请先对各分类名称点击「保存」后再关闭'
    return
  }
  closeShell()
}
</script>

<template>
  <!--
    载体 B（PLAN-WSC-3.1 §1.5）：保留独立路由，页内等价 modal 壳（约 960px）。
    与原型 browse overlay 偏差记入差距草稿（非 P0）。
  -->
  <div
    v-if="categoryMaintainVisible"
    class="category-overlay"
    data-testid="category-admin"
    role="dialog"
    aria-modal="true"
    aria-labelledby="category-admin-title"
  >
    <div class="category-modal" id="category-modal">
      <header class="modal-header">
        <div class="header-left">
          <h3 id="category-admin-title">维护三级分类</h3>
          <span v-if="dirty" class="pending-indicator" data-testid="dirty-hint">
            <span class="pending-dot" aria-hidden="true" />
            待保存
          </span>
        </div>
        <button type="button" class="modal-close" aria-label="关闭" @click="closeShell">
          <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18" />
            <line x1="6" y1="6" x2="18" y2="18" />
          </svg>
        </button>
      </header>

      <div class="modal-body">
        <div v-if="state === 'loading'" class="hint">加载中…</div>

        <div v-else class="category-layout">
          <section class="category-col col-l1" data-testid="pane-l1">
            <div class="col-header">一级分类</div>
            <div class="col-body">
              <ul class="cat-list">
                <li
                  v-for="c in l1List"
                  :key="c.id"
                  :class="{ active: selectedL1Id === c.id }"
                >
                  <button type="button" class="select-btn" @click="selectL1(c.id)">选择</button>
                  <input v-model="renameMap[c.id]" @input="markDirty" />
                  <button type="button" class="btn sm" @click="saveRename(c.id, 'L1')">保存</button>
                  <button type="button" class="btn sm danger" @click="remove(c.id)">删除</button>
                </li>
              </ul>
              <div class="add-row">
                <input v-model="draftL1Name" placeholder="请输入空间名称" />
                <button type="button" class="add-btn" @click="addL1">+ 新增空间</button>
              </div>
            </div>
          </section>

          <section class="category-col col-l2" data-testid="pane-l2">
            <div class="col-header">二级分类</div>
            <div class="col-body">
              <ul class="cat-list">
                <li
                  v-for="c in l2List"
                  :key="c.id"
                  :class="{ active: selectedL2Id === c.id }"
                >
                  <button type="button" class="select-btn" @click="selectL2(c.id)">选择</button>
                  <input v-model="renameMap[c.id]" @input="markDirty" />
                  <button type="button" class="btn sm" @click="saveRename(c.id, 'L2')">保存</button>
                  <button type="button" class="btn sm danger" @click="remove(c.id)">删除</button>
                </li>
              </ul>
              <div class="add-row">
                <input
                  v-model="draftL2Name"
                  placeholder="请输入二级分类名称"
                  :disabled="!selectedL1Id"
                />
                <button type="button" class="add-btn" :disabled="!selectedL1Id" @click="addL2">
                  + 新增二级分类
                </button>
              </div>
            </div>
          </section>

          <section class="category-col col-l3" data-testid="pane-l3">
            <div class="col-header">三级分类</div>
            <div class="col-body">
              <ul class="cat-list">
                <li v-for="c in l3List" :key="c.id">
                  <input v-model="renameMap[c.id]" @input="markDirty" />
                  <button type="button" class="btn sm" @click="saveRename(c.id, 'L3')">保存</button>
                  <button type="button" class="btn sm danger" @click="remove(c.id)">删除</button>
                </li>
              </ul>
              <div class="add-row">
                <input
                  v-model="draftL3Name"
                  placeholder="请输入三级分类名称"
                  :disabled="!selectedL2Id"
                />
                <button type="button" class="add-btn" :disabled="!selectedL2Id" @click="addL3">
                  + 新增三级分类
                </button>
              </div>
            </div>
          </section>
        </div>

        <p
          v-if="feedback"
          class="feedback"
          :class="state === 'success' ? 'ok' : state === 'error' ? 'err' : ''"
          data-testid="admin-feedback"
        >
          {{ feedback }}
        </p>
      </div>

      <footer class="modal-footer">
        <button type="button" class="btn" id="category-modal-cancel" @click="closeShell">
          取消
        </button>
        <button
          type="button"
          class="btn primary"
          id="category-modal-save"
          :disabled="state === 'saving'"
          @click="onFooterSave"
        >
          保存
        </button>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.category-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 24px 16px;
  font-family: var(--font-family-sans);
  font-size: var(--font-size-base);
  color: var(--text-primary);
}

.category-modal {
  width: min(960px, 96vw);
  max-height: 80vh;
  background: var(--card-bg);
  border-radius: 14px;
  box-shadow: var(--shadow-lg);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 20px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.pending-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #b36b00;
  background: var(--orange-light);
  border: 1px solid #fcd34d;
  padding: 2px 8px;
  border-radius: 999px;
}

.pending-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--orange);
}

.modal-close {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-secondary);
  transition: background 0.15s, color 0.15s;
}

.modal-close:hover {
  background: #f3f4f6;
  color: var(--text-primary);
}

.modal-body {
  padding: 0;
  overflow: auto;
  flex: 1;
  min-height: 0;
}

.hint {
  padding: 40px 20px;
  text-align: center;
  color: var(--text-secondary);
}

.category-layout {
  display: flex;
  min-height: 360px;
}

.category-col {
  border-right: 1px solid var(--border-color);
  padding: 16px;
  display: flex;
  flex-direction: column;
  background: var(--card-bg);
  min-width: 0;
}

.category-col:last-child {
  border-right: none;
}

.col-l1 {
  width: 25%;
}

.col-l2 {
  width: 28%;
}

.col-l3 {
  width: 47%;
}

.col-header {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
  flex-shrink: 0;
}

.col-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
  min-height: 0;
}

.cat-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 48px;
  overflow: auto;
}

.cat-list li {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  padding: 6px;
  border-radius: 4px;
  background: #fafbfc;
  border: 1px solid transparent;
}

.cat-list li.active {
  background: var(--blue-light);
  border-color: #bfdbfe;
}

.cat-list input,
.add-row input {
  flex: 1;
  min-width: 80px;
  padding: 4px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font: inherit;
  font-size: 13px;
  color: var(--text-primary);
  background: var(--card-bg);
}

.cat-list input:focus,
.add-row input:focus {
  outline: none;
  border-color: var(--blue);
}

.select-btn {
  font-size: 12px;
  padding: 3px 8px;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  background: var(--card-bg);
  cursor: pointer;
  color: var(--text-secondary);
  font: inherit;
}

.btn {
  padding: 6px 14px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: var(--card-bg);
  cursor: pointer;
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.btn.sm {
  padding: 3px 8px;
  font-size: 12px;
}

.btn.primary {
  background: var(--blue);
  border-color: var(--blue);
  color: #fff;
}

.btn.primary:hover {
  background: #2563eb;
}

.btn.primary:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.btn.danger {
  color: #a33;
  border-color: #fecaca;
}

.add-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
  padding-top: 8px;
}

.add-btn {
  padding: 7px 10px;
  border: 1px dashed #bfdbfe;
  border-radius: var(--radius-sm);
  background: var(--blue-light);
  color: var(--blue);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  text-align: center;
}

.add-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.feedback {
  margin: 0;
  padding: 10px 20px 14px;
  font-size: 13px;
  border-top: 1px solid #f3f4f6;
  color: var(--text-secondary);
}

.feedback.ok {
  color: #1a7f37;
  background: var(--green-light);
}

.feedback.err {
  color: #a33;
  background: var(--red-light);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid var(--border-color);
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .category-layout {
    flex-direction: column;
  }

  .col-l1,
  .col-l2,
  .col-l3 {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--border-color);
  }

  .category-col:last-child {
    border-bottom: none;
  }
}
</style>
