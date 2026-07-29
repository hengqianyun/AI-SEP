import type { Role, Session } from '@/api/auth'
import { ApiError, type ApiEnvelope } from '@/api/client'

const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1'

/**
 * 角色切换端点（§3.2 等价扩展，未写入冻结 OpenAPI client）。
 * 禁止改 frontend/src/api/**，故在 auth feature 内直连。
 */
export async function switchSessionRole(role: Role): Promise<ApiEnvelope<Session>> {
  const res = await fetch(`${baseUrl}/auth/session/role`, {
    method: 'POST',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify({ role }),
  })
  const body = (await res.json()) as ApiEnvelope<Session>
  if (!res.ok || body.code !== '0') {
    throw new ApiError(
      body.message || res.statusText,
      body.code || String(res.status),
      body.correlationId,
      body.data,
    )
  }
  return body
}
