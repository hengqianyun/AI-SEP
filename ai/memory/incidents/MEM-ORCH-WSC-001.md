# MEM-ORCH-WSC-001 — PowerShell Add-Content 破坏 JSONL

```yaml
memoryId: MEM-ORCH-WSC-001
type: incident
severity: P1
status: RECORDED
detectedAt: 2026-07-28
runId: RUN-WSC-001
artifact: ai/runs/RUN-WSC-001/events.jsonl
lines: "19-23"
relatedPrinciple: 失败先记录，后晋升知识
promoteCandidate: false
```

## 现象

`events.jsonl` 第 19–23 行不是合法 JSON，形如：

```text
{ts:2026-07-28T16:42:00+08:00,type:HUMAN_DECISION,...}
```

键名与字符串值均无双引号，机器解析会失败；其后第 24 行起恢复为合法 JSON。

## 根因

Orchestrator 在 Windows PowerShell 下用 `Add-Content` / `\"` 转义拼接 JSON 时，**引号被外壳吞掉**，写出的是“像 JSON 的纯文本”，不是 JSONL。

对比：

| 写法 | 结果 |
|---|---|
| `Add-Content ... '{\"ts\":\"...\"}'`（嵌套转义） | 引号丢失 → 非法行（L19–23） |
| here-string / 直接 `Write` 整文件合法行 | 合法 JSONL（L1–18、L24+） |

同会话更早也曾出现过类似转义损坏，后用整文件重写修复；**未写入 memory**，导致同类错误复发。

## 强制约定（本仓库半自动编排）

1. **禁止**在 PowerShell 里用多层 `\"` 拼 JSON 再 `Add-Content`。
2. 追加事件优先：
   - 用编辑器/`Write`/`StrReplace` 写完整合法 JSON 行；或
   - PowerShell here-string：`@"{"ts":"..."}"@ | Add-Content ...`（外层单引号/here-string 保护双引号）。
3. 追加后抽查：末行 `ConvertFrom-Json` 或等价解析必须成功。
4. 发现非法行：保留审计痕迹（可在 memory/本文件注明原内容），再改写为合法 JSON；**不得静默丢事件语义**。

## 处置（本 Run）

- 将 L19–23 纠正为合法 JSON（语义与原字段一致）。
- 本记录落入 `ai/memory/incidents/`，未晋升 Rule。

## 为何此前 memory 为空

- 试点半自动过程中，多数问题以聊天说明或直接改文件消化，**未执行「失败先入 memory」门禁**。
- `ai/memory/` 仅有 README，缺少强制落盘检查；Orchestrator 未把「编排侧写入失败/格式损坏」当作必须记录的事件。
- 并非“一直没有问题”：至少存在 JSONL 转义损坏、JDK 基线误设（后经 DEC-WSC-004）等，本应进 memory。
