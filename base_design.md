



```
project
│
├── product/                  # 产品层
│   ├── prd/
│   │    v1.0.md
│   │    v1.1.md
│   │
│   ├── changelog/
│   │
│   └── glossary.md           # 业务术语
│
├── design/                   # UI设计层
│   │
│   ├── ui-spec/
│   │
│   ├── prototype/
│   │
│   ├── components/
│   │
│   └── design-token/
│
├── planning/                 # AI规划层
│   │
│   ├── task/
│   │
│   ├── coding-plan/
│   │
│   ├── api-design/
│   │
│   ├── database/
│   │
│   └── dependency-graph/
│
├── frontend/
│
├── backend/
│
├── e2e/
│
├── ai/
│   ├── skills/
│   ├── rules/
│   ├── prompts/
│   ├── templates/
│   ├── agents/
│   └── memory/
│
└── docs/
```

---

# 第一层 Product

这一层唯一职责：

> 告诉 AI 我想做什么。

建议不仅仅放 PRD。

例如

```

```

```
product
    prd/
        user.md

        role.md

        permission.md

    changelog/

    glossary.md

    business-rule.md

    user-story.md
```

例如：

```

```

```
business-rule.md

权限规则：

超级管理员

可以管理全部组织

组织管理员

只能管理自己组织

普通员工

没有管理权限
```

AI非常喜欢这种结构化文档。

比全部塞PRD里效果好很多。

---

# 第二层 Design

这里不要只放UI。

应该放

```

```

```
design

    prototype/

    ui-spec/

    components/

    tokens/

    assets/
```

例如：

```

```

```
Button

Primary

Danger

Success

Hover

Disabled
```

AI以后生成页面的时候不用猜。

---

# 第三层 Planning

这是整个AI开发最重要的一层。

也是很多团队没有的。

例如

```

```

```
planning

    coding-plan/

    api-design/

    database/

    task/

    dependency/

```

例如 AI解析PRD后生成

```

```

```
Task-001

实现用户管理

依赖：

登录

组织

接口：

GET /users

POST /users

数据库：

User

Role
```

再继续拆

```

```

```
Task001

↓

Frontend

Backend

Test

Review
```

Agent就可以并发。

---

# Coding Plan

我建议不是一个文档。

而是一堆。

例如

```

```

```
coding-plan

    epic-user/

        overview.md

        FE-001.md

        FE-002.md

        BE-001.md

        TEST-001.md
```

每个Agent拿一个。

---

# API Design

例如

```

```

```
GET

/users

返回

{
 list:[]
}
```

AI开发Frontend的时候不用再猜接口。

---

# Database

```

```

```
User

id

name

role

dept

```

AI写SQL直接看这里。

---

# Dependency

AI其实最怕

不知道先做什么。

所以

```

```

```
graph.md

Login

↓

Permission

↓

User

↓

Role

↓

Menu
```

Agent知道依赖。

---

# Frontend

这里建议增加

```

```

```
frontend

    src/

    docs/

        architecture/

        coding-standard/

        route/

        state/

        api/

```

例如

```

```

```
state

所有Pinia Store说明
```

以后Agent不用分析代码。

---

# Backend

同理

```

```

```
backend

    docs/

        architecture/

        dto/

        service/

        entity/
```

---

# Test

你的结构已经很好。

我建议再加两层。

```

```

```
e2e

    tests

    pages

    fixtures

    utils

    data

    reports

    snapshots

    docs

        testcase/

        coverage/

```

---

testcase不是代码。

而是

```

```

```
新增用户

步骤

登录

点击新增

输入

保存

预期

成功
```

AI再生成Playwright。

---

# AI目录

我建议不要叫config。

建议直接

```

```

```
ai
```

因为未来东西会越来越多。

例如

```

```

```
ai

    agents/

    skills/

    rules/

    prompts/

    templates/

    memory/

    logs/

```

---

## rules

例如

```

```

```
vue.rule

所有组件必须script setup

必须使用Composition API

不能使用Options API

```

---

## skills

例如

```

```

```
generate-crud.skill

输入

PRD

输出

CRUD页面
```

或者

```

```

```
write-playwright.skill

输入

TestCase

输出

Playwright
```

---

## prompt

例如

```

```

```
frontend.prompt

你是高级Vue工程师

...
```

---

## memory

这是很多人没有想到的。

Agent需要记忆。

例如

```

```

```
memory

bug-history/

coding-history/

review/

```

例如

```

```

```
Bug-001

原因：

Select组件异步加载

经验：

所有Select等待接口返回

```

以后Agent开发到Select

直接读取。

---

# 学习机制

这是我觉得最值得做的一部分。

建议增加

```

```

```
learning/

```

例如

```

```

```
learning

    failed-tests/

    root-cause/

    generated-rules/

    generated-skills/

```

工作流

```

```

```
Playwright失败

↓

AI读取Report

↓

定位代码

↓

分析失败原因

↓

总结经验

↓

生成Rule

↓

下次开发自动引用
```

例如

失败

```

```

```
Locator Timeout
```

AI总结

```

```

```
Rule-018

所有Dialog

必须等待动画结束

禁止立即点击
```

以后自动加入rule。

---

再比如

```

```

```
连续5次

都是Table滚动失败

```

AI总结

```

```

```
Skill

TableScroll.skill
```

以后所有项目都会用。

---

# 完整工作流（推荐）

```

```

```
PRD
        │
        ▼
AI解析业务
        │
        ▼
生成任务(Task)
        │
        ├──────────────┐
        ▼              ▼
生成API        生成UI
        │              │
        └──────┬───────┘
               ▼
      Frontend Agent
               │
               ▼
      Backend Agent
               │
               ▼
       自动Code Review
               │
               ▼
     AI生成Test Case
               │
               ▼
 AI生成Playwright脚本
               │
               ▼
        自动执行E2E
               │
      ┌────────┴─────────┐
      ▼                  ▼
    成功              失败
      │                  │
      ▼                  ▼
   Merge        Root Cause Analysis
                          │
                          ▼
              Rule / Skill Generator
                          │
                          ▼
                  更新 ai/rules
                  更新 ai/skills
                          │
                          ▼
                  下一轮开发引用
```

## 可以进一步优化的几个关键点

如果目标是打造一个长期可演进的 AI 开发平台，而不仅是一个项目模板，我建议增加以下能力：

1. **需求可追踪性（Traceability）**：为每条 PRD 需求分配唯一 ID（如 `REQ-001`），并在设计、开发任务、代码注释、测试用例中引用该 ID。这样可以快速回答“这个需求是否已经实现并覆盖测试”。 
2. **Agent 明确职责边界**：不要让一个 Agent 完成所有事情，而是拆分为 Product Analyst、UI Planner、Frontend Developer、Backend Developer、Code Reviewer、Test Generator、Root Cause Analyzer 等角色，每个角色只消费固定格式的输入并输出固定格式的文档。 
3. **知识库分层**：将 `ai/rules` 区分为全局规则（所有项目通用）、团队规则（公司规范）和项目规则（当前项目特有），避免项目越来越大后规则互相污染。 
4. **自动评估学习价值**：不是所有失败都值得生成 Rule。建议增加一个筛选流程，例如同类问题重复出现 3 次以上才提升为 Rule，否则仅记录到 `memory` 中，避免规则库膨胀。 
5. **版本化 Prompt 与 Skill**：随着模型升级，同一个 Skill 的 Prompt 也会演进。建议每个 Skill 和 Prompt 都带版本号，并记录适用模型、更新时间和效果说明，方便回滚和持续优化。 

整体来看，你的设想已经不仅仅是“AI 辅助编码”，而是一个**AI 驱动的软件工程平台（AI Software Engineering Platform）**。如果把文档规范、Agent 通信协议、知识沉淀机制设计好，后续无论接入 Cursor、Claude Code、Codex 还是其他 Agent，都可以复用同一套工程体系。