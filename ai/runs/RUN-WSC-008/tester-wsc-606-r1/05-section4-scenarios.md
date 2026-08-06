# §6.1 / V1.4 场景勾选（本轮 E2E）

evidenceId: TESTRUN-WSC-E2E-V14  
command: `pnpm run test:p0-v14`（exitCode=0；15 passed）

| # | 场景 | 结果 | 用例 |
|---|---|---|---|
| 1 | 真登录三角色；角色绑定；只读角色 | **PASS** | `1)×3` |
| 2 | ADMIN 用户管理 CRUD（弹框创建 / 编辑角色）；PROVIDER/USER 不可达 | **PASS** | `2a`；`2b)×2` |
| 3 | 公共目录三角色无增改导 | **PASS** | `3)×3` |
| 4 | PROVIDER 我的产品增改导；无座序图；返回 | **PASS** | `4)`；`4x` |
| 5 | ADMIN 产品写 403；分类/维护可达 | **PASS** | `5)` |
| 6 | 公共目录座序图 Top5+总数+hover；我的产品无图 | **PASS** | `6)` |
| 7 | V1.3 回归：导入四态 + OpenAPI 编辑不回退 | **PASS** | `7a`；`7b` |
