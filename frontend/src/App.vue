<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="app-root">
    <header class="app-header">
      <strong>接入端工作台</strong>
      <span class="muted">WSC scaffold · contracts 1.1.0</span>
    </header>
    <main>
      <RouterView />
    </main>
  </div>
</template>

<style>
:root {
  font-family: "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #1a1a1a;
  background: #f7f7f5;
}
body {
  margin: 0;
}
.app-root {
  min-height: 100vh;
}
.app-header {
  display: flex;
  gap: 12px;
  align-items: baseline;
  padding: 16px 24px;
  border-bottom: 1px solid #e5e5e0;
  background: #fff;
}
.muted {
  color: #666;
  font-size: 13px;
}
main {
  padding: 24px;
}
</style>
