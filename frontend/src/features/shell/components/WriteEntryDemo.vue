<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/features/auth/store/authStore'
import { useCanWrite } from '@/features/auth/composables/useCanWrite'

const router = useRouter()
const auth = useAuthStore()
const { role } = storeToRefs(auth)
const {
  categoryMaintainVisible,
  catalogMaintenanceVisible,
  productWriteVisible,
  productImportVisible,
} = useCanWrite(role)
</script>

<template>
  <section class="write-demo" aria-label="写入口演示">
    <h2>写入口（按角色）</h2>
    <p class="muted">壳层入口 · 角色变更后立即更新可见性</p>
    <div class="actions">
      <button
        v-if="categoryMaintainVisible"
        type="button"
        class="primary"
        @click="router.push('/catalog/admin/categories')"
      >
        维护行业分类
      </button>
      <button
        v-if="catalogMaintenanceVisible"
        type="button"
        class="primary"
        @click="router.push('/catalog/maintenance')"
      >
        目录维护
      </button>
      <button
        v-if="productWriteVisible"
        type="button"
        class="primary"
        @click="router.push('/catalog/products/new')"
      >
        新增产品
      </button>
      <button
        v-if="productImportVisible"
        type="button"
        class="primary"
        @click="router.push({ path: '/catalog', query: { import: '1' } })"
      >
        批量导入
      </button>
      <p
        v-if="
          !categoryMaintainVisible &&
          !catalogMaintenanceVisible &&
          !productWriteVisible &&
          !productImportVisible
        "
        class="empty"
      >
        当前角色无目录写入口
      </p>
    </div>
  </section>
</template>

<style scoped>
.write-demo {
  margin-top: 16px;
  padding: 12px 14px;
  border: 1px dashed #cfd6df;
  border-radius: 6px;
  background: #fafbfc;
}
h2 {
  margin: 0 0 4px;
  font-size: 14px;
}
.muted {
  margin: 0 0 10px;
  color: #777;
  font-size: 12px;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
button {
  padding: 6px 10px;
  border: 1px solid #cfd6df;
  border-radius: 4px;
  background: #fff;
  font: inherit;
  cursor: pointer;
}
button.primary {
  border-color: #1f4b7a;
  color: #1f4b7a;
}
.empty {
  margin: 0;
  color: #888;
  font-size: 13px;
}
</style>
