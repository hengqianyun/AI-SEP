import type { Role, Session } from '@/api/auth'
import { createSession, deleteSession, getSession } from '@/api/auth'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { switchSessionRole } from '../api/switchRole'

/** Pinia auth store — REQ-RBAC-001 / REQ-SHELL-001 */
export const useAuthStore = defineStore('auth', () => {
  const session = ref<Session | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  const role = computed(() => session.value?.role ?? null)
  const enterpriseName = computed(() => session.value?.enterpriseName ?? '')
  const isAuthenticated = computed(() => session.value != null)

  async function fetchSession() {
    loading.value = true
    error.value = null
    try {
      const res = await getSession()
      session.value = res.data
      return session.value
    } catch {
      session.value = null
      return null
    } finally {
      loading.value = false
    }
  }

  async function login(username: string, password: string, roleHint?: Role) {
    loading.value = true
    error.value = null
    try {
      const res = await createSession({
        username,
        password,
        ...(roleHint ? { role: roleHint } : {}),
      })
      session.value = res.data
      return session.value
    } catch (e) {
      session.value = null
      error.value = e instanceof Error ? e.message : '登录失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    loading.value = true
    error.value = null
    try {
      await deleteSession()
    } catch {
      // 本地仍清理会话
    } finally {
      session.value = null
      loading.value = false
    }
  }

  /** 切换角色：旧会话服务端失效，写入口立即按新角色更新。 */
  async function switchRole(next: Role) {
    loading.value = true
    error.value = null
    try {
      const res = await switchSessionRole(next)
      session.value = res.data
      return session.value
    } catch (e) {
      error.value = e instanceof Error ? e.message : '角色切换失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  return {
    session,
    loading,
    error,
    role,
    enterpriseName,
    isAuthenticated,
    fetchSession,
    login,
    logout,
    switchRole,
  }
})
