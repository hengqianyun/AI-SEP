import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { ApiError } from '@/api/client'

vi.mock('@/api/auth', () => ({
  getSession: vi.fn(),
  createSession: vi.fn(),
  deleteSession: vi.fn(),
  switchSessionRole: vi.fn(),
}))

import {
  createSession,
  deleteSession,
  getSession,
  switchSessionRole,
} from '@/api/auth'
import { useAuthStore } from './authStore'

describe('authStore (TASK-WSC-602)', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('login success binds session role from account (no client role)', async () => {
    vi.mocked(createSession).mockResolvedValue({
      code: '0',
      message: 'ok',
      correlationId: 'c1',
      data: {
        userId: '1',
        displayName: '演示管理员',
        role: 'ADMIN',
        enterpriseName: '演示企业',
      },
    })
    const store = useAuthStore()
    await store.login('admin', 'demo')
    expect(createSession).toHaveBeenCalledWith({ username: 'admin', password: 'demo' })
    expect(store.role).toBe('ADMIN')
    expect(store.isAuthenticated).toBe(true)
  })

  it('login failure clears session', async () => {
    vi.mocked(createSession).mockRejectedValue(new Error('未授权'))
    const store = useAuthStore()
    await expect(store.login('admin', 'wrong')).rejects.toThrow()
    expect(store.session).toBeNull()
    expect(store.error).toBeTruthy()
  })

  it('switchRole always fails with ERR_ROLE_SWITCH_DISABLED', async () => {
    vi.mocked(switchSessionRole).mockRejectedValue(
      new ApiError('角色由账号绑定，禁止会话切换', 'ERR_ROLE_SWITCH_DISABLED', 'c2'),
    )
    const store = useAuthStore()
    store.session = {
      userId: '1',
      displayName: '演示管理员',
      role: 'ADMIN',
      enterpriseName: '演示企业',
    }
    await expect(store.switchRole('USER')).rejects.toMatchObject({
      code: 'ERR_ROLE_SWITCH_DISABLED',
    })
    expect(store.role).toBe('ADMIN')
  })

  it('logout clears local session', async () => {
    vi.mocked(deleteSession).mockResolvedValue({
      code: '0',
      message: 'ok',
      correlationId: 'c3',
      data: null,
    })
    const store = useAuthStore()
    store.session = {
      userId: '1',
      displayName: 'x',
      role: 'USER',
    }
    await store.logout()
    expect(store.session).toBeNull()
  })

  it('fetchSession hydrates or clears', async () => {
    vi.mocked(getSession).mockResolvedValue({
      code: '0',
      message: 'ok',
      correlationId: 'c4',
      data: {
        userId: '2',
        displayName: '提供方',
        role: 'PROVIDER',
        enterpriseName: '演示企业',
      },
    })
    const store = useAuthStore()
    await store.fetchSession()
    expect(store.role).toBe('PROVIDER')
  })
})
