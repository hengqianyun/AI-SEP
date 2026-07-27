# 项目个性化定制清单

> 用途：把抽象架构接到**本仓库**时，人类必须填写/确认的项。  
> 用法：每完成一项勾选 `[x]`，并填写「落盘路径」列中的实际文件。  
> 原则：优先改 `ai/rules/`、`ai/skills/`、`ai/workflow/`；不要为个性化重写 `ai/agents/*.md` 角色骨架。  
> 分工：按职责拆分的任务与排期见 [`PROJECT-CUSTOMIZATION-ASSIGNMENTS.md`](./PROJECT-CUSTOMIZATION-ASSIGNMENTS.md)。

**项目代号**：`________________`  
**开始日期**：`________________`  
**负责人（Tech Lead）**：`________________`

---

## 0. 完成判定

全部勾选前，Orchestrator **不得**假设本项目已可自动跑通规划→开发流水线。

- [ ] §1 人类身份已点名
- [ ] §2 Organization 规则至少有一份 ACTIVE
- [ ] §3 Project 规则（目录地图 + 领域不变量）已落盘
- [ ] §4 工作流策略已按本项目裁剪
- [ ] §5 通用 developer + 各启用代码域 Developer Skill、codeReviewer、tester 已填写
- [ ] §6 制品 Schema/模板与 MODULE 码表已就绪
- [ ] §7 至少一份可执行的 PRD（或明确试点范围）

---

## 1. 人类身份与升级路由

| 勾选 | 定制项 | 落盘路径 | 填写要点 |
|---|---|---|---|
| [ ] | Product Owner | `ai/rules/project/owners.yaml` | 姓名/账号；业务范围与优先级裁决 |
| [ ] | Tech Lead | 同上 | 架构升级、审查超限、技术债接受 |
| [ ] | Security/Ops Owner | 同上 | 高风险接受、合规红线 |
| [ ] | Maintainer | 同上 | 最终集成与发布放行 |
| [ ] | 升级 SLA / 找谁 | `ai/workflow/policies.yaml` → `escalation` | 超时、轮次耗尽、P0 风险接受 |

小团队可一人多身份，但每次人类批准必须**注明当时身份**。

---

## 2. Organization 规则（公司/团队级，可跨项目复用）

| 勾选 | 定制项 | 落盘路径 |
|---|---|---|
| [ ] | 官方技术栈与版本边界 | `ai/rules/organization/RULE-ORG-STACK.md` |
| [ ] | 编码 / API / 日志约定 | `ai/rules/organization/RULE-ORG-CODING.md` |
| [ ] | 安全与合规红线（不可被 Project 覆盖） | `ai/rules/organization/RULE-ORG-SECURITY.md` |
| [ ] | 分支 / PR / 发布惯例 | `ai/rules/organization/RULE-ORG-RELEASE.md` |

---

## 3. Project 规则（本仓库专属）

| 勾选 | 定制项 | 落盘路径 |
|---|---|---|
| [ ] | 目录与模块地图、默认读写边界 | `ai/rules/project/RULE-PROJECT-LAYOUT.md` |
| [ ] | 领域不变量 / 业务规则索引 | `ai/rules/project/RULE-PROJECT-DOMAIN.md` + `product/business-rules/` |
| [ ] | 术语表 | `product/glossary.md` |
| [ ] | MODULE 码表（ID 前缀） | `ai/rules/project/modules.yaml` |
| [ ] | 路径 → 规则匹配表（装载用） | `ai/rules/project/context-map.yaml` |
| [ ] | 既有架构约束 / ADR 入口 | `ai/rules/project/RULE-PROJECT-ARCHITECTURE.md` |

---

## 4. 工作流裁剪与门禁

模板：`ai/workflow/policies.yaml`、`ai/workflow/definition.yaml`

| 勾选 | 定制项 | 说明 |
|---|---|---|
| [ ] | 规划委员会角色启用/省略 | 省略 UX/API/Security 须写原因 |
| [ ] | `maxRounds`（规划 / 代码审查） | 默认 5，可改 |
| [ ] | 强制 Security / Migration Reviewer 的条件 | 风险标签、数据触达等 |
| [ ] | 发布是否强制人类 `maintainer` 批准 | `release.requireHumanApproval` |
| [ ] | 学习晋升阈值与审批人 | `learning.*` |

---

## 5. Skill（按技术栈填写「怎么干」）

模板目录：`ai/skills/`。角色契约不变，差异全部进 Skill。

| 勾选 | Skill ID | 优先级 | 路径 |
|---|---|---|---|
| [ ] | `skill.developer`（通用基线） | P0 | `ai/skills/developer/SKILL.md` |
| [ ] | `skill.frontend-developer`（项目有前端时） | P0/按需 | `ai/skills/frontend-developer/SKILL.md` |
| [ ] | `skill.backend-developer`（项目有后端时） | P0/按需 | `ai/skills/backend-developer/SKILL.md` |
| [ ] | `skill.code-review` | P0 | `ai/skills/code-review/SKILL.md` |
| [ ] | `skill.tester` | P0 | `ai/skills/tester/SKILL.md` |
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
| [ ] | PRD | `product/prd/` |
| [ ] | Out of Scope / 非目标 | 写在 PRD 或 `product/requirements/` |
| [ ] | 已有设计决策（若有） | `design/decisions/` |

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
