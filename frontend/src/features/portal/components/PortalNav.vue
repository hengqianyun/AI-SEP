<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { portalNavigation } from '../navigation'

const route = useRoute()

const items = portalNavigation

const activePath = computed(() => route.path)

function isActive(path: string) {
  if (path === '/') return activePath.value === '/'
  return activePath.value === path || activePath.value.startsWith(path + '/')
}
</script>

<template>
  <header class="portal-nav" data-testid="portal-nav">
    <div class="portal-container">
      <nav class="portal-nav-inner" aria-label="门户导航">
        <RouterLink class="portal-brand" to="/">
          <span class="portal-brand-mark" aria-hidden="true">◈</span>
          <span>
            <span class="portal-brand-title">可信数据流通链</span>
            <span class="portal-brand-sub">官方门户 / 标准接入 / 接入端入口</span>
          </span>
        </RouterLink>
        <div class="portal-nav-links">
          <RouterLink
            v-for="item in items"
            :key="item.path"
            :to="item.path"
            class="portal-nav-link"
            :class="{ 'portal-nav-link-active': isActive(item.path) }"
          >
            <span class="portal-nav-link-label">{{ item.label }}</span>
            <span class="portal-nav-link-desc">{{ item.description }}</span>
          </RouterLink>
        </div>
      </nav>
    </div>
  </header>
</template>
