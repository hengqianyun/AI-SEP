import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from 'vue-router'
import { routes as contractRoutes } from './routes'
import LoginView from '@/features/auth/views/LoginView.vue'
import PortalLayout from '@/layouts/PortalLayout.vue'
import WorkbenchLayout from '@/layouts/WorkbenchLayout.vue'
import { useAuthStore } from '@/features/auth/store/authStore'

function nestUnderLayout(raw: RouteRecordRaw[]): RouteRecordRaw[] {
  return raw.map((r) => {
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
      component: PortalLayout,
      meta: { public: true },
      children: [
        {
          path: '',
          name: 'portal-home',
          component: () => import('@/features/portal/HomePage.vue'),
          meta: { public: true, title: '数据要素流通门户' },
        },
        {
          path: 'workspace',
          name: 'portal-workspace',
          component: () => import('@/features/portal/WorkspaceIntroPage.vue'),
          meta: { public: true, title: '接入端简介' },
        },
        {
          path: 'docs',
          name: 'portal-docs',
          component: () => import('@/features/portal/DocsPage.vue'),
          meta: { public: true, title: '标准接入文档' },
        },
      ],
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
