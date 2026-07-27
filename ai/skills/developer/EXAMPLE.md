# Developer Skill 已填写示例（通用底线）

> 仅展示填写粒度，不代表 AI-SEP 默认技术栈。以下路径与命令均是假设值，复制时必须按真实仓库修改。

```markdown
---
id: skill.developer
version: 1.0.0
status: active
owner: tech-lead
inputSchema: task-package@1
compatibleRoles: [developer]
appliesTo:
  paths: ["**/*"]
relatedRules: [RULE-ORG-CODING, RULE-PROJECT-LAYOUT]
relatedSkills: [skill.frontend-developer, skill.backend-developer]
---

# Developer Skill

## 适用范围

- 所有实现类任务的共用底线；前后端具体命令以路径装载的栈 Skill 为准
- 工作目录：仓库根目录
- 变更必须落在任务包 `allowModify` 内

## 前置条件

- 已阅读任务包、关联 REQ、适用 Rule 与冻结契约
- 本地密钥仅从 `*.example` 文件派生；不得读取生产机密
- 若路径命中 `frontend/**` 或 `backend/**`，必须同时遵循对应栈 Skill

## 实现工作流

1. 确认 REQ 映射与验收场景，未冻结契约则升级，不自行发明。
2. 搜索同类实现，最小变更；禁止顺手重构无关文件。
3. 按栈 Skill 验证矩阵从低成本到高成本执行。
4. 输出摘要必须含命令与真实结果（PASSED/FAILED/BLOCKED）。

## 验证矩阵（底线）

| 场景 | 命令 | 必须执行条件 | 通过标准 |
|---|---|---|---|
| 栈 Skill 适用项 | 见 frontend/backend Skill | 路径命中对应栈 | 各命令退出码符合栈 Skill |
| 契约一致性 | 变更触及 API 时对照 `contracts/openapi/` | API/字段变化 | 无未记录破坏性差异 |
| 秘密扫描自检 | 确认未新增真实密钥/生产数据 | 每任务 | 无真实机密入仓 |

## 禁止事项

- 不得扩大 `allowModify` 或修改 `denyModify` 路径下文件
- 不得关闭 Lint/测试/类型检查来掩盖错误
- 不得提交构建产物、真实环境文件或无关全库格式化
- 不得在未批准 ADR 的情况下引入第二套框架或模式

## 输出示例

- 任务：`TASK-ORDER-014`
- 关联需求：`REQ-ORDER-007`
- 修改文件：见栈 Skill 报告
- 实现摘要：仅实现验收所需最小变更
- 已执行验证：见栈 Skill 命令结果
- 未执行验证：无，或写明 BLOCKED 与解除条件
- 已知风险：无
```
