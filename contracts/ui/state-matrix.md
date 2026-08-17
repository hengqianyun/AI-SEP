# UI 状态矩阵引用（wsc-contracts@2.3.3）

> 权威来源：PLAN-WSC-8.3 §3.1 + SNAP-WSC-008 V1.6 导航/RBAC。

| 页面/操作 | loading | empty | error | success |
|---|---|---|---|---|
| 总览指标/趋势/流/分布 | 骨架或 spinner | 无数据占位文案 | 接口失败可重试提示 | — |
| 公共数据目录列表/筛选 | 列表 loading | 无结果明确空态文案 | 筛选失败提示 | —（只读；无增改导） |
| 座序图（仅公共目录） | 与列表可并存 loading | totalProducts=0 可读空态 | 加载失败可读，不崩页 | hover 熄灭 / leave 恢复 |
| 我的数据产品列表 | 列表 loading | 无本企业产品空态 | 加载失败提示 | — |
| 我的目录（/my-catalog） | 列表 loading | 无条目/筛选空态 | 加载失败 | 单条/批量保存成功反馈 |
| 产品提交（新增/编辑；宿主=我的产品） | 提交中禁用按钮 | — | 校验/业务错误码映射提示 | 成功反馈并回到 `/my-products` |
| 分类维护保存 | 保存中 | — | 失败/禁止删除原因 | 成功反馈；索引刷新 |
| 目录维护列表（全量 /catalog/maintenance） | 列表 loading | 无条目/筛选空态 | 加载失败 | 单条/批量保存成功反馈 |
| 批量导入（提交中；宿主=我的产品） | 上传/解析中（提交控件禁用） | — | 见导入四态 | 见导入四态；关闭/成功回 `/my-products` |
| 用户管理（仅 ADMIN） | 列表 loading | 无用户空态 | 加载/写失败提示 | 创建/改角色/软删成功反馈 |
| 上链列表/快照 | 加载中 | 无记录空态 | 加载失败 | — |
| 壳层角色展示 | — | — | — | **只读标签**（无下拉/无 listbox） |
| 壳层企业信息（P1） | — | — | — | 只读展示 enterpriseName/标识 |
| 壳层未开放菜单 | — | — | — | 明确「本版本未开放」提示 |

## 侧栏可见性（V1.6 / SNAP-WSC-008）

| 菜单 | ADMIN | PROVIDER | USER |
|---|---|---|---|
| 数据目录（公共，只读 + 座序图） | 可见 | 可见 | 可见 |
| 我的目录（`/my-catalog`；scope=myCatalog） | 可见 | 可见 | 隐藏 |
| 我的数据产品（写入口：增改导；本企业） | 可见 | 可见 | 隐藏 |
| 目录维护（`/catalog/maintenance`；scope=full 全量） | 可见 | **隐藏** | 隐藏 |
| 分类维护 | 可见 | 隐藏 | 隐藏 |
| 用户管理 | 可见 | 隐藏 | 隐藏 |
| 角色区 | 只读标签 | 只读标签 | 只读标签 |
| 企业信息区（P1） | 只读 | 只读 | 只读 |

> ADMIN **双入口**：目录维护（全量）与我的目录（本企业）路由/active/页头可区分。PROVIDER **无**目录维护菜单与深链。

## 导入结果四态（继承 V1.4/V1.5；宿主=我的产品）

| 态 | 判定 | UI 最低要求 |
|---|---|---|
| ① 全成功 | `successCount>0` 且 `failureCount=0` | 可关闭/可重传；无报告亦可 |
| ② 部分成功 | `successCount>0` 且 `failureCount>0` | 同时可观测两计数 + 错误报告入口 + 可关闭/重传 |
| ③ 全失败（行级） | `successCount=0` 且 `failureCount>0` | 失败计数 + 报告 + 可关闭/重传 |
| ④ 文件级拒绝 | 请求级错误（超限/非法格式/**旧模板或非 v0729 表头**） | 停留上传步；映射 `ERR_IMPORT_FILE_TOO_LARGE` / `ERR_IMPORT_FORMAT_INVALID` / **`ERR_IMPORT_TEMPLATE_UNSUPPORTED`**；不伪装成行级结果态 |

## 实现约束

- 异步页必须提供 loading / empty / error / success（按上表适用列）。
- **导入 UI 宿主：我的数据产品**（非公共目录）；关闭或成功后回到 `/my-products`。
- **公共数据目录**：三角色只读浏览 + 座序图；结构不可达增改导。
- **角色展示**：壳层 footer **唯一**只读角色标签；禁止可切换下拉 / `aria-haspopup=listbox`。
- **企业 scope**：mine/myCatalog/写/维护 **仅**信 SessionPrincipal；前端隐藏 ≠ 授权。
- `ERR_IMPORT_TEMPLATE_UNSUPPORTED` 仅用于可解析但列名行集合 ≠ v0729 权威列名行的整单拒绝；不得与 `ERR_IMPORT_FORMAT_INVALID` 互换。
- 报告白名单：`rowNumber, productCode, reasonCode, reasonMessage`（不扩大 PII）。
