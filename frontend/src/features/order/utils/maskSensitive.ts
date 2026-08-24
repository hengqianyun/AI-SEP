/**
 * 订单脱敏工具 — 按角色对敏感字段做截断展示（§2.2 OQ-V17-001 假设）。
 *
 * ADMIN：完整可读。
 * PROVIDER：需求方姓名/单位可见，信用代码/连接器/合约截断。
 * USER：仅本人信息明文，提供方信用代码等截断。
 *
 * 截断策略对齐目录 SensitiveId：保留前 N 后 M，中间用省略号。
 * 完整值仍可通过复制获得（由 SensitiveId 组件支持）。
 */

import type { Role } from '@/api/auth'

/**
 * 截断敏感标识（对齐目录 truncateSensitiveId）。
 * 值长度 <= head + tail + 1 时不截断。
 */
export function truncateSensitiveId(value: string | null | undefined, head = 10, tail = 6): string {
  if (value == null) return '—'
  if (value.length <= head + tail + 1) {
    return value
  }
  return `${value.slice(0, head)}…${value.slice(-tail)}`
}

/**
 * 对字符串值按角色脱敏。
 *
 * @param value 原始值
 * @param role 当前角色
 * @param sensitiveRoles 需要脱敏的角色列表（默认 PROVIDER + USER）
 * @returns 脱敏后的值
 */
export function maskByRole(
  value: string | null | undefined,
  role: Role | null | undefined,
  sensitiveRoles: Role[] = ['PROVIDER', 'USER'],
): string {
  if (!value) return '—'
  if (role === 'ADMIN') return value
  if (role && sensitiveRoles.includes(role)) return truncateSensitiveId(value)
  return value
}

/**
 * 订单列表/详情脱敏字段映射。
 *
 * @param fields 需要脱敏的字段对象
 * @param role 当前角色
 * @returns 脱敏后的字段对象
 */
export function maskOrderFields<T extends Record<string, string | null | undefined>>(
  fields: T,
  role: Role | null | undefined,
): T {
  if (role === 'ADMIN') return fields
  const masked = { ...fields } as Record<string, string | null | undefined>
  for (const key of Object.keys(masked)) {
    if (masked[key] != null && masked[key] !== '') {
      masked[key] = truncateSensitiveId(masked[key])
    }
  }
  return masked as T
}
