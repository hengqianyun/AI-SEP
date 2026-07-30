---
id: RULE-ORG-STACK
version: 1.1.0
status: active
owner: architecture-committee
override: allowed-with-adr
appliesTo:
  roles: ["solutionArchitect", "developer", "codeReviewer", "apiDataDesigner"]
reviewBy: 2026-10-28
pilot: WSC
---

# 官方技术栈与版本边界（WSC 试点）

> 后端与数据基线对齐组织 `data-chain` 骨架（见 DEC-WSC-005）。Java 17 见 DEC-WSC-004。

## 允许

| 层 | 技术 | 版本范围 | 备注 |
|---|---|---|---|
| 语言 | TypeScript | 5.x | 前端 |
| 语言 | Java | 17 LTS | 后端（DEC-WSC-004） |
| 前端 | Vue 3 + Vite + pnpm | Vue ^3.4 / Vite ^5 | `frontend/` |
| 前端 UI | Ant Design Vue | 与 Vue 3 兼容主版本 | 禁止第二套 UI 框架 |
| 前端状态 | Pinia + Vue Router 4 | 随 frontend package.json | |
| 后端 | Spring Boot | **2.7.18** | Maven 多模块；目录见 LAYOUT（DEC-WSC-005） |
| 后端 Web / 数据访问 | Spring Boot Web、Spring Data JPA、QueryDSL | 随 Boot 2.7.18 BOM | 禁止第二套 ORM |
| 数据 | MySQL + Flyway | 单主数据源 | 迁移目录见 backend / migration Skill |
| 缓存 | Spring Data Redis | 随 Boot | 配置见 backend Skill 环境变量表 |
| API 文档 | Knife4j / OpenAPI 3 | 随服务模块 | `/doc.html`、`/v3/api-docs` |
| 测试 | Vitest / JUnit 5 + Testcontainers / Playwright | 见各 Skill | |
| 上链 | 模拟存证适配层 | V1 | 可替换；字段见 DEC-WSC-002 |

## 禁止引入（除非 ADR 批准）

- 第二套前端框架 / 状态管理 / HTTP Client / UI 库
- 第二套后端 Web 框架 / ORM（与 JPA 并存）/ 迁移工具
- 多数据源、读写分离、动态数据源（DEC-WSC-005）
- 真实区块链 SDK 直连业务层（须经适配层接口）
- 未扫描许可证的 npm/Maven 依赖

## 引入新依赖的门槛

- [ ] 说明替代方案
- [ ] 安全与许可证检查
- [ ] Tech Lead 或架构委员会批准
- [ ] 证据落盘 `design/decisions/`
