import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'

const dir = dirname(fileURLToPath(import.meta.url))
const src = readFileSync(join(dir, 'UsersAdminPage.vue'), 'utf8')
const layoutSrc = readFileSync(join(dir, '../../layouts/WorkbenchLayout.vue'), 'utf8')
const routesSrc = readFileSync(join(dir, '../../router/routes.ts'), 'utf8')

describe('UsersAdminPage (TASK-WSC-602 / TASK-WSC-606)', () => {
  it('uses token hierarchy + loading/empty/error surfaces + wsc-surface card', () => {
    expect(src).toContain('page-header')
    expect(src).toContain('wsc-surface')
    expect(src).toContain('background: var(--card-bg)')
    expect(src).toContain('var(--text-primary)')
    expect(src).toContain('var(--blue)')
    expect(src).toContain('data-testid="users-loading"')
    expect(src).toContain('data-testid="users-empty"')
    expect(src).toContain('data-testid="users-error"')
    expect(src).toContain('data-testid="users-list-card"')
    expect(src).toContain("role.value === 'ADMIN'")
    expect(src).not.toMatch(/import\s*\{[^}]*canManageUsers/)
    expect(src).not.toMatch(/from\s+['"]@\/features\/auth\/composables\/useCanWrite['"]/)
    expect(src).toContain('data-testid="users-forbidden"')
  })

  it('606: create via dialog; edit-role button; no sys_user subtitle; no inline-only role select', () => {
    expect(src).not.toContain('创建账号并配置角色（持久化 sys_user）')
    expect(src).toContain('data-testid="user-open-create"')
    expect(src).toContain('data-testid="user-create-dialog"')
    expect(src).toContain('data-testid="user-edit-role"')
    expect(src).toContain('data-testid="user-edit-role-dialog"')
    expect(src).toContain('编辑角色')
    expect(src).toContain('data-testid="user-create"')
    expect(src).toContain('deleted: true')
    // 行内不再用 select 冒充编辑；角色 select 仅在弹框内
    expect(src).toMatch(/data-testid="user-row-role"/)
    expect(src).toContain('user-edit-role-dialog')
  })

  it('ADMIN-only UI: non-ADMIN structurally gated; soft-delete via PUT deleted', () => {
    expect(src).toContain("role.value === 'ADMIN'")
    expect(src).toContain('deleted: true')
    expect(src).toContain('listUsers')
    expect(src).toContain('createUser')
    expect(src).toContain('updateUser')
  })

  it('shell whitelist: admin-users ADMIN-only; my-products PROVIDER (V1.4)', () => {
    expect(layoutSrc).toContain('nav-users')
    expect(layoutSrc).toContain('/admin/users')
    expect(layoutSrc).toContain('userManageVisible')
    expect(layoutSrc).toContain("role.value === 'ADMIN'")
    expect(layoutSrc).toContain('session-role-label')
    expect(layoutSrc).toContain('myProductsVisible')
    expect(layoutSrc).toContain('nav-my-products')
    expect(layoutSrc).toContain('/my-products')
    expect(layoutSrc).not.toMatch(/import\s*\{[^}]*canManageUsers/)
    expect(routesSrc).toContain("path: '/admin/users'")
    expect(routesSrc).toContain('UsersAdminPage')
  })
})
