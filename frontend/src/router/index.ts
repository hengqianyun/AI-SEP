import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from 'vue-router'
import { routes as contractRoutes } from './routes'
import LoginView from '@/features/auth/views/LoginView.vue'
import WorkbenchLayout from '@/layouts/WorkbenchLayout.vue'
import { useAuthStore } from '@/features/auth/store/authStore'

/**
 * 在不修改 routes.ts（001 冻结清单）的前提下：
 * - 增加 /login
 * - 将契约路由挂到 WorkbenchLayout 下
 * - 挂载登录守卫
 */
function nestUnderLayout(raw: RouteRecordRaw[]): RouteRecordRaw[] {
  return raw.map((r) => {
    if (r.redirect != null && (r.path === '/' || r.path === '')) {
      return { path: '', redirect: r.redirect }
    }
    const path =
      typeof r.path === 'string' && r.path.startsWith('/') ? r.path.slice(1) : r.path
    return { ...r, path }
  })
}

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true, title: '登录' },
    },
    {
      path: '/',
      component: WorkbenchLayout,
      meta: { requiresAuth: true },
      children: nestUnderLayout(contractRoutes),
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (!auth.session) {
    await auth.fetchSession()
  }
  if (to.meta.public) {
    if (to.name === 'login' && auth.session) {
      return { path: '/overview' }
    }
    return true
  }
  if (!auth.session) {
    return {
      name: 'login',
      query: to.fullPath !== '/' ? { redirect: to.fullPath } : undefined,
    }
  }
  return true
})

export default router
