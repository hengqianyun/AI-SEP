/**
 * Admin users API — 对齐 contracts/openapi (wsc-contracts@2.2.0；REQ-USER-001)
 * 响应永不含 password / passwordHash；软删 = PUT deleted: true。
 */
import { apiRequest } from './client'
import type { Role } from './auth'

export type AdminUser = {
  userId: string
  username: string
  displayName: string
  role: Role
  enterpriseName: string
  deleted: boolean
  createdAt?: string
  updatedAt?: string
}

export type AdminUserCreate = {
  username: string
  password: string
  role: Role
  displayName?: string
  enterpriseName?: string
}

export type AdminUserUpdate = {
  displayName?: string
  role?: Role
  enterpriseName?: string
  password?: string
  /** 软删权威形状 */
  deleted?: boolean
}

export type AdminUserPage = {
  items: AdminUser[]
}

export function listUsers() {
  return apiRequest<AdminUserPage>('/admin/users')
}

export function createUser(body: AdminUserCreate) {
  return apiRequest<AdminUser>('/admin/users', {
    method: 'POST',
    body: JSON.stringify(body),
  })
}

export function updateUser(userId: string, body: AdminUserUpdate) {
  return apiRequest<AdminUser>(`/admin/users/${userId}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
}
