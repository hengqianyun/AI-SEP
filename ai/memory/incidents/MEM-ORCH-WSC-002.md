# MEM-ORCH-WSC-002 — 并行 review/test 竞态导致假 BLOCKED

```yaml
memoryId: MEM-ORCH-WSC-002
type: incident
severity: P2
status: RECORDED
detectedAt: 2026-07-29
runId: RUN-WSC-001
wave: W3
related: [TASK-WSC-003, TASK-WSC-004, TASK-WSC-006]
```

## 现象

W3 并行派发 codeReviewer 与 tester 时，tester 先结束，此时 `REV-TASK-WSC-00x.md` 尚未落盘，TESTRUN 将 `reviewDecision: MISSING` 记为 **BLOCKED**，尽管命令层已全绿。

## 根因

门禁依赖「审查 APPROVE → 测试 PASSED」的**顺序**，但半自动编排把审查与测试**并行**调度，造成假阻塞。

## 约定

1. 同一任务：tester **须在**对应 REV 文件存在且 decision=APPROVE 后再出最终 PASSED；或先写 DRAFT 证据，审查到达后 reconfirm。
2. 并行只允许「不同任务」之间；同任务的 review→test 保持串行。
3. 假 BLOCKED 解除：reconfirm 更新 TESTRUN（EVID-*-2），不得伪造审查。
