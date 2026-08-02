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
  <section class="write-demo wsc-card" aria-label="写入口演示">
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
  padding: 16px 18px;
}

h2 {
  margin: 0 0 4px;
  font-size: var(--font-size-base);
  color: var(--text-primary);
  font-weight: 600;
}

.muted {
  margin: 0 0 12px;
  color: var(--text-secondary);
  font-size: 12px;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

button {
  padding: 7px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--card-bg);
  color: var(--text-primary);
  font: inherit;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}

button.primary {
  border-color: #bfdbfe;
  background: var(--blue-light);
  color: var(--blue);
  font-weight: 500;
}

button.primary:hover {
  background: #dbeafe;
}

.empty {
  margin: 0;
  color: var(--text-tertiary);
  font-size: 13px;
}
</style>
