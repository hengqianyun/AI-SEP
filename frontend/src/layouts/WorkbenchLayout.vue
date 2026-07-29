<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import RoleSwitcher from '@/features/shell/components/RoleSwitcher.vue'
import WriteEntryDemo from '@/features/shell/components/WriteEntryDemo.vue'

const auth = useAuthStore()
const { enterpriseName, session } = storeToRefs(auth)
const route = useRoute()
const router = useRouter()

const navOpen = [
  { to: '/overview', label: '总览' },
  { to: '/catalog', label: '数据目录' },
] as const

const navClosed = [
  { feature: 'registration', label: '数据登记' },
  { feature: 'orders', label: '交易订单' },
  { feature: 'connectors', label: '连接器管理' },
] as const

const activePath = computed(() => route.path)

function isActive(path: string) {
  return activePath.value === path || activePath.value.startsWith(path + '/')
}

function goUnavailable(feature: string) {
  void router.push(`/unavailable/${feature}`)
}

async function onLogout() {
  await auth.logout()
  await router.replace({ name: 'login' })
}
</script>

<template>
  <div class="workbench">
    <aside class="sidebar">
      <div class="brand">
        <strong>接入端工作台</strong>
        <span class="brand-sub">WSC</span>
      </div>

      <nav class="nav">
        <RouterLink
          v-for="item in navOpen"
          :key="item.to"
          :to="item.to"
          class="nav-item"
          :class="{ active: isActive(item.to) }"
        >
          {{ item.label }}
        </RouterLink>
        <button
          v-for="item in navClosed"
          :key="item.feature"
          type="button"
          class="nav-item closed"
          @click="goUnavailable(item.feature)"
        >
          {{ item.label }}
          <span class="badge">未开放</span>
        </button>
      </nav>

      <div class="enterprise">
        <div class="label">当前企业</div>
        <div class="name">{{ enterpriseName || '—' }}</div>
        <div v-if="session" class="user">{{ session.displayName }}</div>
        <RoleSwitcher class="role" />
        <button type="button" class="logout" @click="onLogout">退出登录</button>
      </div>
    </aside>

    <div class="main">
      <RouterView />
      <WriteEntryDemo />
    </div>
  </div>
</template>

<style scoped>
.workbench {
  display: grid;
  grid-template-columns: 240px 1fr;
  min-height: 100vh;
  background: #f7f7f5;
}
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 16px;
  background: #1b2a3a;
  color: #e8eef5;
}
.brand {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.brand strong {
  font-size: 16px;
}
.brand-sub {
  font-size: 12px;
  opacity: 0.7;
}
.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}
.nav-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 4px;
  color: inherit;
  text-decoration: none;
  font-size: 14px;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
  font: inherit;
}
.nav-item:hover,
.nav-item.active {
  background: rgba(255, 255, 255, 0.1);
}
.nav-item.closed {
  opacity: 0.85;
}
.badge {
  font-size: 11px;
  color: #f0c674;
}
.enterprise {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  font-size: 13px;
}
.enterprise .label {
  opacity: 0.65;
  font-size: 12px;
}
.enterprise .name {
  font-weight: 600;
}
.enterprise .user {
  opacity: 0.85;
}
.role {
  margin-top: 4px;
}
.role :deep(span),
.role :deep(select) {
  color: #1a1a1a;
}
.logout {
  margin-top: 8px;
  padding: 6px 8px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 4px;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}
.main {
  padding: 24px;
}
@media (max-width: 800px) {
  .workbench {
    grid-template-columns: 1fr;
  }
  .sidebar {
    min-height: auto;
  }
}
</style>
