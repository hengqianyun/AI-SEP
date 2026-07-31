from pathlib import Path
from datetime import datetime, timezone, timedelta
import json

root = Path(r"C:/WorkSpace/AI-SEP")
ev = root / "ai/runs/RUN-WSC-002/tester-wsc-102"
out = root / "planning/tasks/TESTRUN-TASK-WSC-102.md"
now = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec="seconds")

# Build markdown with unicode escapes decoded
title = "\u6d4b\u8bd5\u8bc1\u636e"
concl = "\u7ed3\u8bba\u6458\u8981"
summary_body = (
    "**PASSED** \u2014 \u72ec\u7acb tester-wsc-102 \u6309 testScope \u590d\u8dd1\uff1a"
    "\u540e\u7aef RbacMatrixTest + SessionAuthIntegrationTest \u5171 12 \u6d4b BUILD SUCCESS\uff1b"
    "\u524d\u7aef useCanWrite.spec.ts 3 \u6d4b + typecheck + build \u9000\u51fa\u7801 0\u3002"
    "\u767b\u51fa/\u89d2\u8272\u53d8\u66f4\u540e\u5199 API \u62d2\u7edd\u7531\u96c6\u6210\u6d4b "
    "logout_thenImportWriteApiRejected (401) \u4e0e roleSwitch_oldSessionImportRejected "
    "(\u65e7\u4f1a\u8bdd\u62d2\u7edd + \u65b0 USER 403) \u8986\u76d6\u5e76\u590d\u8dd1\u901a\u8fc7\u3002"
    "\u58f3\u5c42\u300c\u975e\u7ba1\u7406\u5458\u4e0d\u53ef\u89c1\u76ee\u5f55\u7ef4\u62a4\u300d\u4ee5 "
    "useCanWrite/canMaintainCatalog \u5355\u6d4b\u4e3a\u8bc1\u636e\uff1b"
    "WorkbenchLayout UI E2E \u4e0d\u5728\u672c\u4efb\u52a1 writeSet/testScope \u53ef\u6267\u884c E2E \u8303\u56f4\u5185 "
    "-> SKIPPED\uff08\u4e0d\u4f2a\u9020 PASS\uff09\u3002"
)

md = f"""# {title}

```yaml
evidenceId: EVID-TASK-WSC-102-1
taskId: TASK-WSC-102
planId: PLAN-WSC-2.2
actorInstance: tester-wsc-102
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/tasks/TASK-WSC-102.md
  - planning/tasks/DEV-TASK-WSC-102.md
  - planning/tasks/REV-TASK-WSC-102.md
  - planning/approved/PLAN-WSC-2.2.md
reviewDecision: APPROVE
executedAt: {now}
status: PASSED
```

## {concl}

{summary_body}

- \u6a21\u5757\uff1abackend/app/data-chain-service (com.shdata.datachain)
- \u5ba1\u67e5\uff1aREV-TASK-WSC-102 APPROVE, P0/P1=0
- \u672c\u673a 3306/6379 \u7aef\u53e3\u63a2\u9488\u4ec5\u4fe1\u606f\u8bb0\u5f55\uff0c\u4e0d\u8ba1\u5165 PASSED\uff1b\u96c6\u6210\u6d4b H2+\u6392\u9664 Redis\uff0c\u65e0\u9700 live MySQL/Redis
- \u672a\u4fee\u6539\u4e1a\u52a1\u4ee3\u7801\u6216\u5ba1\u67e5\u62a5\u544a\uff1b\u672a\u81ea\u884c\u5ba3\u79f0 VERIFIED

\u8bc1\u636e\u76ee\u5f55\uff1a`ai/runs/RUN-WSC-002/tester-wsc-102/`

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| backend-rbac-unit | mvn -f backend/pom.xml -Dtest=RbacMatrixTest,SessionAuthIntegrationTest test | PASSED | Tests run: 6 \u2014 04-mvn-rbac-session.txt, 04-mvn-exit.txt (exitCode=0) |
| backend-session-integration | same run / SessionAuthIntegrationTest | PASSED | Tests run: 6; total 12 / BUILD SUCCESS \u2014 04-mvn-rbac-session.txt, 03-session-test-methods.txt |
| session-denial-proof | logout_thenImportWriteApiRejected + roleSwitch_oldSessionImportRejected | PASSED | logout->401; roleSwitch old reject / new USER 403 \u2014 03-session-test-methods.txt, 03-session-denial-log.txt (MockMvc+H2) |
| frontend-useCanWrite | pnpm exec vitest run src/features/auth/composables/useCanWrite.spec.ts | PASSED | 3 tests \u2014 05-vitest-useCanWrite.txt, 05-vitest-exit.txt |
| frontend-typecheck | pnpm typecheck | PASSED | exitCode=0 \u2014 05-typecheck.txt |
| frontend-build | pnpm build | PASSED | exitCode=0 \u2014 05-frontend-build.txt |
| shell-catalog-maintenance-visibility | unit canMaintainCatalog + static WorkbenchLayout v-if | PASSED (unit) | 05-vitest-cases.txt, 06-shell-visibility-static.txt |
| shell-ui-e2e | (none in scope) | SKIPPED | no UI E2E in writeSet/testScope \u2014 06-e2e-probe.txt; not faked PASS |
| env-mysql-redis-probe | local port probe (info) | NOT SCORED | 00-ports-probe.txt; not marked PASSED |

## testScope \u5bf9\u7167

| # | \u8981\u6c42 | \u7ed3\u679c |
|---|---|---|
| 1 | BE unit/integration \u00a74 matrix + session | PASSED (12/12) |
| 2 | FE useCanWrite vitest + typecheck/build | PASSED |
| 3 | logout/role-change write API denial | PASSED (integration re-run) |
| 4 | non-admin hide catalog maintenance | PASSED (unit); UI E2E SKIPPED |

## \u8bc1\u636e\u7ea2\u7ebf

- env/port probe not marked PASSED
- no self VERIFIED claim (TESTRUN only)
- no business code or REV edits
- no passwords / application-local.yml printed
- actorInstance=tester-wsc-102 (differs from developer-wsc-102 / code-reviewer-wsc-102)
"""

out.write_text(md, encoding="utf-8")
(ev / "summary.json").write_text(
    json.dumps(
        {
            "evidenceId": "EVID-TASK-WSC-102-1",
            "taskId": "TASK-WSC-102",
            "actorInstance": "tester-wsc-102",
            "status": "PASSED",
            "reviewDecision": "APPROVE",
            "contracts": "wsc-contracts@2.0.0",
            "backendTests": "12/12 PASSED",
            "frontendVitest": "3/3 PASSED",
            "sessionDenial": "SessionAuthIntegrationTest logout+roleSwitch PASSED",
            "shellE2E": "SKIPPED",
            "executedAt": now,
            "testrunPath": "planning/tasks/TESTRUN-TASK-WSC-102.md",
        },
        ensure_ascii=False,
        indent=2,
    ),
    encoding="utf-8",
)
t = out.read_text(encoding="utf-8")
assert "\u6d4b\u8bd5\u8bc1\u636e" in t and "status: PASSED" in t
print("OK", len(t), out)
