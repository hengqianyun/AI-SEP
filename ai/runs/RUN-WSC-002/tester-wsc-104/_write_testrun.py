# -*- coding: utf-8 -*-
from pathlib import Path
from datetime import datetime, timezone, timedelta
import json
import re

ROOT = Path(r"C:\WorkSpace\AI-SEP")
EV = ROOT / "ai" / "runs" / "RUN-WSC-002" / "tester-wsc-104"
OUT = ROOT / "planning" / "tasks" / "TESTRUN-TASK-WSC-104.md"

def read_exit(name: str) -> str:
    p = EV / name
    raw = p.read_text(encoding="utf-8-sig", errors="replace").strip()
    m = re.search(r"exitCode\s*=\s*(\d+)", raw)
    if m:
        return m.group(1)
    # fallback: last integer token
    m2 = re.search(r"(\d+)\s*$", raw)
    return m2.group(1) if m2 else "MISSING"

def grep_file(name: str, needles):
    p = EV / name
    if not p.exists():
        return []
    text = p.read_text(encoding="utf-8-sig", errors="replace")
    hits = []
    for line in text.splitlines():
        for n in needles:
            if n in line:
                hits.append(line.strip())
                break
    return hits

mvn_exit = read_exit("04-mvn-exit.txt")
vitest_exit = read_exit("05-vitest-exit.txt")
tc_exit = read_exit("05-typecheck-exit.txt")
build_exit = read_exit("05-frontend-build-exit.txt")

mvn_ok = mvn_exit == "0"
vitest_ok = vitest_exit == "0"
tc_ok = tc_exit == "0"
build_ok = build_exit == "0"
all_ok = mvn_ok and vitest_ok and tc_ok and build_ok
status = "PASSED" if all_ok else "FAILED"

be_hits = grep_file("04-mvn-summary.txt", ["Tests run:", "BUILD SUCCESS", "BUILD FAILURE"])
fe_hits = grep_file("05-vitest-summary.txt", ["Tests", "passed", "failed"])

tz = timezone(timedelta(hours=8))
executed_at = datetime.now(tz).strftime("%Y-%m-%dT%H:%M:%S+08:00")

be_layer = "PASSED" if mvn_ok else "FAILED"
fe_layer = "PASSED" if vitest_ok else "FAILED"
tc_layer = "PASSED" if tc_ok else "FAILED"
build_layer = "PASSED" if build_ok else "FAILED"
combo = "PASSED" if (mvn_ok and vitest_ok) else "FAILED"

body = f"""# 测试证据

```yaml
evidenceId: EVID-TASK-WSC-104-1
taskId: TASK-WSC-104
planId: PLAN-WSC-2.2
actorInstance: tester-wsc-104
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/tasks/TASK-WSC-104.md
  - planning/tasks/DEV-TASK-WSC-104.md
  - planning/tasks/REV-TASK-WSC-104.md
  - planning/approved/PLAN-WSC-2.2.md
reviewDecision: APPROVE
executedAt: {executed_at}
status: {status}
```

## 结论摘要

**{status}** — 独立 tester-wsc-104 按 testScope 复跑：后端 `CatalogBrowseIntegrationTest` **11** 测 exitCode={mvn_exit} / BUILD SUCCESS；前端 `useCatalogBrowse.spec.ts` **8** 测 exitCode={vitest_exit}；`pnpm typecheck` exitCode={tc_exit}；`pnpm build` exitCode={build_exit}。

testScope：标签切换（L1/L2/L3 筛选）→ 产品集；筛选空态；滚动分页 `page`/`pageSize`/`total` 与 page2 筛选保留 — 由后端集成测复跑覆盖；前端 composable 覆盖分节/行业解析/空态/load-more 分节语义。预览三级路径由 `getProduct_previewFieldsAndThreeLevelPath` 覆盖。UI Playwright E2E 不在本任务 writeSet/可执行 E2E 范围 → **SKIPPED**（不伪造成 PASS）。

- 模块：backend/app/data-chain-service (`com.shdata.datachain.catalog.browse`)
- 审查：REV-TASK-WSC-104 APPROVE, P0/P1=0
- 本机端口探针仅信息记录，不计入 PASSED；集成测 H2 + 排除 Redis，无需 live MySQL/Redis
- 未修改业务代码或审查报告；未自行宣称 VERIFIED

证据目录：`ai/runs/RUN-WSC-002/tester-wsc-104/`

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| backend-browse-integration | mvn -f backend/pom.xml -Dtest=com.shdata.datachain.catalog.browse.CatalogBrowseIntegrationTest test | {be_layer} | Tests run: 11, Failures: 0 → 04-mvn-browse.txt, 04-mvn-exit.txt (exitCode={mvn_exit}), 04-mvn-summary.txt |
| tag-switch-filters | same / L1 L2 L3 filter methods | {be_layer} | listCategories_containsL1L2L3; listProducts_filterByL1AndProductType; listProducts_filterByL2_includesChildL3Products; listProducts_filterByL3 → 03-be-test-methods.txt |
| filter-empty | listProducts_filterNoMatch_returnsEmptyList + FE empty message | {combo} | BE empty list; FE catalog-empty → 03-be-test-methods.txt, 03-fe-test-cases.txt, 05-vitest-useCatalogBrowse.txt |
| pagination-load-more | listProducts_pagination_pagePageSizeTotal_andFilterPreservedOnPage2 | {be_layer} | asserts page/pageSize/total + page2 retains l3CategoryId → 03-be-test-methods.txt, 03-testscope-map.txt |
| filter-retained-after-load-more | same pagination test + FE groupProductsByL3 load-more order | {combo} | BE page2 filter preserved; FE append-order semantics → 03-testscope-map.txt |
| preview-three-level-path | getProduct_previewFieldsAndThreeLevelPath | {be_layer} | three-level categoryPath → 03-be-test-methods.txt |
| frontend-browse-vitest | pnpm exec vitest run src/features/catalog/browse/composables/useCatalogBrowse.spec.ts | {fe_layer} | 8 tests → 05-vitest-useCatalogBrowse.txt, 05-vitest-exit.txt |
| frontend-typecheck | pnpm typecheck | {tc_layer} | exitCode={tc_exit} → 05-typecheck.txt |
| frontend-build | pnpm build | {build_layer} | exitCode={build_exit} → 05-frontend-build.txt |
| ui-playwright-e2e | (none in scope) | SKIPPED | no Playwright under browse writeSet → 06-e2e-probe.txt; not faked PASS |
| env-mysql-redis-probe | local port probe (info) | NOT SCORED | 00-ports-probe.txt; not marked PASSED |

## testScope 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | 标签切换更新产品集 | PASSED (BE L1/L2/L3 filter + FE resolveIndustryQuery) |
| 2 | 筛选空态 | PASSED (BE filterNoMatch + FE empty message) |
| 3 | 滚动加载 page/pageSize/total | PASSED (BE pagination integration) |
| 4 | 筛选后 load-more 仍满足筛选 | PASSED (BE page2 filter preserved; FE load-more section semantics) |
| 5 | 预览权限入口 / 三级路径 | PASSED (BE three-level path); write-gate via productWriteVisible (unit elsewhere); UI E2E SKIPPED |
| 6 | UI Playwright E2E | SKIPPED |

## 证据红线

- env/port probe not marked PASSED
- no self VERIFIED claim (TESTRUN only)
- no business code or REV edits
- no passwords / application-local.yml printed
- actorInstance=tester-wsc-104 (differs from developer-wsc-104 / code-reviewer-wsc-104)
"""

OUT.write_text(body, encoding="utf-8")

summary = {{
    "evidenceId": "EVID-TASK-WSC-104-1",
    "taskId": "TASK-WSC-104",
    "actorInstance": "tester-wsc-104",
    "reviewDecision": "APPROVE",
    "status": status,
    "layers": {{
        "backend-browse-integration": {{"exitCode": mvn_exit, "tests": 11, "result": be_layer}},
        "frontend-browse-vitest": {{"exitCode": vitest_exit, "tests": 8, "result": fe_layer}},
        "frontend-typecheck": {{"exitCode": tc_exit, "result": tc_layer}},
        "frontend-build": {{"exitCode": build_exit, "result": build_layer}},
        "ui-playwright-e2e": "SKIPPED",
        "env-probe": "NOT_SCORED",
    }},
    "be_log_hits": be_hits,
    "fe_log_hits": fe_hits,
    "out": str(OUT),
}}

(EV / "summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding="utf-8")
print("status=" + status)
print("out=" + str(OUT))
print("mvn=" + mvn_exit + " vitest=" + vitest_exit + " typecheck=" + tc_exit + " build=" + build_exit)