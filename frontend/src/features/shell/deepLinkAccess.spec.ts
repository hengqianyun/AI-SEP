import { describe, expect, it } from 'vitest'
import { isDeepLinkAllowed } from './deepLinkAccess'
import type { Role } from '@/api/auth'

const GATED = [
  '/catalog/maintenance',
  '/my-catalog',
  '/my-products',
  '/admin/users',
] as const

describe('deepLinkAccess §4.1 (TASK-WSC-906)', () => {
  it('ADMIN can enter dual catalog entries, my-products, and users', () => {
    const role: Role = 'ADMIN'
    expect(isDeepLinkAllowed('/catalog', role)).toBe(true)
    expect(isDeepLinkAllowed('/catalog/maintenance', role)).toBe(true)
    expect(isDeepLinkAllowed('/my-catalog', role)).toBe(true)
    expect(isDeepLinkAllowed('/my-products', role)).toBe(true)
    expect(isDeepLinkAllowed('/admin/users', role)).toBe(true)
  })

  it('PROVIDER can enter my-catalog and my-products; maintenance and users denied', () => {
    const role: Role = 'PROVIDER'
    expect(isDeepLinkAllowed('/catalog', role)).toBe(true)
    expect(isDeepLinkAllowed('/my-catalog', role)).toBe(true)
    expect(isDeepLinkAllowed('/my-products', role)).toBe(true)
    expect(isDeepLinkAllowed('/catalog/maintenance', role)).toBe(false)
    expect(isDeepLinkAllowed('/catalog/maintenance/extra', role)).toBe(false)
    expect(isDeepLinkAllowed('/admin/users', role)).toBe(false)
  })

  it('USER: 菜单外深链结构不可达 — maintenance / my-catalog / my-products / admin/users', () => {
    const role: Role = 'USER'
    expect(isDeepLinkAllowed('/catalog', role)).toBe(true)
    expect(isDeepLinkAllowed('/overview', role)).toBe(true)
    for (const path of GATED) {
      expect(isDeepLinkAllowed(path, role), path).toBe(false)
    }
  })
})
