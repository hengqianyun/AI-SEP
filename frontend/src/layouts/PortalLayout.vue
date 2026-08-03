<script setup lang="ts">
import { RouterView } from 'vue-router'
import PortalNav from '@/features/portal/components/PortalNav.vue'
import '@/features/portal/styles/portal.css'
</script>

<template>
  <div class="portal-shell" data-testid="portal-shell">
    <PortalNav />
    <div class="portal-container">
      <main class="portal-page">
        <RouterView />
      </main>
      <footer class="portal-footer">
        <div class="portal-footer-inner">
          <p>可信数据流通链门户 POC 当前承载门户介绍、标准接入说明与接入端入口能力。</p>
          <p>面向生态伙伴的方案发布、接入引导与业务验证入口。</p>
        </div>
      </footer>
    </div>
  </div>
</template>
