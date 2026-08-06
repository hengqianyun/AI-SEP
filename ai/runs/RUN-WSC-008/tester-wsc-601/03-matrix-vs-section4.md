# Matrix × PLAN §4 三角色关键能力交叉检查

> actorInstance: tester-wsc-601  
> sources: `contracts/rbac/matrix.yaml` (version 2.2.0) × `planning/approved/PLAN-WSC-5.2.md` §4  
> method: 文档核对（非运行时 API）

| 能力（§4） | §4 ADMIN | matrix ADMIN | §4 PROVIDER | matrix PROVIDER | §4 USER | matrix USER | 结果 |
|---|---|---|---|---|---|---|---|
| 公共目录浏览 + 座序图 | 可见 | （matrix 无单独 browse 键；state-matrix 三角色可见公共目录） | 可见 | 同左 | 可见 | 同左 | PASS（state-matrix 侧栏） |
| 分类维护 UI/API | 可见 / 200 | categoryMaintainUI=visible；categoryWriteApi expect 200 | 隐藏 / 403 | hidden / 403 ERR_FORBIDDEN | 隐藏 / 403 | hidden / 403 | PASS |
| 目录维护 UI/API | 可见 / 200 | catalogMaintenanceUI=visible；Api expect 200 | 隐藏 / 403 | hidden / 403 ERR_MAINTENANCE_FORBIDDEN | 隐藏 / 403 | hidden / 403 | PASS |
| 产品增改 UI/API | **隐藏 / 403** | productWriteUI=hidden；Api 403 ERR_FORBIDDEN | 可见 / 200（本人） | visible；Api 200 on ownCreateBy | 隐藏 / 403 | hidden / 403 | PASS |
| 批量导入 UI/API | **隐藏 / 403** | productImportUI=hidden；Api 403 | 可见 / 200 | visible；Api 200 | 隐藏 / 403 | hidden / 403 | PASS |
| 我的数据产品菜单 | 隐藏 | myProductsUI=hidden | 可见 | myProductsUI=visible | 隐藏 | myProductsUI=hidden | PASS |
| 用户管理 UI/API | 可见 / 200 | userManageUI=visible；Api 200 | 隐藏 / 403 | hidden / 403 | 隐藏 / 403 | hidden / 403 | PASS |
| 会话切角色 | **禁用** | sessionRoleSwitch 410 ERR_ROLE_SWITCH_DISABLED | **禁用** | 同左 410 | **禁用** | 同左 410 | PASS |
| 导入报告 GET | 200（规则内） | importReportGet 200 on ownOrAdmin | 200（importer） | 200 on importer | 403 | 403 ERR_FORBIDDEN | PASS |

结论：matrix.yaml 与 PLAN §4 关键能力字面一致；切角色三角色均为 410 + ERR_ROLE_SWITCH_DISABLED。
