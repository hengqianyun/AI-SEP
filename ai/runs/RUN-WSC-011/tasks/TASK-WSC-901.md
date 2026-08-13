# 任务包 — TASK-WSC-901：我的目录菜单

```yaml
taskId: TASK-WSC-901
planId: PLAN-WSC-8.1
wave: A
status: IN_PROGRESS
requirements: [REQ-SHELL-009, REQ-SHELL-010]
dependsOn: []
startedAt: 2026-08-14
```

## 1. 任务目标

管理员和数据提供方增加菜单"我的目录"，功能同数据提供方的目录维护页面。
数据提供方不展示"目录维护"菜单。

## 2. 写集（writeSet）

| 文件 | 操作 | 说明 |
|------|------|------|
| `frontend/src/features/auth/composables/useCanWrite.ts` | 修改 | 添加 canSeeMyMaintenance 函数 |
| `frontend/src/layouts/WorkbenchLayout.vue` | 修改 | 添加"我的目录"菜单项 |
| `frontend/src/router/routes.ts` | 修改 | 添加 /my-maintenance 路由 |

## 3. 只读集（readSet）

| 文件 | 说明 |
|------|------|
| `SNAP-WSC-008` | 需求文档 |
| `contracts/rbac/matrix.yaml` | RBAC矩阵 |

## 4. 禁止修改（denyModify）

| 文件 | 原因 |
|------|------|
| `frontend/src/features/catalog/browse/**` | Wave C 任务范围 |
| `frontend/src/features/catalog/maintenance/**` | 仅复用，不修改 |

## 5. 验收标准

- [ ] 管理员可见"我的目录"菜单
- [ ] 数据提供方可见"我的目录"菜单
- [ ] 普通用户不可见"我的目录"菜单
- [ ] 数据提供方不可见"目录维护"菜单
- [ ] 路由 `/my-maintenance` 可访问
