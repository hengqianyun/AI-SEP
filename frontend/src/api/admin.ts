/**
 * Admin users API — 对齐 contracts/openapi (wsc-contracts@2.3.3；REQ-USER-001 / REQ-USER-002)
 * 响应永不含 password / passwordHash；软删 = PUT deleted: true；启用 = PUT deleted: false；
 * 硬删 = DELETE /admin/users/{userId}（物理删行）。
 * 列表含已停用账号（deleted=true），供管理端「启用」恢复。
 */
import { apiRequest } from './client'
import type { Role } from './auth'

export type AdminUser = {
  userId: string
  username: string
  displayName: string
  role: Role
  enterpriseId: string
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
  enterpriseId?: string
  enterpriseName?: string
}

export type AdminUserUpdate = {
  displayName?: string
  role?: Role
  enterpriseId?: string
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

/** 物理删除用户行（硬删）；不可删除当前登录账号。 */
export function deleteUser(userId: string) {
  return apiRequest<{ deleted?: boolean } | null>(`/admin/users/${userId}`, {
    method: 'DELETE',
  })
}
