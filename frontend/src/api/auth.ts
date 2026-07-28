/**
 * Auth API — 对齐 contracts/openapi (wsc-contracts@1.1.0)
 * 由契约生成/手写对齐；禁止在后续业务任务中手改契约语义。
 */
import { apiRequest } from './client'

export type Role = 'ADMIN' | 'PROVIDER' | 'USER'

export type Session = {
  userId: string
  displayName: string
  role: Role
  enterpriseName?: string
}

export function getSession() {
  return apiRequest<Session>('/auth/session')
}

export function createSession(body: { username: string; password: string; role?: Role }) {
  return apiRequest<Session>('/auth/session', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function deleteSession() {
  return apiRequest<null>('/auth/session', { method: 'DELETE' })
}
