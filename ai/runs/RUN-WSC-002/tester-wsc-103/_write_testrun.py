from pathlib import Path
from datetime import datetime, timezone, timedelta
import json

root = Path(r"C:/WorkSpace/AI-SEP")
ev = root / "ai/runs/RUN-WSC-002/tester-wsc-103"
out = root / "planning/tasks/TESTRUN-TASK-WSC-103.md"
now = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec="seconds")
fence = chr(96) * 3
bq = chr(96)

def code(s):
    return bq + s + bq

lines = []
a = lines.append
a("# " + "\u6d4b\u8bd5\u8bc1\u636e")
a("")
a(fence + "yaml")
a("evidenceId: EVID-TASK-WSC-103-1")
a("taskId: TASK-WSC-103")
a("planId: PLAN-WSC-2.2")
a("actorInstance: tester-wsc-103")
a("contracts: wsc-contracts@2.0.0")
a("basedOn:")
a("  - planning/tasks/TASK-WSC-103.md")
a("  - planning/tasks/DEV-TASK-WSC-103.md")
a("  - planning/tasks/REV-TASK-WSC-103.md")
a("  - planning/approved/PLAN-WSC-2.2.md")
a("reviewDecision: APPROVE")
a("executedAt: " + now)
a("status: PASSED")
a(fence)
a("")
a("## " + "\u7ed3\u8bba\u6458\u8981")
a("")
summary = (
    "**PASSED** \u2014 \u72ec\u7acb tester-wsc-103 \u6309 testScope \u590d\u8dd1\uff1a"
    "\u540e\u7aef CategoryAdminIntegrationTest(3) + CategoryAdminServiceTest(5) \u5171 8 \u6d4b BUILD SUCCESS"
    "\uff08exitCode=0\uff09\uff1b\u524d\u7aef useCategoryAdmin.spec.ts 4 \u6d4b + typecheck \u9000\u51fa\u7801 0\u3002"
    "\u8986\u76d6\uff1a\u4e09\u7ea7 CRUD\uff08"
    + code("crud_threeLevels") + " / " + code("createL3_underL2_and_deleteExtraEmptyL3_ok")
    + " / vitest add L3\uff09\uff1b\u6709\u6302\u8f7d\u7981\u5220\u8d1f\u4f8b\uff08"
    + code("delete_mountedL3_andL2_andL1_forbidden_emptyOk") + " / " + code("deleteMountedL3_forbidden")
    + "\uff0c\u5ba1\u8ba1 ERR_CATEGORY_HAS_PRODUCTS\uff09\uff1b\u5b50\u8282\u70b9\u7981\u5220\uff08"
    + code("deleteL2_withL3Child_forbidden")
    + "\uff09\uff1b\u975e\u7ba1\u7406\u5458 403\uff08"
    + code("nonAdmin_cannotReachCategoryWrite")
    + "\uff09\uff1b\u81f3\u5c11\u4fdd\u7559\u4e00\u4e2a\u4e09\u7ea7\uff08"
    + code("lastL3_globally_forbidden")
    + "\uff0c\u5355\u6d4b mock\uff09\u3002CategoryAdminPage UI E2E \u4e0d\u5728 writeSet "
    "\u53ef\u6267\u884c E2E \u8303\u56f4 \u2192 SKIPPED\uff08\u4e0d\u4f2a\u9020 PASS\uff09\u3002"
)
a(summary)
a("")
a("- \u6a21\u5757\uff1abackend/app/data-chain-service (" + code("com.shdata.datachain.catalog.admin") + ")")
a("- \u5ba1\u67e5\uff1aREV-TASK-WSC-103 APPROVE, P0/P1=0")
a("- \u96c6\u6210\u6d4b H2 MockMvc\uff1b\u672a\u5c06 env/live DB \u63a2\u9488\u8bb0\u4e3a PASSED")
a("- \u672a\u4fee\u6539\u4e1a\u52a1\u4ee3\u7801\u6216\u5ba1\u67e5\u62a5\u544a\uff1b\u672a\u81ea\u884c\u5ba3\u79f0 VERIFIED")
a("")
a("\u8bc1\u636e\u76ee\u5f55\uff1a" + code("ai/runs/RUN-WSC-002/tester-wsc-103/"))
a("")
a("## Layers")
a("")
a("| name | command | result | notes / evidence |")
a("| --- | --- | --- | --- |")
a("| backend-category-admin-integration | mvn -f backend/pom.xml -Dtest=CategoryAdminIntegrationTest,CategoryAdminServiceTest test | PASSED | Tests run: 3; EXIT=0 \u2014 04-mvn-admin.txt, 04-mvn-exit.txt, 03-backend-test-methods.txt |")
a("| backend-category-admin-unit | same run / CategoryAdminServiceTest | PASSED | Tests run: 5; total 8 BUILD SUCCESS \u2014 04-mvn-summary.txt |")
a("| crud-coverage | crud_threeLevels + createL3 + vitest add L3 | PASSED | 03-backend-test-methods.txt, 05-vitest-cases.txt |")
a("| delete-with-products-negative | delete_mountedL3... + deleteMountedL3_forbidden | PASSED | ERR_CATEGORY_HAS_PRODUCTS \u2014 04-mvn-summary.txt |")
a("| children-delete-negative | deleteL2_withL3Child_forbidden | PASSED | 03-backend-test-methods.txt |")
a("| non-admin-403 | nonAdmin_cannotReachCategoryWrite | PASSED | 403 ERR_FORBIDDEN |")
a("| at-least-one-leaf | lastL3_globally_forbidden | PASSED | unit mock \u2014 03-backend-test-methods.txt |")
a("| frontend-useCategoryAdmin | pnpm exec vitest run useCategoryAdmin.spec.ts | PASSED | 4 tests \u2014 05-vitest-useCategoryAdmin.txt |")
a("| frontend-typecheck | pnpm typecheck | PASSED | exitCode=0 \u2014 05-typecheck.txt |")
a("| ui-e2e-category-admin | (none in scope) | SKIPPED | 06-e2e-probe.txt; not faked PASS |")
a("")
a("## testScope \u5bf9\u7167")
a("")
a("| # | \u8981\u6c42 | \u7ed3\u679c |")
a("|---|---|---|")
a("| 1 | CRUD | PASSED \u2014 crud_threeLevels, createL3_underL2_and_deleteExtraEmptyL3_ok, vitest add L3 |")
a("| 2 | \u7981\u6b62\u5220\u9664\u8d1f\u4f8b | PASSED \u2014 mounted L3/L2/L1 + deleteL2_withL3Child_forbidden |")
a("| 3 | \u975e\u7ba1\u7406\u5458 403 | PASSED \u2014 nonAdmin_cannotReachCategoryWrite |")
a("| 4 | \u81f3\u5c11\u4e00\u4e2a\u4e09\u7ea7 | PASSED \u2014 lastL3_globally_forbidden (unit) |")
a("| 5 | FE vitest + typecheck | PASSED \u2014 4/4 + typecheck EXIT=0 |")
a("| 6 | UI E2E | SKIPPED |")
a("")
a("## \u8bc1\u636e\u7ea2\u7ebf")
a("")
a("- env/live DB not marked PASSED")
a("- no self VERIFIED claim (TESTRUN only)")
a("- no business code or REV edits")
a("- actorInstance=tester-wsc-103 (differs from developer-wsc-103 / code-reviewer-wsc-103)")
a("")

out.write_text("\n".join(lines), encoding="utf-8")
payload = {
    "evidenceId": "EVID-TASK-WSC-103-1",
    "taskId": "TASK-WSC-103",
    "actorInstance": "tester-wsc-103",
    "reviewDecision": "APPROVE",
    "status": "PASSED",
    "executedAt": now,
    "backend": {"testsRun": 8, "failures": 0, "exitCode": 0},
    "frontend": {"vitest": {"tests": 4, "passed": 4, "exitCode": 0}, "typecheck": {"exitCode": 0}},
    "uiE2e": "SKIPPED",
    "testrunPath": "planning/tasks/TESTRUN-TASK-WSC-103.md",
    "evidenceDir": "ai/runs/RUN-WSC-002/tester-wsc-103/",
}
(ev / "summary.json").write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
text = out.read_text(encoding="utf-8")
assert fence + "yaml" in text
assert "status: PASSED" in text
assert text.startswith("# \u6d4b\u8bd5\u8bc1\u636e")
print("OK", out, out.stat().st_size)