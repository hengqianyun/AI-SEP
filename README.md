# AI-SEP

**AI Software Engineering Pipeline** — 工具中立的多 Agent 软件交付架构模板。

> 本仓库提供的是**可复制的协作协议与目录骨架**，不是某个业务系统的实现，也不是「一个全能自动写代码的机器人」。  
> 下游项目通过填写 Rule / Skill / Policy 完成个性化后，再接入 Cursor、Claude Code、Codex 等运行时。

| 项 | 说明 |
|---|---|
| 定位 | 架构模板 + 控制面约定 + 角色契约 |
| 架构真理源 | [`design.md`](./design.md) |
| 产品说明（模板自身） | [`product/prd/v1.0.md`](./product/prd/v1.0.md)（状态：`DRAFT`） |
| 实例化勾选 | [`PROJECT-CUSTOMIZATION.md`](./PROJECT-CUSTOMIZATION.md) |
| 人工分工 | [`PROJECT-CUSTOMIZATION-ASSIGNMENTS.md`](./PROJECT-CUSTOMIZATION-ASSIGNMENTS.md) |
| 实现仓映射 | [`ai/rules/project/repos.yaml`](./ai/rules/project/repos.yaml)（前端/后端独立 Codeup 仓） |

> **本仓角色**：AI-SEP **控制面**（产品/规划/Run）保留本地。业务实现推送至：  
> - 前端 [`data-chain-static`](https://codeup.aliyun.com/5f4356276207a1a8b17f985c/SH-BIGDATA/data-chain/data-chain-static.git)（目录 `frontend/`）  
> - 后端 [`data-chain-backend`](https://codeup.aliyun.com/5f4356276207a1a8b17f985c/SH-BIGDATA/data-chain/data-chain-backend.git)（目录 `backend/`）  

---

## 1. 要解决什么问题

常见多 Agent 用法容易出现：

- 单会话「换帽子」扮演开发与审查，结论不可审计；
- 进度只存在于聊天里，关会话即丢失权威状态；
- 规范与具体项目、具体 IDE 绑死，无法复制；
- 一次失败就写进全局规则，污染知识库；
- 规划未共识就编码，并行任务写冲突无法证明安全。

AI-SEP 的目标是建立一条**可追踪、可并行、可审计、可回滚**的交付流水线，并让同一套骨架能被不同项目实例化。

### 核心原则（摘要）

1. **文档是协作协议** — Agent 只通过版本化制品交接，不以聊天为权威源。  
2. **角色职责单一** — 分析、规划、开发、审查、测试分离。  
3. **规划先于编码** — 必需角色明确批准后才能开发。  
4. **并行必须可证明安全** — 依赖满足且写集无冲突。  
5. **开发与审查相互独立** — 同一任务 `developer` ≠ `codeReviewer`。  
6. **结论可追踪** — REQ / PLAN / TASK / REVIEW / TEST 等稳定 ID。  
7. **失败先记录，后晋升知识** — 禁止单次失败直接晋升全局 Rule。  
8. **人类拥有最终决策权** — 无法共识则 `ESCALATED`，不得伪造批准。

完整论述见 [`design.md`](./design.md)。

---

## 2. 本仓库是什么 / 不是什么

### 是

- 多 Agent **角色契约**（`ai/agents/`）
- **规则分层**与 **Skill** 空壳（供实例化填写）
- **工作流定义与策略**模板（`ai/workflow/`）
- **制品 Schema 与 Markdown 模板**（评审、任务包、测试证据等）
- **实例化清单与跨职能任务分配**（产品 / 前后端 / 测试 / 协调人）
- 启动与恢复**协议文档**（半自动也可遵循）

### 不是

- 现成的业务应用（商城、后台等应在下游仓库）
- 已填好的公司技术栈与领域规则（Org/Project 多为 `draft` 占位）
- 已绑定单一 IDE 的插件（Adapter 后置）
- 无人值守从 PRD 到生产的全自动系统（V1 允许人工门禁与半自动调度）

`temp/`、`temp2/`、`docs.zip` 等为本地试验或参考物，**不是**模板权威内容，请勿当作正式制品源。

---

## 3. 当前状态（如何理解「能做什么」）

| 能力 | 状态 |
|---|---|
| 阅读架构并理解角色边界 | 可用 |
| 按分工人工填写 Rule / Skill / Policy | 可用（模板已就绪） |
| 将本仓库直接当已配置项目跑完整流水线 | **不可用**（需先实例化填写） |
| CLI / Cursor Adapter 一键启动 | 未实装（协议已有，实现后置） |

判定依据见定制清单 [`PROJECT-CUSTOMIZATION.md`](./PROJECT-CUSTOMIZATION.md) 的 **§0 完成判定**：未勾选完成前，不得假设 Orchestrator 可自动跑通规划→开发。

---

## 4. 目录结构

### 4.1 仓库现状（模板已落盘）

```text
AI-SEP/
├── README.md                          # 本文件
├── design.md                          # 架构规范（真理源）
├── PROJECT-CUSTOMIZATION.md           # 实例化勾选清单
├── PROJECT-CUSTOMIZATION-ASSIGNMENTS.md  # 人工任务分配（RACI）
├── product/                           # 产品层（模板自身 PRD + 占位）
│   ├── prd/v1.0.md
│   ├── glossary.md
│   └── business-rules/
├── design/
│   └── decisions/                     # ADR / 设计决策落盘处
├── ai/                                # Agent 控制与知识面
│   ├── agents/                        # 角色契约（稳定，一般不改）
│   ├── rules/
│   │   ├── global/                    # 平台底线（已有 active 范例）
│   │   ├── organization/              # 公司级规范（待实例化填写）
│   │   └── project/                   # 本项目规范 + owners/modules/context-map
│   ├── skills/                        # 按角色的「怎么做」（多为 draft）
│   ├── schemas/                       # JSON Schema + Markdown 制品模板
│   ├── workflow/                      # definition / policies / start / resume
│   ├── adapters/                      # 运行时映射（占位）
│   ├── runs/                          # Run 状态（执行时产生）
│   └── memory/                        # 任务级失败与发现
└── learning/                          # 知识晋升候选（运行后产生）
```

实例化后的业务仓库通常还会按 `design.md` 补齐 `planning/`、`frontend/`、`backend/`、`tests/` 等；**本模板不强制预置空业务代码树**。

### 4.2 分层职责

| 层 | 路径 | 职责 |
|---|---|---|
| 产品 | `product/` | 做什么、为什么、业务约束与术语 |
| 设计 | `design/` | 决策、交互与视觉约定（按项目需要） |
| 规划 | `planning/`（实例化后） | 计划、评审、任务 DAG、API/数据契约 |
| 实现 | `frontend/` / `backend/`（实例化后） | 源码 |
| 验证 | `tests/`（实例化后） | 测试与追踪证据 |
| AI 控制面 | `ai/` | 角色、规则、技能、工作流、制品校验、运行状态 |
| 学习 | `learning/` | 从失败中筛选可复用知识（需审批） |

---

## 5. 个性化模型（模板如何变成「你的项目」）

个性化**不要**靠重写 `ai/agents/*.md`，而应写入：

```text
Task 上下文  >  Project 规则  >  Organization 规则  >  Global 规则
```

低层不能覆盖标注为不可覆盖的安全/合规 Global/Org 规则。

| 类型 | 含义 | 典型路径 |
|---|---|---|
| Rule | 约束与红线（做什么 / 不做什么） | `ai/rules/**` |
| Skill | 可执行做法（命令、检查清单） | `ai/skills/**/SKILL.md` |
| Policy | 门禁、角色启用、升级路由 | `ai/workflow/policies.yaml` |
| Schema/模板 | 制品形状 | `ai/schemas/**` |
| Owner | 人类决策身份 | `ai/rules/project/owners.yaml` |

模板中 `projectId`、`owners.name` 等应为 **`REPLACE_ME` 或空字符串**；填入真实值是**下游实例化**的第一步，不是维护本模板时的默认内容。

详细勾选：[`PROJECT-CUSTOMIZATION.md`](./PROJECT-CUSTOMIZATION.md)  
按产品 / 前端 / 后端 / 测试 / 协调人分工：[`PROJECT-CUSTOMIZATION-ASSIGNMENTS.md`](./PROJECT-CUSTOMIZATION-ASSIGNMENTS.md)

---

## 6. Agent 角色一览

默认运行模型：

```text
用户 ↔ 唯一主会话（orchestrator）
         └── 按需隔离 subagent 装载 ai/agents/{role}.md + 适用 Rule/Skill
```

| 层级 | 角色 | 说明 |
|---|---|---|
| 编排 | `orchestrator` | 状态机、调度、门禁；不做专业代批 |
| 规划委员会 | `productAnalyst`、`solutionArchitect`、`qaStrategist`、`parallelPlanner`、`planEditor` | 核心不可省略 |
| 规划可选 | `uxUiPlanner`、`apiDataDesigner`、`securityOperations` | 可省略，须在 policies 写原因 |
| 实现对抗 | `developer`、`codeReviewer` | 每任务必需且必须不同实例 |
| 验证 | `tester`、`integrationReviewer` | 测试与并行束集成审查 |
| 高风险 | `securityReviewer`、`migrationReviewer` | 由 risk 标签触发 |
| 人类 | `productOwner`、`techLead`、`securityOperationsOwner`、`maintainer` | 见 `human-owners.md` |

规划评审决策枚举仅允许：`APPROVE` / `REQUEST_CHANGES` / `BLOCK` / `ABSTAIN`。

详情：[`ai/agents/README.md`](./ai/agents/README.md)

---

## 7. 交付主流程（逻辑）

```text
PRD
  → 需求快照（productAnalyst）
  → 候选计划（planEditor）
  → 规划委员会独立评审 → 共识或 ESCALATED
  → 任务 DAG（parallelPlanner）
  → developer ↔ codeReviewer
  → tester
  → integrationReviewer（并行束）
  → 发布门禁 / 人类批准
```

制品链：

```text
PRD → Requirement → Design Decision → Approved Plan
  → Task DAG → Code Change → Code Review → Test Evidence
  → Release → Learning Candidate
```

工作流节点与策略：[`ai/workflow/definition.yaml`](./ai/workflow/definition.yaml)、[`ai/workflow/policies.yaml`](./ai/workflow/policies.yaml)  
启动 / 恢复协议：[`ai/workflow/start.md`](./ai/workflow/start.md)、[`ai/workflow/resume.md`](./ai/workflow/resume.md)

---

## 8. 如何使用本模板

### 8.1 维护本仓库（架构模板）

- 演进 `design.md` 与 `ai/agents/` 契约时保持工具中立。  
- Global 规则与 Schema 变更需可回滚、可说明。  
- 保持实例相关文件为**明确占位**，避免半填入某个具体项目。

### 8.2 实例化到新业务项目（推荐路径）

1. 复制本仓库（或作为 monorepo 子树 / 模板生成）。  
2. 指定协调人，打开 `PROJECT-CUSTOMIZATION.md`，按 `PROJECT-CUSTOMIZATION-ASSIGNMENTS.md` 认领任务。  
3. 填写 `owners.yaml`、`projectId`、Org/Project 规则、P0 Skill（developer / code-review / tester）。  
4. 裁剪 `policies.yaml`（省略角色须写 `omitReason`）。  
5. 勾选 §0；将对应文件 `draft` → `active`。  
6. 放入业务 PRD，再按 `start.md` 以半自动或 Adapter 方式启动 Run。

### 8.3 最小用户启动话术（实例化且 §0 通过后）

```text
启动交付流程。
PRD：product/prd/<file>.md
```

主会话只扮演 `orchestrator`，专业结论必须落盘。

---

## 9. 关键文档索引

| 文档 | 用途 |
|---|---|
| [`docs/AI-SEP-架构说明.md`](./docs/AI-SEP-架构说明.md) | **架构单一入口说明**：背景、目录、工作流、操作与 **WSC/Console 已完成闭环**样例 |
| [`design.md`](./design.md) | 完整架构：制品链、DAG、对抗、学习、控制面 |
| [`product/prd/v1.0.md`](./product/prd/v1.0.md) | 本模板作为「平台产品」的需求说明 |
| [`PROJECT-CUSTOMIZATION.md`](./PROJECT-CUSTOMIZATION.md) | 实例化必填项勾选 |
| [`PROJECT-CUSTOMIZATION-ASSIGNMENTS.md`](./PROJECT-CUSTOMIZATION-ASSIGNMENTS.md) | 谁写哪份规范 |
| [`customization-templates/`](./customization-templates/README.md) | 每个实例化任务的可复制填写模板 |
| [`ai/README.md`](./ai/README.md) | `ai/` 目录导读 |
| [`ai/agents/README.md`](./ai/agents/README.md) | 角色总表与契约共性 |
| [`ai/rules/README.md`](./ai/rules/README.md) | 规则分层与装载 |
| [`ai/skills/README.md`](./ai/skills/README.md) | Skill 约定 |
| [`ai/schemas/README.md`](./ai/schemas/README.md) | 制品校验与模板 |
| [`ai/workflow/README.md`](./ai/workflow/README.md) | 工作流文件说明 |

---

## 10. 贡献与变更约定

- **改角色骨架**（`ai/agents/`）：影响所有下游，需架构级评审。  
- **改 Global 规则**：安全/共识底线，默认高门槛。  
- **改 Org/Project 模板空壳**：保持可填写、无具体项目私货。  
- **规范冲突**：落盘 `design/decisions/`，禁止只在聊天里改。  
- Rule / Skill：`draft` → `active` 须有明确责任人；失败经验先入 `ai/memory/`，再走 `learning/` 晋升。

---

## 11. 许可与范围声明

本 README 描述的是仓库**当前作为架构模板**的用途与边界。具体组织的技术栈、合规条款与业务需求，须在实例化时由对应 Owner 写入 `ai/rules/organization|project` 与 `product/`，不在本模板中预设。
