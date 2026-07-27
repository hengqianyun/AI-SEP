# RULE-ORG-SECURITY 填写模板

> 已填写示例：[SECURITY-EXAMPLE.md](./SECURITY-EXAMPLE.md)（仅粒度参考，非默认）
> Security/Ops Owner 主导填写 `RULE-ORG-SECURITY.md`；工程侧可起草可执行条款。尖括号内容必须替换。  
> 默认 `override: forbidden`，Project 层不得削弱。禁止写入真实密钥、生产数据或未脱敏示例。

## 1. 元数据

```yaml
---
id: RULE-ORG-SECURITY
version: <语义化版本，如 1.0.0>
status: draft
owner: <securityOperationsOwner 或安全委员会>
override: forbidden
appliesTo:
  roles: ["*"]
reviewBy: <YYYY-MM-DD 或 TBD>
---
```

## 2. 数据分级与处理

```markdown
## 数据分级

| 级别 | 含义 | 允许位置/处理方式 | 禁止行为 | 自动/人工检查 |
|---|---|---|---|---|
| `<级别名>` | `<含义>` | `<存储/传输/日志要求>` | `<禁止项>` | `<命令或流程>` |

## 必须

- 密码/凭据存储：`<要求>`
- PII / 敏感个人信息：`<要求>`
- 支付或高敏数据（若适用）：`<要求或 N/A>`
- 审计日志保留：`<保留期与访问控制>`
- 密钥来源与轮换：`<批准来源、禁止落盘位置、轮换/吊销>`
```

## 3. 密钥与秘密

```markdown
## 密钥与秘密

- 批准来源：`<密钥管理系统 / CI 机密 / 本地示例文件约定>`
- 禁止位置：`<仓库、日志、截图、任务包正文等>`
- 本地开发：`<从示例文件创建；禁止提交真实环境文件>`
- 泄露响应：`<吊销、轮换、通知路径>`
```

## 4. 安全评审触发

可与 `ai/workflow/policies.yaml` 的 `riskTriggers` 对齐；此处写可判定条件。

```markdown
## 触发 Security Reviewer 的条件

| ID | 条件 | 必需评审角色 | 必需证据 |
|---|---|---|---|
| `<稳定标识>` | `<可判定的变更条件，如鉴权模型变更>` | `<角色>` | `<测试/审查/批准记录>` |

示例条件类别（按项目取舍，勿照抄未适用项）：
- 鉴权/授权模型变更
- 对外暴露新接口
- 处理个人数据或高敏数据
- 依赖引入高风险组件
```

## 5. 完成检查

- [ ] 数据分级与处理要求可执行
- [ ] 密钥规则不含真实秘密，禁止位置明确
- [ ] 安全评审触发条件可判定，并与 policies 风险标签一致或已说明差异
- [ ] Security/Ops Owner 已会签；未会签不得 `active`
- [ ] `override: forbidden` 保持不变
- [ ] Owner 将 `status` 改为 `active`
