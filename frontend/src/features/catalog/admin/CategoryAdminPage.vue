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
</script>

<template>
  <div class="category-admin" data-testid="category-admin">
    <div class="panel" role="dialog" aria-labelledby="category-admin-title">
      <header class="page-header">
        <button type="button" class="link" @click="router.push('/catalog')">← 返回目录</button>
        <h1 id="category-admin-title">维护三级分类</h1>
        <p class="subtitle">空间 → 行业 → 子类</p>
        <p v-if="dirty" class="pending" data-testid="dirty-hint">有未保存的名称变更（待保存）</p>
      </header>

      <div v-if="state === 'loading'">加载中…</div>

      <div v-else class="panes">
        <section class="pane" data-testid="pane-l1">
          <h2>空间（一级）</h2>
          <ul>
            <li v-for="c in l1List" :key="c.id" :class="{ active: selectedL1Id === c.id }">
              <button type="button" class="select" @click="selectL1(c.id)">选择</button>
              <input v-model="renameMap[c.id]" @input="markDirty" />
              <button type="button" @click="saveRename(c.id, 'L1')">保存</button>
              <button type="button" class="danger" @click="remove(c.id)">删除</button>
            </li>
          </ul>
          <div class="add-row">
            <input v-model="draftL1Name" placeholder="新空间名称" />
            <button type="button" class="primary" @click="addL1">新增空间</button>
          </div>
        </section>

        <section class="pane" data-testid="pane-l2">
          <h2>行业（二级）</h2>
          <ul>
            <li v-for="c in l2List" :key="c.id" :class="{ active: selectedL2Id === c.id }">
              <button type="button" class="select" @click="selectL2(c.id)">选择</button>
              <input v-model="renameMap[c.id]" @input="markDirty" />
              <button type="button" @click="saveRename(c.id, 'L2')">保存</button>
              <button type="button" class="danger" @click="remove(c.id)">删除</button>
            </li>
          </ul>
          <div class="add-row">
            <input v-model="draftL2Name" placeholder="新行业名称" :disabled="!selectedL1Id" />
            <button type="button" class="primary" :disabled="!selectedL1Id" @click="addL2">
              新增行业
            </button>
          </div>
        </section>

        <section class="pane" data-testid="pane-l3">
          <h2>子类（三级）</h2>
          <ul>
            <li v-for="c in l3List" :key="c.id">
              <input v-model="renameMap[c.id]" @input="markDirty" />
              <button type="button" @click="saveRename(c.id, 'L3')">保存</button>
              <button type="button" class="danger" @click="remove(c.id)">删除</button>
            </li>
          </ul>
          <div class="add-row">
            <input v-model="draftL3Name" placeholder="新子类名称" :disabled="!selectedL2Id" />
            <button type="button" class="primary" :disabled="!selectedL2Id" @click="addL3">
              新增子类
            </button>
          </div>
        </section>
      </div>

      <p
        v-if="feedback"
        :class="state === 'success' ? 'ok' : state === 'error' ? 'err' : ''"
        data-testid="admin-feedback"
      >
        {{ feedback }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.category-admin {
  padding: 16px 20px 40px;
}
.panel {
  max-width: 1100px;
  margin: 0 auto;
  border: 1px solid #e6ebf0;
  border-radius: 8px;
  background: #fff;
  padding: 16px 18px 20px;
  box-shadow: 0 8px 24px rgba(20, 40, 60, 0.06);
}
.page-header h1 {
  margin: 8px 0 4px;
  font-size: 22px;
}
.subtitle {
  margin: 0;
  color: #5a6b7d;
  font-size: 13px;
}
.link {
  border: none;
  background: none;
  color: #1f4b7a;
  cursor: pointer;
  padding: 0;
  font: inherit;
}
.pending {
  color: #b36b00;
  font-size: 13px;
  margin: 8px 0 0;
}
.panes {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 16px;
  margin-top: 12px;
}
.pane {
  border: 1px solid #e6ebf0;
  border-radius: 6px;
  padding: 12px;
  background: #fafbfc;
}
.pane h2 {
  margin: 0 0 10px;
  font-size: 15px;
}
ul {
  list-style: none;
  margin: 0 0 12px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 48px;
}
li {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  padding: 6px;
  border-radius: 4px;
  background: #fff;
}
li.active {
  background: #f0f5fa;
  outline: 1px solid #c5d4e4;
}
input {
  flex: 1;
  min-width: 100px;
  padding: 4px 8px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  font: inherit;
}
button {
  padding: 4px 10px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font: inherit;
}
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
button.primary {
  border-color: #1f4b7a;
  color: #1f4b7a;
}
button.danger {
  color: #a33;
}
button.select {
  font-size: 12px;
}
.add-row {
  display: flex;
  gap: 8px;
}
.ok {
  color: #1a7f37;
}
.err {
  color: #a33;
}
@media (max-width: 960px) {
  .panes {
    grid-template-columns: 1fr;
  }
}
</style>
