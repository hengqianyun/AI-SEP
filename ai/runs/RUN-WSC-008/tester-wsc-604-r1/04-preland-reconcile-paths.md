# 预落地对账自动化路径（TASK-WSC-604 行 P0）

对齐 PLAN §0.2 **604** 行 + testScope「预落地对账自动化」：须有命名测试；交付说明附证据定位。

| P0 行为（§0.2 604） | 命名测试类 / spec | 方法 / 用例 | 本轮 |
|---|---|---|---|
| 拆入 604 独占 L2 类文件（不在 Browse Controller 残留） | `L2DistributionIntegrationTest.java` | `prelandReconcile_604_l2EndpointOwnedByL2DistributionController_notBrowse` | PASS（#1） |
| Top5 / 比例 / hover / leave / 真实总数（UI 契约） | `CirculationSeatMap.spec.ts` | `Top5 truncate…`；`hover dims…`；`empty + loading…` | PASS（#2） |
| 仅公共目录挂载 | `CirculationSeatMap.spec.ts` | `mount contract: only catalog showSeatMap=true; mine false` | PASS（#2） |
| 座序图目录内 card 层次（§1.5 UX-003） | `CirculationSeatMap.spec.ts` | `prelandReconcile_604: directory card layer + seat-map root testid` | PASS（#2） |
| 后端真实总数 + 降序 + 空目录 | `L2DistributionIntegrationTest` + `L2DistributionServiceTest` | `seededProducts_…`；`emptyCatalog_…`；Service 3 例 | PASS（#1） |

全部命名路径存在且本轮 `01-backend` / `02-vitest` exitCode=0。
