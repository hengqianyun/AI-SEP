---
id: RULE-ORG-STACK
version: 1.0.0
status: active
owner: architecture-committee
override: allowed-with-adr
appliesTo:
  roles: ["solutionArchitect", "developer", "codeReviewer", "apiDataDesigner"]
reviewBy: 2026-10-28
pilot: WSC
---

# 官方技术栈与版本边界（WSC 试点）

## 允许

| 层 | 技术 | 版本范围 | 备注 |
|---|---|---|---|
| 语言 | TypeScript | 5.x | 前端 |
| 语言 | Java | 21 LTS | 后端 |
| 前端 | Vue 3 + Vite + pnpm | Vue ^3.4 / Vite ^5 | `frontend/` |
| 前端 UI | Ant Design Vue | 与 Vue 3 兼容主版本 | 禁止第二套 UI 框架 |
| 前端状态 | Pinia + Vue Router 4 | 随 frontend package.json | |
| 后端 | Spring Boot | 3.3.x | `backend/` |
| 数据 | PostgreSQL + Flyway | PG 16 | 迁移目录见 backend Skill |
| 测试 | Vitest / JUnit 5 + Testcontainers / Playwright | 见各 Skill | |
| 上链 | 模拟存证适配层 | V1 | 可替换；字段见 DEC-WSC-002 |

## 禁止引入（除非 ADR 批准）

- 第二套前端框架 / 状态管理 / HTTP Client / UI 库
- 第二套后端 Web 框架 / ORM（与 JPA 并存）/ 迁移工具
- 真实区块链 SDK 直连业务层（须经适配层接口）
- 未扫描许可证的 npm/Maven 依赖

## 引入新依赖的门槛

- [ ] 说明替代方案
- [ ] 安全与许可证检查
- [ ] Tech Lead 或架构委员会批准
- [ ] 证据落盘 `design/decisions/`
