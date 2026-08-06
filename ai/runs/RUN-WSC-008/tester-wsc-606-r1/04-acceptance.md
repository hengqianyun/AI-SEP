# TASK-WSC-606 acceptance 对照（tester-wsc-606-r1）

| # | acceptance | 结果 | 证据 |
|---|---|---|---|
| 1 | 数据目录可滚动；触底分页合理 | **PASS**（源码交付 + Vitest CatalogBrowsePage；E2E 场景 3/6 目录可达） | `01-vitest.txt`；E2E #3/#6 |
| 2a | 用户创建改为弹框 | **PASS** | E2E `2a`；Vitest UsersAdmin |
| 2b | 操作栏「编辑角色」按钮 | **PASS** | E2E `2a`（`editUserRoleViaDialog`） |
| 2c | 白色卡片 / wsc-surface | **PASS** | Vitest UsersAdmin 壳层断言 |
| 2d | 去除 sys_user 副标题 | **PASS** | Vitest UsersAdmin |
| 3 | 目录维护去除「← 返回目录」 | **PASS**（源码交付；本轮未单独挂维护页 E2E） | DEV + 源码只读对照（未改实现） |
| 4 | 行业分类 = INDUSTRY_CATEGORY_OPTIONS；筛选 industryCategory | **PASS** | Vitest useCatalogBrowse；`02b` industryCategory 集成测 exit 0 |

## 备注

- 全类 `CatalogBrowseIntegrationTest` 8 失败见 `BUG-WSC-606-TESTRUN-001`（P2，不阻塞本门禁）。
- 正式 E2E：`TESTRUN-WSC-E2E-V14` / `reports/p0-wsc-v1.4/` → **15/0**。
