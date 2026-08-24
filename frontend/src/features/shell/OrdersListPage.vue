<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '交易订单')
</script>

<template>
  <section class="wsc-card">
    <h1 data-testid="workbench-page-title">{{ title }}</h1>
    <p class="hint">订单列表功能开发中，敬请期待。</p>
  </section>
</template>

<style scoped>
.wsc-card {
  padding: 24px 28px;
  max-width: 720px;
}

h1 {
  margin: 0 0 8px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.hint {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
}
</style>
