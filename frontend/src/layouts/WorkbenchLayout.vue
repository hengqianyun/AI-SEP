<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/features/auth/store/authStore'
import {
  canMaintainCatalog,
  canSeeMyCatalog,
  canSeeMyProducts,
} from '@/features/auth/composables/useCanWrite'
import { ROUTE_ORDERS } from '@/features/shell/navSurfaces'
import { isDeepLinkAllowed } from '@/features/shell/deepLinkAccess'
import {
  ROUTE_CATALOG_BROWSE,
  ROUTE_MY_PRODUCTS,
  isCatalogGroupActive,
  isNavActive,
} from '@/features/shell/navSurfaces'

const CATALOG_GROUP_OPEN_KEY = 'wsc.nav.catalogGroupOpen'

const auth = useAuthStore()
const { enterpriseName, session, role } = storeToRefs(auth)
const route = useRoute()
const router = useRouter()

const catalogMaintenanceVisible = computed(() => canMaintainCatalog(role.value))
/** 602：用户管理仅 ADMIN（源码字面量保留，供既有 spec 扫描） */
const userManageVisible = computed(() => role.value === 'ADMIN')
/** 603：我的数据产品 — ADMIN + PROVIDER */
const myProductsVisible = computed(() => canSeeMyProducts(role.value))
const myCatalogVisible = computed(() => canSeeMyCatalog(role.value))

/** 一级「数据目录」展开态，sessionStorage 会话级持久（REQ-SHELL-001 / HOTFIX-SHELL-002） */
function readCatalogGroupOpen(): boolean {
  try {
    const stored = sessionStorage.getItem(CATALOG_GROUP_OPEN_KEY)
    if (stored === '0') return false
    if (stored === '1') return true
  } catch {
    /* ignore */
  }
  return true
}

const catalogGroupOpen = ref(readCatalogGroupOpen())

function persistCatalogGroupOpen(open: boolean) {
  try {
    sessionStorage.setItem(CATALOG_GROUP_OPEN_KEY, open ? '1' : '0')
  } catch {
    /* ignore */
  }
}

function toggleCatalogGroup() {
  catalogGroupOpen.value = !catalogGroupOpen.value
  persistCatalogGroupOpen(catalogGroupOpen.value)
}

const navOpen = [
  { to: '/overview', label: '总览', icon: 'overview' },
] as const

const navClosed = [
  { feature: 'registration', label: '数据登记', icon: 'registration' },
  { feature: 'connectors', label: '连接器管理', icon: 'connectors' },
] as const

const activePath = computed(() => route.path)

/** 双表面：写路径带 from=mine 或 meta.fromMine 时高亮「我的数据产品」（UX-002） */
const onMineSurface = computed(
  () =>
    isNavActive(ROUTE_MY_PRODUCTS, activePath.value, route.query.from, route.meta.fromMine === true),
)

const catalogGroupActive = computed(() =>
  isCatalogGroupActive(activePath.value, route.query.from, route.meta.fromMine === true),
)

function isActive(path: string) {
  if (path === ROUTE_MY_PRODUCTS) return onMineSurface.value
  return isNavActive(path, activePath.value, route.query.from, route.meta.fromMine === true)
}

/**
 * REQ-SHELL-009：无权限深链结构不可达（USER 维护/我的目录/我的产品/用户管理；PROVIDER 无目录维护）。
 * 页面 onMounted 可再做双保险；907 可扩 /my-catalog 页级 guard，不得删除 ADMIN 双入口。
 */
watch(
  [() => route.path, role],
  () => {
    if (isDeepLinkAllowed(route.path, role.value)) return
    void router.replace(ROUTE_CATALOG_BROWSE)
  },
  { immediate: true },
)

watch(
  () => route.meta.title,
  (title) => {
    if (typeof document === 'undefined') return
    if (typeof title === 'string' && title.trim()) {
      document.title = title
    }
  },
  { immediate: true },
)

function goUnavailable(feature: string) {
  void router.push(`/unavailable/${feature}`)
}

async function onLogout() {
  await auth.logout()
  await router.replace({ name: 'login' })
}

const roleLabel = computed(() => {
  switch (role.value) {
    case 'ADMIN':
      return '管理员'
    case 'PROVIDER':
      return '数据提供方'
    case 'USER':
      return '普通用户'
    default:
      return '—'
  }
})
</script>

<template>
  <div class="workbench">
    <aside class="sidebar" data-testid="workbench-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
        <div class="sidebar-brand">
          <p class="brand-eyebrow">WORKSPACE CONSOLE</p>
          <h1 class="brand-title">接入端工作台</h1>
        </div>
      </div>

      <nav class="sidebar-menu" aria-label="主导航">
        <RouterLink
          v-for="item in navOpen"
          :key="item.to"
          :to="item.to"
          class="menu-item"
          :class="{ active: isActive(item.to) }"
        >
          <svg v-if="item.icon === 'overview'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <rect x="3" y="3" width="7" height="7" />
            <rect x="14" y="3" width="7" height="7" />
            <rect x="14" y="14" width="7" height="7" />
            <rect x="3" y="14" width="7" height="7" />
          </svg>
          <span>{{ item.label }}</span>
        </RouterLink>

        <!-- REQ-SHELL-009：一级「数据目录」可收起/展开 -->
        <div
          class="menu-group"
          data-testid="nav-catalog-group"
          :class="{ active: catalogGroupActive }"
        >
          <button
            type="button"
            class="menu-group-label"
            :class="{ active: catalogGroupActive }"
            :aria-expanded="catalogGroupOpen"
            aria-controls="nav-catalog-submenu"
            data-testid="nav-catalog-group-toggle"
            @click="toggleCatalogGroup"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M3 6h18" />
              <path d="M7 12h10" />
              <path d="M10 18h4" />
            </svg>
            <span class="menu-group-title">数据目录</span>
            <svg
              class="menu-group-chevron"
              :class="{ open: catalogGroupOpen }"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </button>

          <div
            v-show="catalogGroupOpen"
            id="nav-catalog-submenu"
            class="menu-sub"
            role="group"
            aria-label="数据目录子菜单"
          >
            <RouterLink
              to="/catalog"
              class="menu-item menu-item--sub"
              :class="{ active: isActive('/catalog') }"
              data-testid="nav-catalog-browse"
            >
              <span>全链数据目录</span>
            </RouterLink>

            <RouterLink
              v-if="myCatalogVisible"
              to="/my-catalog"
              class="menu-item menu-item--sub"
              :class="{ active: isActive('/my-catalog') }"
              data-testid="nav-my-catalog"
            >
              <span>我的目录</span>
            </RouterLink>

            <RouterLink
              v-if="myProductsVisible"
              to="/my-products"
              class="menu-item menu-item--sub"
              :class="{ active: isActive('/my-products') }"
              data-testid="nav-my-products"
            >
              <span>我的数据产品</span>
            </RouterLink>

            <RouterLink
              v-if="catalogMaintenanceVisible"
              to="/catalog/maintenance"
              class="menu-item menu-item--sub"
              :class="{ active: isActive('/catalog/maintenance') }"
              data-testid="nav-catalog-maintenance"
            >
              <span>目录维护</span>
            </RouterLink>
          </div>
        </div>

        <RouterLink
          :to="ROUTE_ORDERS"
          class="menu-item"
          :class="{ active: isActive(ROUTE_ORDERS) }"
          data-testid="nav-orders"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
          </svg>
          <span>交易订单</span>
        </RouterLink>

        <RouterLink
          v-if="userManageVisible"
          to="/admin/users"
          class="menu-item"
          :class="{ active: isActive('/admin/users') }"
          data-testid="nav-users"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
          <span>用户管理</span>
        </RouterLink>

        <button
          v-for="item in navClosed"
          :key="item.feature"
          type="button"
          class="menu-item closed"
          @click="goUnavailable(item.feature)"
        >
          <svg v-if="item.icon === 'registration'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="12" y1="18" x2="12" y2="12" />
            <line x1="9" y1="15" x2="15" y2="15" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2" />
            <line x1="8" y1="21" x2="16" y2="21" />
            <line x1="12" y1="17" x2="12" y2="21" />
          </svg>
          <span class="menu-label">{{ item.label }}</span>
          <span class="badge">未开放</span>
        </button>
      </nav>

      <div class="sidebar-footer">
        <div
          class="company-card"
          data-testid="sidebar-enterprise"
          data-enterprise-readonly="true"
        >
          <h3 data-testid="sidebar-enterprise-name">{{ enterpriseName || '—' }}</h3>
          <p
            v-if="session?.enterpriseId"
            class="enterprise-id"
            data-testid="sidebar-enterprise-id"
          >{{ session.enterpriseId }}</p>
          <p v-if="session" class="user-line">{{ session.displayName }}</p>
          <p class="role-line" data-testid="session-role-label">角色：{{ roleLabel }}</p>
          <button type="button" class="logout" @click="onLogout">退出登录</button>
        </div>
      </div>
    </aside>

    <div class="main">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.workbench {
  display: flex;
  min-height: 100vh;
  background: var(--main-bg);
}

.sidebar {
  width: var(--sidebar-width);
  background: var(--sidebar-bg);
  color: #fff;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-header {
  padding: 24px 20px 28px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.sidebar-logo {
  width: 38px;
  height: 38px;
  background: var(--sidebar-logo-bg);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar-logo svg {
  width: 22px;
  height: 22px;
  color: var(--sidebar-accent);
}

.sidebar-brand {
  min-width: 0;
}

.brand-eyebrow {
  margin: 0 0 2px;
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.12em;
  color: var(--sidebar-text-muted);
}

.brand-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  line-height: 1.25;
}

.sidebar-menu {
  flex: 1;
  padding: 0 12px;
  overflow-y: auto; scrollbar-width: none; -ms-overflow-style: none; } .sidebar-menu::-webkit-scrollbar { display: none; }

.menu-group {
  margin-bottom: 4px;
}

.menu-group-label {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  border-radius: var(--radius-menu);
  color: var(--sidebar-text);
  font-size: var(--font-size-base);
  font-weight: 500;
  border: 0;
  background: transparent;
  cursor: pointer;
  font: inherit;
  text-align: left;
  transition: background 0.2s ease, color 0.2s ease;
}

.menu-group-label:hover {
  background: var(--sidebar-hover-bg);
  color: var(--sidebar-text-hover);
}

.menu-group-label:focus-visible {
  outline: 2px solid var(--sidebar-accent, #4da3ff);
  outline-offset: 2px;
}

.menu-group-label svg:first-of-type {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.menu-group-title {
  flex: 1;
  min-width: 0;
}

.menu-group-chevron {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  opacity: 0.75;
  transition: transform 0.2s ease;
  transform: rotate(-90deg);
}

.menu-group-chevron.open {
  transform: rotate(0deg);
}

.menu-group-label.active {
  color: #fff;
}

.menu-sub {
  display: flex;
  flex-direction: column;
  padding: 0 0 4px 14px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 12px 14px;
  margin-bottom: 4px;
  border-radius: var(--radius-menu);
  color: var(--sidebar-text);
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: var(--font-size-base);
  text-decoration: none;
  border: 0;
  background: transparent;
  text-align: left;
  font: inherit;
}

.menu-item--sub {
  padding: 10px 14px 10px 28px;
  font-size: 13px;
  margin-bottom: 2px;
}

.menu-item svg {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
}

.menu-item:hover {
  background: var(--sidebar-hover-bg);
  color: var(--sidebar-text-hover);
}

.menu-item.active {
  background: var(--sidebar-active-bg);
  color: #fff;
}

.menu-item.closed {
  justify-content: flex-start;
}

.menu-label {
  flex: 1;
  min-width: 0;
}

.badge {
  margin-left: auto;
  font-size: 11px;
  color: var(--orange);
  opacity: 0.95;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid var(--sidebar-border);
  margin: 0 12px 12px;
}

.company-card {
  background: var(--sidebar-card-bg);
  border-radius: var(--radius-menu);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.company-card h3 {
  margin: 0;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
}

.enterprise-id {
  margin: 0;
  font-size: 12px;
  color: var(--sidebar-text-muted);
}

.user-line {
  margin: 0;
  font-size: 12px;
  color: var(--sidebar-text-muted);
}

.role-line {
  margin: 0;
  font-size: 12px;
  color: var(--sidebar-text-muted);
}

.role {
  margin-top: 2px;
}

.logout {
  margin-top: 4px;
  padding: 6px 8px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 4px;
  background: transparent;
  color: var(--sidebar-text-muted);
  font: inherit;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s, border-color 0.2s;
}

.logout:hover {
  color: rgba(255, 255, 255, 0.85);
  border-color: rgba(255, 255, 255, 0.35);
}

.main {
  flex: 1;
  margin-left: var(--sidebar-width);
  min-height: 100vh;
  padding: var(--main-padding);
  min-width: 0;
  box-sizing: border-box;
}

@media (max-width: 900px) {
  .sidebar {
    position: relative;
    width: var(--sidebar-width);
    min-height: auto;
  }

  .workbench {
    flex-direction: column;
  }

  .main {
    margin-left: 0;
  }
}
</style>
