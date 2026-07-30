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
  selectedL1Id,
  renameMap,
  l1List,
  l2List,
  load,
  markDirty,
  addL1,
  addL2,
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
    <header class="page-header">
      <button type="button" class="link" @click="router.push('/catalog')">← 返回目录</button>
      <h1>维护行业分类</h1>
      <p v-if="dirty" class="pending" data-testid="dirty-hint">有未保存的名称变更</p>
    </header>

    <div v-if="state === 'loading'">加载中…</div>

    <div v-else class="panes">
      <section class="pane">
        <h2>一级分类</h2>
        <ul>
          <li v-for="c in l1List" :key="c.id" :class="{ active: selectedL1Id === c.id }">
            <button type="button" class="select" @click="selectedL1Id = c.id">选择</button>
            <input
              v-model="renameMap[c.id]"
              @input="markDirty"
            />
            <button type="button" @click="saveRename(c.id, 'L1')">保存</button>
            <button type="button" class="danger" @click="remove(c.id)">删除</button>
          </li>
        </ul>
        <div class="add-row">
          <input v-model="draftL1Name" placeholder="新一级名称" />
          <button type="button" class="primary" @click="addL1">新增一级</button>
        </div>
      </section>

      <section class="pane">
        <h2>二级分类</h2>
        <ul>
          <li v-for="c in l2List" :key="c.id">
            <input v-model="renameMap[c.id]" @input="markDirty" />
            <button type="button" @click="saveRename(c.id, 'L2')">保存</button>
            <button type="button" class="danger" @click="remove(c.id)">删除</button>
          </li>
        </ul>
        <div class="add-row">
          <input v-model="draftL2Name" placeholder="新二级名称" :disabled="!selectedL1Id" />
          <button type="button" class="primary" :disabled="!selectedL1Id" @click="addL2">
            新增二级
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
</template>

<style scoped>
.category-admin {
  padding: 16px 20px 40px;
}
.page-header h1 {
  margin: 8px 0 4px;
  font-size: 22px;
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
}
.panes {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 12px;
}
.pane {
  border: 1px solid #e6ebf0;
  border-radius: 6px;
  padding: 12px;
  background: #fff;
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
}
li {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  align-items: center;
  padding: 6px;
  border-radius: 4px;
}
li.active {
  background: #f0f5fa;
}
input {
  flex: 1;
  min-width: 120px;
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
@media (max-width: 800px) {
  .panes {
    grid-template-columns: 1fr;
  }
}
</style>
