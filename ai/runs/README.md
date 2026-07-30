# runs/

每次交付运行一个 `RUN-{PROJECT}-{NNN}/`（由 Orchestrator/Adapter 创建）。

## 何时必须新建 Run

- **明确新的 PRD 版本时必须起草新 Run**（例：`wsc-v1.0.md` → `wsc-v1.1.md` ⇒ `RUN-WSC-001` → `RUN-WSC-002`）。
- 不得在已 `COMPLETED` / `CANCELLED` 的 Run 上为新 PRD 版本续派发开发任务。
- 同一 PRD 文件的小幅澄清若不升版本号，可继续当前活动 Run；一旦文件名/版本字段升级，先建新 Run 再规划。

## 本仓 Run

| runId | PRD | 状态 |
|---|---|---|
| [`RUN-WSC-001`](./RUN-WSC-001/) | `product/prd/wsc-v1.0.md` | COMPLETED |
| [`RUN-WSC-002`](./RUN-WSC-002/) | `product/prd/wsc-v1.1.md` | ACTIVE（PLAN_APPROVED · PLAN-WSC-2.1） |

勿手改 `events.jsonl` 历史；恢复协议见 `ai/workflow/resume.md`。
