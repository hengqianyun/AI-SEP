# 可选 E2E 抽样 — 未执行（环境阻塞）

| 项 | 值 |
|---|---|
| actorInstance | `tester-wsc-607-r1` |
| 建议范围 | PROVIDER 目录维护可见相关（如 `p0-wsc-v1.4` 中 `nav-catalog-maintenance`） |
| 5173 | **不可用**（连接失败） |
| 8080 | UP（`/actuator/health`） |
| 命令 | **未执行** |
| 结果 | **SKIPPED / BLOCKED（环境）** — **非 PASS** |

## 解除条件

启动 frontend（`5173` 可访问）后，可选重跑例如：

```text
cd tests/e2e
# E2E_RUN=1 E2E_BASE_URL=http://127.0.0.1:5173
pnpm exec playwright test specs/p0-wsc-v1.4.spec.ts
```

（非本轮必需门禁；HOTFIX acceptance #5 称 E2E 可选。）
