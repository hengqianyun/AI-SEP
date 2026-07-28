<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '占位页')
const hint = computed(() => (route.meta.hint as string) || '本页为脚手架占位，业务实现见后续任务。')
</script>

<template>
  <section>
    <h1>{{ title }}</h1>
    <p>{{ hint }}</p>
    <p v-if="route.meta.unavailable" class="notice">本版本未开放</p>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 8px;
  font-size: 22px;
}
p {
  margin: 0 0 8px;
  color: #444;
}
.notice {
  color: #a15c00;
  font-weight: 600;
}
</style>
