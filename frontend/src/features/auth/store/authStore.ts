import type { Role, Session } from '@/api/auth'
import {
  createSession,
  deleteSession,
  getSession,
  switchSessionRole,
} from '@/api/auth'
import { ApiError } from '@/api/client'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

/** Pinia auth store — REQ-RBAC-001 / REQ-SHELL-001（V1.4 角色绑定账号） */
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

  async function login(username: string, password: string) {
    loading.value = true
    error.value = null
    try {
      const res = await createSession({
        username,
        password,
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

  /**
   * 会话切角色已下线：调用契约端点，预期 410 + ERR_ROLE_SWITCH_DISABLED。
   * 无成功路径；会话角色不变。
   */
  async function switchRole(next: Role) {
    try {
      await switchSessionRole({ role: next })
      throw new Error('角色切换不应成功')
    } catch (e) {
      if (e instanceof ApiError && e.code === 'ERR_ROLE_SWITCH_DISABLED') {
        error.value = e.message
        throw e
      }
      throw e
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
