# 项目个性化定制清单

> 用途：把抽象架构接到**本仓库**时，人类必须填写/确认的项。  
> 用法：每完成一项勾选 `[x]`，并填写「落盘路径」列中的实际文件。  
> 原则：优先改 `ai/rules/`、`ai/skills/`、`ai/workflow/`；不要为个性化重写 `ai/agents/*.md` 角色骨架。  
> 分工：按职责拆分的任务与排期见 [`PROJECT-CUSTOMIZATION-ASSIGNMENTS.md`](./PROJECT-CUSTOMIZATION-ASSIGNMENTS.md)。

**项目代号**：`WSC`  
**开始日期**：`2026-07-28`  
**负责人（Tech Lead）**：`试点操作者（一人多身份）`

### 试点豁免声明（WSC）

- **试点范围**：接入端工作台半自动交付流水线（PRD → SNAP → 规划 → …）；主会话扮演 orchestrator。
- **豁免**：Org CODING/SECURITY/RELEASE 精细化全量填写；规划侧非 P0 Skill 全部 active；Cursor Adapter 一键启动；无人值守全自动。
- **不豁免**：可读 PRD、owners、`projectId`、STACK 基线、layout/modules/glossary、P0 实现 Skill、run 状态机、规划共识门禁。
- **状态**：§0 为**试点部分完成**——允许半自动推进至规划/实现门禁，**不得**假设可无人值守跑通全流水线。

---

## 0. 完成判定

全部勾选前，Orchestrator **不得**假设本项目已可自动跑通规划→开发流水线。

- [x] §1 人类身份已点名（试点一人多身份）
- [x] §2 Organization 规则至少有一份 ACTIVE（`RULE-ORG-STACK`；其余 Org 规则试点豁免）
- [x] §3 Project 规则（目录地图 + modules/glossary；DOMAIN 细则可后续补）已落盘
- [x] §4 工作流策略已按本项目裁剪（`projectId: WSC`）
- [x] §5 通用 developer + frontend/backend、codeReviewer、tester 已填写（试点 active）
- [x] §6 制品 Schema/模板沿用仓库默认；MODULE 码表已就绪
- [x] §7 至少一份可执行的 PRD（`product/prd/wsc-v1.0.md`）

---

## 1. 人类身份与升级路由

| 勾选 | 定制项 | 落盘路径 | 填写要点 |
|---|---|---|---|
| [x] | Product Owner | `ai/rules/project/owners.yaml` | 试点操作者 |
| [x] | Tech Lead | 同上 | 试点操作者 |
| [x] | Security/Ops Owner | 同上 | 试点操作者 |
| [x] | Maintainer | 同上 | 试点操作者 |
| [x] | 升级 SLA / 找谁 | `ai/workflow/policies.yaml` → `escalation` | 保持默认 24h |

小团队可一人多身份，但每次人类批准必须**注明当时身份**。

---

## 2. Organization 规则（公司/团队级，可跨项目复用）

| 勾选 | 定制项 | 落盘路径 |
|---|---|---|
| [x] | 官方技术栈与版本边界 | `ai/rules/organization/RULE-ORG-STACK.md` |
| [ ] | 编码 / API / 日志约定 | `ai/rules/organization/RULE-ORG-CODING.md`（试点豁免） |
| [ ] | 安全与合规红线（不可被 Project 覆盖） | `ai/rules/organization/RULE-ORG-SECURITY.md`（试点豁免） |
| [ ] | 分支 / PR / 发布惯例 | `ai/rules/organization/RULE-ORG-RELEASE.md`（试点豁免） |

---

## 3. Project 规则（本仓库专属）

| 勾选 | 定制项 | 落盘路径 |
|---|---|---|
| [x] | 目录与模块地图、默认读写边界 | `ai/rules/project/RULE-PROJECT-LAYOUT.md` |
| [ ] | 领域不变量 / 业务规则索引 | `ai/rules/project/RULE-PROJECT-DOMAIN.md` + `product/business-rules/`（试点后置；DEC-WSC-* 已落盘） |
| [x] | 术语表 | `product/glossary.md` |
| [x] | MODULE 码表（ID 前缀） | `ai/rules/project/modules.yaml` |
| [x] | 路径 → 规则匹配表（装载用） | `ai/rules/project/context-map.yaml` |
| [ ] | 既有架构约束 / ADR 入口 | `ai/rules/project/RULE-PROJECT-ARCHITECTURE.md`（试点后置） |

---

## 4. 工作流裁剪与门禁

模板：`ai/workflow/policies.yaml`、`ai/workflow/definition.yaml`

| 勾选 | 定制项 | 说明 |
|---|---|---|
| [x] | 规划委员会角色启用/省略 | 可选角色保持 enabled（`policies.yaml`） |
| [x] | `maxRounds`（规划 / 代码审查） | 默认 5 |
| [x] | 强制 Security / Migration Reviewer 的条件 | 保持模板风险标签 |
| [x] | 发布是否强制人类 `maintainer` 批准 | true |
| [x] | 学习晋升阈值与审批人 | 保持模板 |

---

## 5. Skill（按技术栈填写「怎么干」）

模板目录：`ai/skills/`。角色契约不变，差异全部进 Skill。

| 勾选 | Skill ID | 优先级 | 路径 |
|---|---|---|---|
| [x] | `skill.developer`（通用基线） | P0 | `ai/skills/developer/SKILL.md` |
| [x] | `skill.frontend-developer`（项目有前端时） | P0/按需 | `ai/skills/frontend-developer/SKILL.md` |
| [x] | `skill.backend-developer`（项目有后端时） | P0/按需 | `ai/skills/backend-developer/SKILL.md` |
| [x] | `skill.code-review` | P0 | `ai/skills/code-review/SKILL.md` |
| [x] | `skill.tester` | P0 | `ai/skills/tester/SKILL.md` |
| [ ] | `skill.solution-architect` | P1 | `ai/skills/solution-architect/SKILL.md` |
| [ ] | `skill.api-data-designer` | P1 | `ai/skills/api-data-designer/SKILL.md` |
| [ ] | `skill.qa-strategist` | P1 | `ai/skills/qa-strategist/SKILL.md` |
| [ ] | `skill.parallel-planner` | P1 | `ai/skills/parallel-planner/SKILL.md` |
| [ ] | `skill.ux-ui-planner` | 按需 | `ai/skills/ux-ui-planner/SKILL.md` |
| [ ] | `skill.security-operations` | 按需 | `ai/skills/security-operations/SKILL.md` |
| [ ] | `skill.security-review` | 按需 | `ai/skills/security-review/SKILL.md` |
| [ ] | `skill.migration-review` | 按需 | `ai/skills/migration-review/SKILL.md` |
| [ ] | `skill.integration-review` | P1 | `ai/skills/integration-review/SKILL.md` |

规则：每个实际启用的代码域必须有对应的 `*-developer` Skill；不存在前端或后端时写明 `N/A`，不要创建空壳并标为 `active`。若技术栈尚未确定，Skill 必须保持 `draft`。

---

## 6. Schema / 制品模板

| 勾选 | 定制项 | 落盘路径 |
|---|---|---|
| [ ] | 规划评审输出 | `ai/schemas/planning-review.schema.json` + `ai/schemas/templates/planning-review.md` |
| [ ] | 任务包 | `ai/schemas/task-package.schema.json` + `ai/schemas/templates/task-package.md` |
| [ ] | 代码审查 | `ai/schemas/code-review.schema.json` + `ai/schemas/templates/code-review.md` |
| [ ] | 测试证据 | `ai/schemas/test-evidence.schema.json` + `ai/schemas/templates/test-evidence.md` |
| [ ] | 需求快照 | `ai/schemas/templates/requirement-snapshot.md` |

---

## 7. 产品输入（流程点火）

| 勾选 | 定制项 | 落盘路径 |
|---|---|---|
| [x] | PRD | `product/prd/wsc-v1.0.md` |
| [x] | Out of Scope / 非目标 | 写在 PRD |
| [x] | 已有设计决策（若有） | `design/decisions/DEC-WSC-001..003.md` |

---

## 8. 运行时映射（后置，工具相关）

| 勾选 | 定制项 | 落盘路径 |
|---|---|---|
| [ ] | Cursor / 其他 Adapter 说明 | `ai/adapters/README.md` |
| [ ] | 隔离执行方式（worktree/分支） | `ai/workflow/policies.yaml` → `isolation` |

业务规则仍写在 `ai/rules/`，**不要**只写在某一工具的私有配置里。

---

## 9. 可后置

- [ ] `ai/evaluations/` 评测集
- [ ] Learning 过期复查周期
- [ ] 性能/安全数值门槛
- [ ] UI tokens / 组件库（纯后端可省略并记原因）

---

## 快速对照

| 层 | 谁填 | 目录 |
|---|---|---|
| Global | 平台 | `ai/rules/global/` |
| Organization | 架构委员会 | `ai/rules/organization/` |
| Project | Tech Lead + PO | `ai/rules/project/` + `product/` |
| Skill | 栈负责人 | `ai/skills/` |
| Workflow | 项目负责人 | `ai/workflow/` |
| Agent 契约 | 一般不改 | `ai/agents/` |
