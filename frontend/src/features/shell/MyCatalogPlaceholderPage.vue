<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

/**
 * `/my-catalog` 壳层占位（TASK-WSC-906）。
 * 不实现目录维护业务；907 独占 CatalogMaintenancePage / useCatalogMaintenance。
 */
const route = useRoute()
const title = computed(
  () => (typeof route.meta.title === 'string' && route.meta.title) || '我的目录（本企业/本人）',
)
</script>

<template>
  <div class="my-catalog-placeholder" data-testid="my-catalog-placeholder">
    <header class="page-header wsc-surface">
      <h1 data-testid="workbench-page-title">{{ title }}</h1>
      <p class="subtitle">本企业 / 本人维护入口。目录维护 UX 由后续任务接入，本页不实现业务。</p>
    </header>
  </div>
</template>

<style scoped>
.page-header {
  padding: 20px 24px;
}

h1 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
}
</style>
