# §6.1 七场景对照（tester-wsc-605-r1）

依据：`planning/approved/PLAN-WSC-5.2.md` §6.1；规格 `tests/e2e/specs/p0-wsc-v1.4.spec.ts`；JUnit `tests/e2e/reports/p0-wsc-v1.4/junit.xml`（15/0）。

| # | 场景要点 | 用例 | 结果 |
|---|---|---|---|
| 1 | 真登录三角色；角色绑定；无自由切角色；只读角色展示 | `1)×3` admin/provider/user | **PASS** |
| 2 | ADMIN 用户管理 CRUD；PROVIDER/USER 不可达 | `2a`；`2b)×2` | **PASS** |
| 3 | 公共目录三角色无增改导 | `3)×3` | **PASS** |
| 4 | PROVIDER 我的产品增改导；列表本人；无座序图；返回仍在我的产品 | `4)`；`4x` | **PASS** |
| 5 | ADMIN 产品写 403；分类/维护仍可用 | `5)` | **PASS** |
| 6 | 公共目录座序图 Top5+总数+hover；我的产品无图 | `6)` | **PASS** |
| 7 | V1.3 回归：导入四态 + OpenAPI 编辑不回退 | `7a`；`7b` | **PASS** |

备注（非 FAIL）：REV FIND-WSC-605-R1-001（P2）指出场景 4 字面「改/导」部分举证跨 `7b`/`7a`；本轮门禁按计划七场景强制用例全部 ok，未开 P0。
