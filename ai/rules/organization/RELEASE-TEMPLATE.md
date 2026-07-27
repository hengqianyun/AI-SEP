# RULE-ORG-RELEASE 填写模板

> 已填写示例：[RELEASE-EXAMPLE.md](./RELEASE-EXAMPLE.md)（仅粒度参考，非默认）
> Maintainer / Tech Lead 复制本结构填写 `RULE-ORG-RELEASE.md`。尖括号内容必须替换；须与 `ai/workflow/policies.yaml` 的 `release` / `escalation` 保持一致。  
> 本模板不预设托管平台或 CI 产品名；写团队真实流程与可执行检查。

## 1. 元数据

```yaml
---
id: RULE-ORG-RELEASE
version: <语义化版本，如 1.0.0>
status: draft
owner: <architecture-committee 或 Maintainer 角色>
override: allowed-with-adr
appliesTo:
  roles: ["maintainer", "orchestrator", "integrationReviewer", "tester"]
reviewBy: <YYYY-MM-DD 或 TBD>
---
```

## 2. 分支与提交

```markdown
## 分支模型

- 模型：`<trunk-based / gitflow / 其他>`
- 默认基线分支：`<分支名>`
- 分支命名：`<模式>`
- 提交约定：`<约定或 N/A；说明与 formatter 关系>`
```

## 3. PR

```markdown
## PR

- 最少审查人数：`<数字>`
- 必需审查身份/角色：`<列表>`
- 必需检查：`<CI 任务或检查名>`
- 必需章节：`<Summary / Test plan 等>`
- 合并条件：`<CI 绿、无 P0/P1、审查通过等>`
- 紧急合并例外：`<条件、批准身份、事后补证路径>`
```

## 4. 发布与回滚

```markdown
## 发布

- 环境与晋级：`<环境列表与晋级规则>`
- 发布窗口：`<窗口或 N/A>`
- 是否强制 Maintainer 人工批准：`<是/否；与 policies.yaml release 一致>`
- 批准身份：`<与 policies.yaml approverIdentity 一致>`
- 是否要求集成审查：`<是/否>`
- 是否要求全部 P0 已验证：`<是/否>`
- 回滚负责人：`<角色>`
- 紧急回滚程序：`<路径或步骤摘要>`
```

## 5. 完成检查

- [ ] 内容与团队实际分支、PR、发布流程一致
- [ ] 必需检查、批准权限、紧急路径和回滚权限明确
- [ ] 与 `policies.yaml` 的 `release` / `escalation` 无冲突
- [ ] 引用的流程/命令真实存在或标明待创建责任人
- [ ] Owner 将 `status` 改为 `active`
