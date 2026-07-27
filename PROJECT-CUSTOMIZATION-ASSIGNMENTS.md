# 项目模板定制 — 任务分配计划

> 适用：本仓库作为**项目模板**被新项目实例化时，人工补齐规则、Skill、门禁。  
> 勾选总表见 [`PROJECT-CUSTOMIZATION.md`](./PROJECT-CUSTOMIZATION.md)。  
> 所有任务的可复制填写参考见各交付物旁的 `TEMPLATE` 文件（与 [`FRONTEND-TEMPLATE.md`](./ai/skills/frontend-developer/FRONTEND-TEMPLATE.md) 同模式）；已填写粒度参考见同目录 `*-EXAMPLE*`（如 [`FRONTEND-EXAMPLE-VUE.md`](./ai/skills/frontend-developer/FRONTEND-EXAMPLE-VUE.md)、[`BACKEND-EXAMPLE-SPRING.md`](./ai/skills/backend-developer/BACKEND-EXAMPLE-SPRING.md)、[`RELEASE-EXAMPLE.md`](./ai/rules/organization/RELEASE-EXAMPLE.md)），**仅示例、非默认栈**。Template 列可直达空白稿。
> 旧的集中索引：[`customization-templates/`](./customization-templates/README.md)（以旁路 TEMPLATE 为准）。
> 原则：各角色只改自己负责的落盘文件；`ai/agents/*.md` 角色骨架不改。

### 覆盖范围说明（是否「完整」）

| 范围 | 是否覆盖 |
|---|---|
| 实例化时人工必填的 Org/Project Rule、P0～P1 Skill、policies、owners、modules、主 Schema | **是** — 见 §3 任务 ID |
| `PROJECT-CUSTOMIZATION.md` §0～§7 的落地责任人 | **是** — 与 RACI §4 对齐 |
| 无前端 / 无 UX 时的裁剪路径 | **部分** — 仅 P-05/FE-06 跳过提示；未单列「后端-only 省略 FE 全套」清单 |
| `skill.security-operations` / `skill.security-review` | **弱** — 安全红线在 BE-03；这两份 Skill 正文未单独立任务（见 §3.6） |
| `productAnalyst` / `planEditor` 专属 Skill | **否** — 仓库未建对应 Skill 文件；靠角色契约 + 制品模板即可起步 |
| Global 规则、`ai/agents` 契约、Adapter/CLI、Learning 评测集 | **否（有意）** — 属平台维护或后置，见定制清单 §8～§9 |
| 业务功能开发任务（写业务代码） | **否** — 本文件只覆盖「规范定制」，不含功能 backlog |

结论：对「实例化后谁来写规范」而言，**主路径完整**；不是全仓库一切工作的总任务书。缺口任务见 §3.6。

---

## 1. 角色与职责边界

| 人工角色 | 对应人类身份（可兼任） | 负责什么 | 不负责什么 |
|---|---|---|---|
| 产品人员 | `productOwner` | 业务范围、术语、领域规则、PRD、UX 是否启用 | 技术栈版本、测试命令、仓库布局细节 |
| 前端开发 | （贡献 `techLead` / Maintainer 时可兼任） | 前端栈、UI/组件约定、前端目录与审查要点 | 后端迁移、领域不变量正文 |
| 后端开发 | 同上 | 后端栈、API/数据约定、迁移与架构约束 | 验收业务优先级、E2E 业务场景文案 |
| 测试人员 | （发布门禁协作） | 测试分层、命令、环境、覆盖门槛、测试证据字段 | 业务 Out of Scope、实现选型 |
| 项目协调人（建议指定 1 人） | `techLead` + `maintainer` | 汇总 `owners.yaml`、`policies.yaml`、合并冲突、勾选 §0 | 代替各角色写专业正文 |

小团队：一人可兼多职，但**每个交付物仍按下表署名责任人**，避免无人认领。

---

## 2. 阶段与依赖（推荐顺序）

```text
阶段 A 点火（0.5～1 天）
  └─ 协调人：owners + projectId + 升级 SLA
        ↓
阶段 B 规范并行（2～4 天）
  ├─ 产品：术语 / 领域 / PRD 骨架
  ├─ 前端：前端栈 + UX Skill + 前端布局段落
  ├─ 后端：后端栈 + API/架构/迁移
  └─ 测试：测试策略 + tester/qa Skill + 门禁阈值建议
        ↓
阶段 C 合流（0.5～1 天）
  └─ 协调人：合并 RULE-ORG-STACK/CODING、LAYOUT、context-map、policies
        ↓
阶段 D 门禁冻结（0.5 天）
  └─ 全员确认 riskTriggers / release / maxRounds → status: active
        ↓
阶段 E 试点（可选）
  └─ 产品给一份最小 PRD → 跑通一轮规划评审
```

未完成阶段 C 前，不要把大量 Skill 标为 `active`（避免 Orchestrator 装载半成品）。

---

## 3. 任务清单（按职责）

### 3.1 产品人员

| ID | 任务 | 交付物 | 完成标准 | 依赖 | Template |
|---|---|---|---|---|---|
| P-01 | 确认 Product Owner 信息 | `ai/rules/project/owners.yaml` → `productOwner` | name/contact 非空 | — | [P-01](./ai/rules/project/OWNERS-TEMPLATE.yaml) |
| P-02 | 编写术语表 | `product/glossary.md` | 核心实体 ≥ 一版可评审；含易混淆点 | — | [P-02](./product/GLOSSARY-TEMPLATE.md) |
| P-03 | 领域不变量 + 业务规则 | `RULE-PROJECT-DOMAIN.md` + `product/business-rules/*` | 至少列出违反即 P0 的不变量；规则有 ID | P-02 | [P-03](./ai/rules/project/DOMAIN-TEMPLATE.md) |
| P-04 | MODULE 业务含义 | `modules.yaml` 中 `code`/`name`/`ownerRole` | 与术语表一致；路径可暂空由开发补 | P-02 | [P-04](./ai/rules/project/MODULES-TEMPLATE.yaml) |
| P-05 | 规划角色裁剪（业务侧） | `policies.yaml` → `optionalRoles.uxUiPlanner` | 无 UI 则 `enabled: false` 并写 `omitReason` | — | [P-05](./ai/workflow/POLICIES-TEMPLATE.yaml) |
| P-06 | UX/体验 Skill（若启用 UX） | `ai/skills/ux-ui-planner/SKILL.md` | 设计系统路径或「暂无、用文字稿」写清 | P-05 | [P-06](./ai/skills/ux-ui-planner/TEMPLATE.md) |
| P-07 | 试点 PRD + 非目标 | `product/prd/` | 有范围、优先级、Out of Scope、开放问题 | P-02～P-03 | [P-07](./product/prd/PRD-TEMPLATE.md) |
| P-08 | 需求快照模板试填 | `ai/schemas/templates/requirement-snapshot.md` 样例或首份 SNAP | 字段可被产品分析角色直接套用 | P-07 | [P-08](./ai/schemas/templates/requirement-snapshot.md) |
| P-09 | 既有产品向设计决策（若有） | `design/decisions/` | 决策有 ID；否则注明 N/A | — | [P-09](./design/decisions/DEC-TEMPLATE.md) |

**产品验收口径**：业务同学不看代码也能判断「Agent 是否越权改需求」。

---

### 3.2 前端开发人员

> 若组织未预先指定前端技术栈，**FE-01～FE-04 由项目的前端负责人/前端团队制定**。AI 不得代选 Vue、React 等方案；普通开发者也不得未经评审自行将个人偏好设为项目规范。前端团队起草，测试人员确认验证门禁，Tech Lead 最终批准 `active`。

| ID | 任务 | 交付物 | 完成标准 | 依赖 | Template |
|---|---|---|---|---|---|
| FE-01 | 前端技术栈段落 | `RULE-ORG-STACK.md` 前端行 | 框架/版本/禁止库写清 | — | [FE-01](./ai/rules/organization/STACK-TEMPLATE.md) |
| FE-02 | 前端编码约定 | `RULE-ORG-CODING.md` 中前端节，或项目补充段 | 目录约定、组件/状态/路由规范 | FE-01 | [FE-02](./ai/rules/organization/CODING-TEMPLATE.md) |
| FE-03 | 前端目录与写边界 | `RULE-PROJECT-LAYOUT.md` 前端表；`modules.yaml` paths | `frontend/`（或实际路径）可被 parallelPlanner 使用 | — | [FE-03](./ai/rules/project/LAYOUT-TEMPLATE.md) |
| FE-04 | Frontend Developer Skill | `ai/skills/frontend-developer/SKILL.md` | 前端负责人按模板填写；lint/类型/单测/构建/启动命令可复制执行，经 QA 与 Tech Lead 复核后激活 | FE-01～FE-03 | [FE-04](./ai/skills/frontend-developer/FRONTEND-TEMPLATE.md) |
| FE-05 | Code Review 前端检查项 | `ai/skills/code-review/SKILL.md` | 列出必查与「不可风格阻塞」项 | FE-02 | [FE-05](./ai/skills/code-review/TEMPLATE.md) |
| FE-06 | UX Skill 技术侧补充 | `ux-ui-planner/SKILL.md` 组件库/tokens 路径 | 与产品 P-06 不冲突；无 UI 则跳过 | P-05 | [FE-06](./ai/skills/ux-ui-planner/TEMPLATE.md) |
| FE-07 | context-map 前端路径 | `context-map.yaml` → `byPath` | 前端 glob → 额外 rules/skills | FE-03 | [FE-07](./ai/rules/project/CONTEXT-MAP-TEMPLATE.yaml) |
| FE-08 | 集成冒烟（前端入口） | `integration-review/SKILL.md` 相关条 | 给出可执行的冒烟路径/命令 | FE-04、QA 配合 | [FE-08](./ai/skills/integration-review/TEMPLATE.md) |

---

### 3.3 后端开发人员

| ID | 任务 | 交付物 | 完成标准 | 依赖 | Template |
|---|---|---|---|---|---|
| BE-01 | 后端/数据技术栈 | `RULE-ORG-STACK.md` 后端与数据行 | 语言、框架、DB、迁移工具版本 | — | [BE-01](./ai/rules/organization/STACK-TEMPLATE.md) |
| BE-02 | API / 日志 / 错误码约定 | `RULE-ORG-CODING.md` | 错误结构、鉴权头、分页有示例字段 | BE-01 | [BE-02](./ai/rules/organization/CODING-TEMPLATE.md) |
| BE-03 | 安全红线（工程可执行部分） | `RULE-ORG-SECURITY.md` 协作稿 | 数据分级、密钥、触发 Security Review 条件；需安全负责人会签 | — | [BE-03](./ai/rules/organization/SECURITY-TEMPLATE.md) |
| BE-04 | 分支/PR/发布惯例 | `RULE-ORG-RELEASE.md` | 与团队真实流程一致 | 协调人确认 Maintainer | [BE-04](./ai/rules/organization/RELEASE-TEMPLATE.md) |
| BE-05 | 项目架构约束 | `RULE-PROJECT-ARCHITECTURE.md` | 边界、模块关系、不可破坏约定、ADR 入口 | BE-01 | [BE-05](./ai/rules/project/ARCHITECTURE-TEMPLATE.md) |
| BE-06 | 后端布局与共享契约路径 | `RULE-PROJECT-LAYOUT.md` + `modules.yaml` paths | 标明共享契约目录（禁止乱并行写） | — | [BE-06](./ai/rules/project/LAYOUT-TEMPLATE.md) |
| BE-07 | API/Data Skill | `ai/skills/api-data-designer/SKILL.md` | 契约落盘路径、兼容策略、迁移路径 | BE-02 | [BE-07](./ai/skills/api-data-designer/TEMPLATE.md) |
| BE-08 | Architect / Parallel / Migration Skill | `solution-architect`、`parallel-planner`、`migration-review` | NFR 基线、写集冲突提示、回滚剧本要点 | BE-05～BE-06 | [arch](./ai/skills/solution-architect/TEMPLATE.md) · [parallel](./ai/skills/parallel-planner/TEMPLATE.md) · [migration](./ai/skills/migration-review/TEMPLATE.md) |
| BE-09 | Backend Developer / Review Skill | `ai/skills/backend-developer/SKILL.md`；并补 `code-review` | 后端负责人按 TEMPLATE 填写真实命令与约定，不与前端共用技术栈正文 | BE-01 | [BE-09](./ai/skills/backend-developer/TEMPLATE.md) |
| BE-10 | 风险触发标签 | `policies.yaml` → `riskTriggers` | 与真实变更类型对齐（鉴权/PII/迁移等） | BE-03 | [BE-10](./ai/workflow/POLICIES-TEMPLATE.yaml) |
| BE-11 | context-map 后端/迁移路径 | `context-map.yaml` | migrations 等强制额外角色 | BE-06、BE-10 | [BE-11](./ai/rules/project/CONTEXT-MAP-TEMPLATE.yaml) |

---

### 3.4 测试人员

| ID | 任务 | 交付物 | 完成标准 | 依赖 | Template |
|---|---|---|---|---|---|
| QA-01 | 测试分层与强制层 | `ai/skills/qa-strategist/SKILL.md` | unit/integration/e2e 目标与「何时强制」 | FE-04、BE-09 命令草案 | [QA-01](./ai/skills/qa-strategist/TEMPLATE.md) |
| QA-02 | Tester Skill（可执行） | `ai/skills/tester/SKILL.md` | 环境、fixture 约定、分层命令、阻塞≠通过 | QA-01 | [QA-02](./ai/skills/tester/TEMPLATE.md) |
| QA-03 | 覆盖与发布测试门槛 | `policies.yaml` 建议项 + Skill 内门槛 | P0 REQ / 关键路径要求可检查 | P-07 优先级口径 | [QA-03](./ai/workflow/POLICIES-TEMPLATE.yaml) |
| QA-04 | 测试证据模板确认 | `test-evidence.schema.json` + `templates/test-evidence.md` | 字段满足审计；按需增列 | QA-02 | [QA-04](./ai/schemas/templates/test-evidence.md) |
| QA-05 | 集成审查测试视角 | `integration-review/SKILL.md` | 跨模块冒烟、契约测试入口 | FE-08、BE | [QA-05](./ai/skills/integration-review/TEMPLATE.md) |
| QA-06 | 规划委员会：是否强化 QA 门禁 | `policies.yaml` → `maxRounds` / release 相关 | 给出「测试未证据化不得 COMPLETED」的确认 | 协调人合并 | [QA-06](./ai/workflow/POLICIES-TEMPLATE.yaml) |
| QA-07 | 安全/隐私测试触发（协作） | 与 BE-03/`riskTriggers` 对齐检查表 | handles-pii 等有对应测试注意点 | BE-10 | [QA-07](./ai/rules/organization/SECURITY-TEMPLATE.md) |

---

### 3.5 项目协调人（Tech Lead / Maintainer）

| ID | 任务 | 交付物 | 完成标准 | Template |
|---|---|---|---|---|
| C-01 | 填全 `owners.yaml` 四身份 | 含升级联系人 | 四人字段齐全（可一人多身份） | [C-01](./ai/rules/project/OWNERS-TEMPLATE.yaml) |
| C-02 | `projectId` 全局替换 | `owners`/`modules`/`policies`/`context-map` | 无 `REPLACE_ME` | [C-02](./ai/rules/project/OWNERS-TEMPLATE.yaml) |
| C-03 | 合并前后端对 STACK/CODING/LAYOUT 的段落 | 无互相矛盾版本 | 冲突已裁决 | [C-03](./ai/rules/organization/STACK-TEMPLATE.md) |
| C-04 | 定稿 `policies.yaml` | 角色启用、escalation、release、isolation、learning | 与各角色确认签字 | [C-04](./ai/workflow/POLICIES-TEMPLATE.yaml) |
| C-05 | 勾选 `PROJECT-CUSTOMIZATION.md` §0 | 全部或写明试点豁免 | 豁免须列未完成项与风险 | [C-05](./PROJECT-CUSTOMIZATION.md) |
| C-06 | Schema 任务包/评审字段（工程侧） | `task-package`、`planning-review`、`code-review` | 与并行/审查习惯一致；产品确认需求快照 | [C-06](./ai/schemas/README.md) |
| C-07 | Adapter / 隔离方式（可后置） | `ai/adapters/`、`policies.isolation` | 工具选定后再做 | [C-07](./ai/adapters/ADAPTER-TEMPLATE.md) |

### 3.6 已知未单列 / 按需追加

| ID | 任务 | 建议责任人 | 说明 | Template |
|---|---|---|---|---|
| OPT-01 | `skill.security-operations` / `skill.security-review` 正文 | 后端 + Security Owner | 启用 security 规划/审查角色时必填；可与 BE-03 同 PR | [ops](./ai/skills/security-operations/TEMPLATE.md) · [review](./ai/skills/security-review/TEMPLATE.md) |
| OPT-02 | 纯后端项目：标注 FE 全套 N/A | 协调人 | 在 §0 豁免中写明；跳过 FE-01～FE-08 | [OPT-02](./PROJECT-CUSTOMIZATION.md) |
| OPT-03 | `skill.product-analyst` / `plan-editor`（若新建） | 产品 / 协调人 | 当前无文件则不必造；契约 + 模板足够 | [OPT-03](./ai/skills/README.md) |
| OPT-04 | Learning 评测集 / 过期复查 | 协调人 | 对应定制清单 §9，实例化首轮可后置 | [OPT-04](./learning/README.md) |
| OPT-05 | Cursor/CLI Adapter 实装 | 协调人 / 平台 | 对应 C-07；不阻塞人工写规则 | [OPT-05](./ai/adapters/ADAPTER-TEMPLATE.md) |

---

## 4. RACI 总表（关键交付物）

| 交付物 | 产品 | 前端 | 后端 | 测试 | 协调人 |
|---|---|---|---|---|---|
| `owners.yaml` | C（PO） | I | I | I | **A/R** |
| `glossary` / 领域规则 / PRD | **R** | C | C | C | A |
| `RULE-ORG-STACK` | I | **R**（前端） | **R**（后端） | C | A |
| `RULE-ORG-CODING` | I | **R**（FE 节） | **R**（API 节） | C | A |
| `RULE-ORG-SECURITY` | C | C | **R** | C | A（安全会签） |
| `RULE-ORG-RELEASE` | I | C | **R** | C | A |
| `RULE-PROJECT-LAYOUT` / `modules` / `context-map` | C（模块名） | **R**（FE 路径） | **R**（BE 路径） | C | A |
| `RULE-PROJECT-DOMAIN` | **R** | I | C | C | A |
| `RULE-PROJECT-ARCHITECTURE` | I | C | **R** | I | A |
| `policies.yaml` | C（UX 省略） | C | C（risk） | C（门槛） | **A/R** |
| `skill.developer`（通用基线） | I | C | C | C | **A/R** |
| `skill.frontend-developer` / 前端 review 约定 | I | **R** | I | C | A |
| `skill.backend-developer` / 后端 review 约定 | I | I | **R** | C | A |
| `skill.tester` / `qa-strategist` | C | C | C | **R** | A |
| `skill.api-data` / `migration` / `architect` | I | I | **R** | C | A |
| `skill.security-operations` / `security-review` | C | I | **R**（OPT-01） | C | A（安全会签） |
| `skill.ux-ui-planner` | **R**（体验） | **R**（组件路径） | I | I | A |
| `skill.integration-review` | I | **R** | **R** | **R** | A |
| Schemas / 模板 | C（快照） | C | C（任务包） | **R**（证据） | A |
| 试点 PRD | **R** | I | I | C | A |

R=执行，A=问责（最终合并），C=协商，I=知情。

---

## 5. 建议排期（模板首次实例化）

| 日序 | 产品 | 前端 | 后端 | 测试 | 协调人 |
|---|---|---|---|---|---|
| D1 | P-01～P-02、P-05 | FE-01～FE-03 | BE-01～BE-02、BE-06 | QA-01 草案 | C-01～C-02 |
| D2 | P-03～P-04、P-06 | FE-04～FE-05 | BE-05、BE-07～BE-08 | QA-02 | 中期对齐会 30min |
| D3 | P-07～P-08 | FE-07～FE-08 | BE-03、BE-09～BE-11 | QA-03～QA-05 | C-03～C-04 |
| D4 | 评审业务稿 | 联调 Skill 命令 | 联调 Skill 命令 | QA-04、QA-06 | C-05～C-06，全部 `active` |
| D5+ | 试点 PRD 流转 | 观察开发/审查装载 | 同左 | 观察测试门禁 | 记 gap → learning 候选 |

人员不足时：D1～D2 合并为「规范周」，但**产品领域规则**与**测试可执行命令**不可双双空缺。

---

## 6. 完成定义（模板可复制给下一项目）

1. `PROJECT-CUSTOMIZATION.md` §0 已勾选或豁免书面化。  
2. Org/Project 关键 Rule 与 P0 Skill 均为 `status: active`。  
3. 前后端命令在干净环境下可按 Skill 文档跑通。  
4. 测试证据模板已用一次真实（或演示）命令填过样例。  
5. `policies.yaml` 的省略角色与 riskTriggers 无「待填」。  
6. 各交付物在 PR 或变更说明中有**责任人署名**。

---

## 7. 协作约定

- 同一文件多角色合写时：**分节 + 文末维护者列表**，由协调人解决冲突。  
- 状态变更：`draft` → `active` 仅协调人或文件 owner 操作。  
- 发现规范互相矛盾：开 `design/decisions/` 一条短决策，禁止只在聊天里改。  
- 安全类（`RULE-ORG-SECURITY`、剩余风险接受）：后端起草，**Security/Ops Owner 会签**后方可 `active`。
