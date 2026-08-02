<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '占位页')
const hint = computed(() => (route.meta.hint as string) || '本页为脚手架占位，业务实现见后续任务。')
</script>

<template>
  <section class="placeholder wsc-card">
    <h1>{{ title }}</h1>
    <p class="hint">{{ hint }}</p>
    <p v-if="route.meta.unavailable" class="notice">本版本未开放</p>
  </section>
</template>

<style scoped>
.placeholder {
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
  margin: 0 0 8px;
  color: var(--text-secondary);
  font-size: var(--font-size-base);
}

.notice {
  margin: 12px 0 0;
  display: inline-block;
  padding: 6px 10px;
  border-radius: 6px;
  background: var(--orange-light);
  color: #b45309;
  font-weight: 600;
  font-size: 13px;
  border: 1px solid #fcd34d;
}
</style>
