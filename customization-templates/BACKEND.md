# 后端定制任务填写模板（BE-01～BE-11）

> 用法：复制对应任务章节到目标交付物或工作 PR，替换所有 `<待填写>`。不适用项填写 `N/A` 并说明原因。本文不替项目选择技术栈或业务规则。

## 目录

- [BE-01](#be-01)
- [BE-02](#be-02)
- [BE-03](#be-03)
- [BE-04](#be-04)
- [BE-05](#be-05)
- [BE-06](#be-06)
- [BE-07](#be-07)
- [BE-08](#be-08)
- [BE-09](#be-09)
- [BE-10](#be-10)
- [BE-11](#be-11)

## BE-01

**后端/数据技术栈**

**目标交付物**：`RULE-ORG-STACK.md` 后端与数据段落  
**依赖**：无

| 类别 | 选型/产品 | 版本或约束 | 用途 | 官方/内部依据 | 禁止或例外 |
|---|---|---|---|---|---|
| 运行时/语言 | `<待填写>` | `<待填写>` | `<待填写>` | `<链接或路径>` | `<待填写/N/A + 原因>` |
| 应用框架 | `<待填写>` | `<待填写>` | `<待填写>` | `<链接或路径>` | `<待填写/N/A + 原因>` |
| 数据存储 | `<待填写>` | `<待填写>` | `<待填写>` | `<链接或路径>` | `<待填写/N/A + 原因>` |
| 数据访问层 | `<待填写>` | `<待填写>` | `<待填写>` | `<链接或路径>` | `<待填写/N/A + 原因>` |
| 迁移工具 | `<待填写>` | `<待填写>` | `<待填写>` | `<链接或路径>` | `<待填写/N/A + 原因>` |
| 其他基础组件 | `<待填写>` | `<待填写>` | `<待填写>` | `<链接或路径>` | `<待填写/N/A + 原因>` |

**责任确认**
- 起草人（Backend R）：`<姓名/角色>`；日期：`<YYYY-MM-DD>`
- 复核人（QA C）：`<姓名/角色>`；批准人（Tech Lead A）：`<姓名/角色>`

**完成检查**
- [ ] 语言、框架、数据存储和迁移工具均有明确版本或版本策略
- [ ] 每项选型有仓库内依据或批准记录，不含个人偏好式默认值
- [ ] 禁止项、例外和 `N/A` 均可客观审查

## BE-02

**API / 日志 / 错误码约定**

**目标交付物**：`RULE-ORG-CODING.md`  
**依赖**：BE-01

### API 契约

```yaml
apiConvention:
  contractLocation: "<待填写：契约文件路径>"
  versioning: "<待填写>"
  authenticationHeader:
    name: "<待填写>"
    format: "<待填写，不放真实凭据>"
  pagination:
    requestFields: ["<待填写>"]
    responseFields: ["<待填写>"]
  compatibilityPolicy: "<待填写>"
```

### 错误与日志

```yaml
errorResponse:
  fields:
    code: "<稳定错误标识字段>"
    message: "<安全的用户可读信息字段>"
    correlationId: "<关联标识字段>"
    details: "<可选字段及暴露限制>"
  example: "<待填写：脱敏示例>"
logging:
  requiredFields: ["<待填写>"]
  forbiddenFields: ["<凭据/敏感数据类别，待确认>"]
  levelsAndUsage: "<待填写>"
  correlationRule: "<待填写>"
```

**责任确认**
- 起草人（Backend R）：`<姓名/角色>`；QA 咨询：`<姓名/角色>`
- 最终批准（Tech Lead A）：`<姓名/角色>`；确认日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 错误结构、鉴权头和分页均含字段级脱敏示例
- [ ] 日志必填/禁止字段及关联规则可检查
- [ ] 兼容策略与 BE-01 技术能力一致

## BE-03

**安全红线（工程可执行部分）**

**目标交付物**：`RULE-ORG-SECURITY.md` 协作稿  
**依赖**：无；激活前必须由 Security/Ops Owner 会签

| 数据/能力类别 | 分级 | 允许位置/处理方式 | 禁止行为 | 自动/人工检查 | 触发安全评审条件 |
|---|---|---|---|---|---|
| `<待填写>` | `<待填写>` | `<待填写>` | `<待填写>` | `<命令/流程>` | `<待填写>` |

```yaml
secrets:
  approvedSources: ["<待填写>"]
  forbiddenLocations: ["<待填写>"]
  rotationAndRevocation: "<待填写>"
securityReviewTriggers:
  - id: "<稳定标识>"
    condition: "<可判定的变更条件>"
    requiredReviewer: "<角色>"
    requiredEvidence: ["<待填写>"]
```

**责任确认**
- 后端起草（R）：`<姓名/角色>`；安全会签：`<Security/Ops Owner>`
- Tech Lead 批准（A）：`<姓名/角色>`；会签日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 数据分级、密钥处理和安全评审触发条件均为可执行规则
- [ ] 不含真实密钥、生产数据或敏感示例
- [ ] Security/Ops Owner 已明确会签；未会签不得标记 `active`

## BE-04

**分支 / PR / 发布惯例**

**目标交付物**：`RULE-ORG-RELEASE.md`  
**依赖**：协调人确认 Maintainer

```yaml
workflow:
  branchNaming: "<待填写>"
  baseBranch: "<待填写>"
  commitPolicy: "<待填写>"
pullRequest:
  requiredSections: ["<待填写>"]
  requiredChecks: ["<待填写>"]
  approvalRules: "<待填写>"
release:
  environments: ["<待填写>"]
  promotionRule: "<待填写>"
  rollbackAuthority: "<角色>"
  emergencyProcedure: "<路径或流程>"
```

**责任确认**
- 后端起草（R）：`<姓名/角色>`；Maintainer 确认：`<姓名/角色>`
- QA 咨询：`<姓名/角色>`；批准人（A）：`<姓名/角色>`

**完成检查**
- [ ] 内容与团队实际分支、PR 和发布流程一致
- [ ] 必需检查、批准权限、紧急路径和回滚权限明确
- [ ] 所有引用的流程/命令真实存在或标明待创建责任人

## BE-05

**项目架构约束**

**目标交付物**：`RULE-PROJECT-ARCHITECTURE.md`  
**依赖**：BE-01

### 边界与关系

| 模块/边界 | 职责 | 允许依赖 | 禁止依赖 | 对外契约位置 | Owner |
|---|---|---|---|---|---|
| `<待填写>` | `<待填写>` | `<待填写>` | `<待填写>` | `<仓库路径>` | `<角色>` |

### 不可破坏约定

- `ARCH-INV-<编号>`：`<必须始终成立的架构约束>`
  - 检查方式：`<测试/静态检查/评审步骤>`
  - 例外审批：`<角色与记录位置>`

### 决策入口

- ADR/设计决策目录：`<仓库路径>`
- 必须新增决策记录的条件：`<待填写>`
- NFR 基线引用：`<路径或 N/A + 原因>`

**责任确认**
- 后端起草（R）：`<姓名/角色>`；受影响模块 Owner：`<姓名/角色>`
- Tech Lead 批准（A）：`<姓名/角色>`；日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 边界、模块关系和禁止依赖可由路径或检查规则验证
- [ ] 每条不可破坏约定有稳定 ID 和验证方式
- [ ] ADR 入口及触发条件明确

## BE-06

**后端布局与共享契约路径**

**目标交付物**：`RULE-PROJECT-LAYOUT.md`、`modules.yaml` 的 `paths`  
**依赖**：无

| 路径/glob | 内容职责 | Owner | 允许写入角色 | 共享/独占 | 并行写规则 |
|---|---|---|---|---|---|
| `<待填写>` | `<待填写>` | `<角色>` | `<角色列表>` | `<共享/独占>` | `<待填写>` |

```yaml
modules:
  - code: "<由产品定义或引用现有值>"
    name: "<由产品定义或引用现有值>"
    ownerRole: "<待确认>"
    paths:
      - "<后端实现路径/glob>"
      - "<测试路径/glob>"
sharedContracts:
  - path: "<共享契约目录/glob>"
    ownerRole: "<角色>"
    parallelWrite: "forbidden"
    changeProtocol: "<锁定/协调/评审流程>"
```

**责任确认**
- 后端路径负责人（R）：`<姓名/角色>`；模块业务含义咨询：`<Product Owner>`
- 协调批准（A）：`<Tech Lead/Maintainer>`；日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 后端、测试、迁移和共享契约路径均映射到 Owner
- [ ] 共享契约明确禁止无协调并行写入
- [ ] 模块名称不由本任务擅自定义，且与术语表一致

## BE-07

**API/Data Skill**

**目标交付物**：`ai/skills/api-data-designer/SKILL.md`  
**依赖**：BE-02

```yaml
---
id: skill.api-data-designer
version: "<待填写>"
status: draft
owner: "<角色>"
inputSchema: "<待填写/N/A + 原因>"
compatibleRoles: ["<仓库约定的角色标识>"]
---
```

### 契约与数据设计执行骨架

- 输入与前置条件：`<任务包、需求、现有契约路径>`
- API 契约落盘路径：`<路径/glob>`
- 数据模型/Schema 落盘路径：`<路径/glob>`
- 兼容性分类与判定：`<兼容/破坏性变更规则>`
- 迁移与回填路径：`<路径或流程>`
- 必须产出的示例/证据：`<待填写>`
- 升级条件：`<何时交由架构/安全/迁移评审>`

**责任确认**
- 后端/API Data 负责人（R）：`<姓名/角色>`；QA 咨询：`<姓名/角色>`
- Tech Lead 批准（A）：`<姓名/角色>`；日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 契约、Schema、迁移产物均有真实落盘路径
- [ ] 兼容/破坏性变更判定及升级条件明确
- [ ] Skill 中不包含未批准的业务模型或技术栈假设

## BE-08

**Architect / Parallel / Migration Skill**

**目标交付物**：`solution-architect`、`parallel-planner`、`migration-review` 对应 Skill  
**依赖**：BE-05、BE-06

| Skill | 输入 | 必填输出 | 项目约束 | 升级/阻塞条件 |
|---|---|---|---|---|
| `solution-architect` | `<待填写>` | `<架构方案、NFR 证据等>` | `<NFR 基线/边界引用>` | `<待填写>` |
| `parallel-planner` | `<待填写>` | `<写集、依赖、合并顺序等>` | `<共享路径/冲突规则引用>` | `<待填写>` |
| `migration-review` | `<待填写>` | `<迁移、验证、回滚剧本等>` | `<数据安全/兼容规则引用>` | `<待填写>` |

### 回滚剧本占位

```markdown
- 变更/迁移标识：<待填写>
- 前置备份或恢复点：<待填写>
- 回滚触发阈值：<待填写>
- 回滚步骤：<待填写>
- 数据一致性验证：<待填写>
- 决策人与执行人：<角色>
```

**责任确认**
- 三项 Skill 后端负责人（R）：`<姓名/角色>`；QA/受影响 Owner：`<姓名/角色>`
- Tech Lead 批准（A）：`<姓名/角色>`；日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] Architect 有可量化或可验证的 NFR 基线
- [ ] Parallel 明确写集冲突与共享契约协调规则
- [ ] Migration 包含前置条件、验证、回滚和责任角色

## BE-09

**Backend Developer / Review Skill**

**目标交付物**：独立的 `skill.backend-developer`（建议路径：`ai/skills/backend-developer/SKILL.md`）及 `code-review` 后端段落  
**依赖**：BE-01

> 必须采用与通用 `skill.developer`、前端技术栈 Skill 相同的“通用基线 + 按路径独立装载”模式。后端 Skill 是独立文件，不与前端共用技术栈正文；其 frontmatter 必须声明 `compatibleRoles: [developer]`。

```yaml
---
id: skill.backend-developer
version: "<待填写>"
status: draft
owner: "<后端负责人角色>"
inputSchema: task-package@1
compatibleRoles: [developer]
---
```

```markdown
# Backend Developer Skill

> 适用路径：<后端路径/glob>
> 通用执行底线：继承/同时装载 `skill.developer`

## 工作目录与环境
- 工作目录：<仓库根目录或明确子目录>
- 前置工具及版本验证：<可复制命令>
- 环境变量来源：<路径/注入方式；不得写真实密钥>

## 可复制命令
| 目的 | 命令 | 预期结果 | 不适用说明 |
|---|---|---|---|
| 安装/恢复依赖 | `<待填写>` | `<退出码/摘要>` | `<N/A + 原因>` |
| 格式/静态检查 | `<待填写>` | `<退出码/摘要>` | `<N/A + 原因>` |
| 类型/编译检查 | `<待填写>` | `<退出码/摘要>` | `<N/A + 原因>` |
| 单元测试 | `<待填写>` | `<退出码/摘要>` | `<N/A + 原因>` |
| 集成测试 | `<待填写>` | `<退出码/摘要>` | `<N/A + 原因>` |
| 构建/打包 | `<待填写>` | `<产物/退出码>` | `<N/A + 原因>` |
| 本地启动/健康检查 | `<待填写>` | `<可观察结果>` | `<N/A + 原因>` |

## 实现约定
- 目录/模块边界：<真实路径与规则>
- API/错误/日志规则引用：<文件与章节>
- 数据访问与迁移规则引用：<文件与章节>
- 禁止事项及客观检查方式：<待填写>
- 升级条件：<架构、安全、迁移或共享契约触发条件>

## 提交审查前自检
- [ ] 变更均在任务 `allowModify` 内
- [ ] 已执行全部适用命令并保存结果
- [ ] 未通过关闭检查、跳过测试或硬编码环境掩盖问题
- [ ] 环境阻塞记为 `BLOCKED`，未宣称通过
```

### Code Review 后端检查项

| 检查项 | 阻塞条件 | 证据 | 明确不作为阻塞的纯风格项 |
|---|---|---|---|
| `<待填写>` | `<可判定条件>` | `<命令/测试/路径>` | `<待填写/N/A>` |

**责任确认**
- 后端负责人（R）：`<姓名/角色>`；QA 复核命令：`<姓名/角色>`
- Tech Lead 批准并激活（A）：`<姓名/角色>`；日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 独立 Skill 的 ID 为 `skill.backend-developer`
- [ ] frontmatter 明确为 `compatibleRoles: [developer]`
- [ ] 所有适用命令可从声明的工作目录复制执行；不适用项有原因
- [ ] 后端约定未混入前端技术栈正文，且 `context-map` 可按路径装载
- [ ] Reviewer 能区分真实缺陷与不应阻塞的纯风格意见

## BE-10

**风险触发标签**

**目标交付物**：`policies.yaml` → `riskTriggers`  
**依赖**：BE-03

```yaml
riskTriggers:
  - id: "<稳定标签，如项目自定义标识>"
    description: "<风险的技术含义>"
    match:
      paths: ["<glob>"]
      changeTypes: ["<可识别变更类型>"]
      keywords: ["<仅作辅助，不应单独误判的词>"]
    requiredRoles: ["<评审角色>"]
    requiredEvidence: ["<测试/审查/批准记录>"]
    releaseGate: "<阻塞条件>"
    falsePositiveHandling: "<豁免角色与记录位置>"
```

**责任确认**
- 后端/安全协作起草（R）：`<姓名/角色>`；QA 咨询：`<姓名/角色>`
- 协调人合并批准（A）：`<姓名/角色>`；日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 标签与项目真实变更类型对齐，覆盖鉴权、敏感数据、迁移等适用风险
- [ ] 每个触发器有评审角色、证据、发布门禁和误报处理
- [ ] 与 BE-03 安全评审条件无冲突

## BE-11

**context-map 后端 / 迁移路径**

**目标交付物**：`context-map.yaml`  
**依赖**：BE-06、BE-10

```yaml
byPath:
  - paths: ["<后端实现路径/glob>"]
    rules: ["<后端规则 ID>"]
    skills: [skill.developer, skill.backend-developer, "<其他适用 Skill>"]
    requiredRoles: ["<待填写>"]
  - paths: ["<迁移路径/glob>"]
    rules: ["<数据/安全规则 ID>"]
    skills: ["<迁移 Skill ID>"]
    requiredRoles: ["<迁移/数据/安全评审角色>"]
    riskTriggers: ["<BE-10 标签 ID>"]
  - paths: ["<共享契约路径/glob>"]
    rules: ["<契约规则 ID>"]
    skills: ["<API/Data Skill ID>"]
    requiredRoles: ["<契约 Owner/评审角色>"]
    parallelWrite: forbidden
```

**责任确认**
- 后端路径负责人（R）：`<姓名/角色>`；受影响 Skill Owner：`<姓名/角色>`
- 协调人批准（A）：`<姓名/角色>`；日期：`<YYYY-MM-DD>`

**完成检查**
- [ ] 后端路径装载通用与独立 Backend Developer Skill
- [ ] 迁移和共享契约路径强制额外角色、规则或风险触发器
- [ ] 所有 glob 与仓库真实路径匹配，无悬空 Rule/Skill ID
