/**
 * HTTP client 基座 — 对齐 wsc-contracts@2.3.3
 * 由契约 OpenAPI 对齐/生成；TASK-WSC-903 唯一写；后续任务只读消费。
 */

const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1'

export const CONTRACT_VERSION = '2.3.3' as const

export type ApiEnvelope<T> = {
  code: string
  message: string
  data: T
  correlationId: string
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly code: string,
    readonly correlationId?: string,
    readonly data?: unknown,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<ApiEnvelope<T>> {
  const headers = new Headers(init.headers)
  if (!headers.has('Accept')) {
    headers.set('Accept', 'application/json')
  }
  if (init.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const res = await fetch(`${baseUrl}${path}`, {
    ...init,
    headers,
    credentials: 'include',
  })

  const body = (await res.json()) as ApiEnvelope<T>
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

export function getApiBaseUrl(): string {
  return baseUrl
}
