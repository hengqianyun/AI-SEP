# 测试证据

`yaml
evidenceId: EVID-TASK-WSC-105-1
taskId: TASK-WSC-105
planId: PLAN-WSC-2.2
actorInstance: tester-wsc-105
contracts: wsc-contracts@2.0.0
basedOn:
  - planning/tasks/TASK-WSC-105.md
  - planning/tasks/DEV-TASK-WSC-105.md
  - planning/tasks/REV-TASK-WSC-105.md
  - planning/approved/PLAN-WSC-2.2.md
reviewDecision: APPROVE
executedAt: 2026-07-31T17:51:39+08:00
status: PASSED
`

## 结论摘要

**PASSED** — 独立 tester-wsc-105 按 testScope 复跑：后端 ProductEditorIntegrationTest(5) + ChainApiIntegrationTest(6) 共 11 测 BUILD SUCCESS（exitCode=0）；前端 vitest editor/detail/chain 共 9 测 + typecheck 退出码 0。覆盖：编码冲突/格式（ERR_PRODUCT_CODE_CONFLICT / ERR_PRODUCT_CODE_FORMAT）；版本递增与旧快照可读（v1→v2）；适配失败无半成品（ttestationFailure_noHalfProduct_noOrphanChain / ERR_ATTESTATION）；快照按 versionId 含三级 categoryPath + categoryPathParts；§4 普通用户写禁（ordinaryUser_productWriteForbidden 403 ERR_FORBIDDEN）；缺叶 ERR_CATEGORY_LEAF_REQUIRED；FE L3/OTHER/级联/三级路径/路径格式化。UI E2E 不在可执行范围 → SKIPPED（不伪造 PASS）。并行 TASK-WSC-106 maintenance 未计入本任务失败面。

- 模块：backend/app/data-chain-service (catalog.editor / chain)；frontend features catalog/{detail,editor} + chain
- 审查：REV-TASK-WSC-105 APPROVE, P0/P1=0
- 集成测 H2 MockMvc；未将 env/live DB 探针记为 PASSED
- 未修改业务代码或审查报告；未自行宣称 VERIFIED

证据目录：i/runs/RUN-WSC-002/tester-wsc-105/

## Layers

| name | command | result | notes / evidence |
| --- | --- | --- | --- |
| backend-product-editor-integration | mvn -f backend/pom.xml -Dtest=ProductEditorIntegrationTest,ChainApiIntegrationTest test | PASSED | Tests run: 5; EXIT=0 → 04-mvn-rbac-editor-chain.txt, 04-mvn-exit.txt, 03-backend-test-methods.txt |
| backend-chain-api-integration | same run / ChainApiIntegrationTest | PASSED | Tests run: 6; total 11 BUILD SUCCESS → 04-mvn-summary.txt |
| code-conflict-format | update_incrementsVersion_oldReadable_conflictAndFormat | PASSED | ERR_PRODUCT_CODE_FORMAT + ERR_PRODUCT_CODE_CONFLICT |
| version-increment | create v1 + update v2 + old snapshot | PASSED | latestVersionNo 1→2; old v1 snapshot readable |
| attestation-no-half | attestationFailure_noHalfProduct_noOrphanChain | PASSED | ERR_ATTESTATION; no half product |
| snapshot-by-versionId | getSnapshot_byVersionId + editor snapshot asserts | PASSED | categoryPath + categoryPathParts L1/L2/L3 |
| section4-product-write | ordinaryUser_productWriteForbidden | PASSED | 403 ERR_FORBIDDEN |
| leaf-required | create_rejectsNonL3_andL2Only | PASSED | ERR_CATEGORY_LEAF_REQUIRED |
| frontend-vitest-editor-detail-chain | pnpm exec vitest run useProductEditor/Detail + useChainPage specs | PASSED | 9 tests / 3 files → 05-vitest-editor-detail-chain.txt, 05-vitest-exit.txt |
| frontend-typecheck | pnpm typecheck | PASSED | exitCode=0 → 05-typecheck.txt |
| ui-e2e-product-editor-chain | (none in scope) | SKIPPED | 06-e2e-probe.txt; not faked PASS |

## testScope 对照

| # | 要求 | 结果 |
|---|---|---|
| 1 | 编码冲突 | PASSED — ERR_PRODUCT_CODE_CONFLICT / FORMAT in update_incrementsVersion_* |
| 2 | 版本递增 | PASSED — create v1 + update v2; old snapshot by versionId |
| 3 | 适配失败无半成品 | PASSED — attestationFailure_noHalfProduct_noOrphanChain |
| 4 | 快照按 versionId | PASSED — ChainApiIntegrationTest#getSnapshot_byVersionId (+ editor chain GETs) |
| 5 | §4 产品写矩阵回归（普通用户无产品写） | PASSED — ordinaryUser_productWriteForbidden 403 |
| 6 | FE vitest + typecheck | PASSED — 9/9 + typecheck EXIT=0 |
| 7 | UI E2E | SKIPPED |

## 证据红线

- env/live DB not marked PASSED
- no self VERIFIED claim (TESTRUN only)
- no business code or REV edits
- no Run state.yaml update
- actorInstance=tester-wsc-105 (differs from developer-wsc-105 / code-reviewer-wsc-105)
- parallel TASK-WSC-106 maintenance changes excluded from this failure surface
