/**
 * Auth API — 对齐 contracts/openapi (wsc-contracts@2.3.3)
 * LoginRequest 无 role；Session 必填 enterpriseId+enterpriseName；POST /auth/session/role 始终 410。
 */
import { apiRequest } from './client'

export type Role = 'ADMIN' | 'PROVIDER' | 'USER'

export type Session = {
  userId: string
  displayName: string
  role: Role
  /** 企业 ID；mine/myCatalog/写 scope 单一真源（REQ-USER-002）；904 落地前可能缺失 */
  enterpriseId?: string
  enterpriseName?: string
}

export type LoginRequest = {
  username: string
  password: string
}

export type SwitchRoleRequest = {
  role: Role
}

export function getSession() {
  return apiRequest<Session>('/auth/session')
}

export function createSession(body: LoginRequest) {
  return apiRequest<Session>('/auth/session', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function deleteSession() {
  return apiRequest<null>('/auth/session', { method: 'DELETE' })
}

/**
 * 会话角色切换 — 契约硬禁用（HTTP 410 / ERR_ROLE_SWITCH_DISABLED）。
 * 调用方应预期 ApiError；无成功路径。
 */
export function switchSessionRole(body: SwitchRoleRequest) {
  return apiRequest<null>('/auth/session/role', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}
