# UX/UI Planner Skill 已填写示例（acme-orders）

> 仅展示填写粒度，不代表 AI-SEP 默认设计系统。以下路径均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.ux-ui-planner
version: 1.0.0
status: active
owner: product-design
compatibleRoles: [uxUiPlanner]
appliesTo:
  paths: [frontend/**]
relatedSkills: [skill.frontend-developer]
---

# UX/UI Planner Skill

## 设计系统

- Token：`frontend/src/styles/tokens.css`
- 组件库：项目内 `frontend/src/components/`（无第二套外部设计系统，除非 ADR）
- 文案与空态：优先复用现有订单列表模式

## 规划产出

- 关键流文字稿或线框路径（本示例：文字稿即可）
- 状态：loading / empty / error / success 必须在任务验收中写明
- 无障碍：核心操作可键盘到达；控件有可达名称

## 与前端协作

- 组件路径变更由前端负责人写入 `skill.frontend-developer`
- 本 Skill 不选定 Vue/React；栈以 `RULE-ORG-STACK` 为准

## 输出示例

- 场景：订单列表按状态筛选
- 交互：筛选变更写入 URL query；空态显示「无匹配订单」+ 清除筛选
- Token：使用现有 `--color-text-secondary`，不新增色板
```
