import type { NavigationGuard } from 'vue-router'
import { useAuthStore } from '@/features/auth/store/authStore'
import { isDeepLinkAllowed } from './deepLinkAccess'

const FALLBACK = '/catalog'

/** routes.ts beforeEnter：无权限深链回全链目录（router/index.ts 不在本任务 writeSet） */
export const requireDeepLinkAccess: NavigationGuard = (to) => {
  const auth = useAuthStore()
  if (isDeepLinkAllowed(to.path, auth.role)) return true
  return { path: FALLBACK }
}
