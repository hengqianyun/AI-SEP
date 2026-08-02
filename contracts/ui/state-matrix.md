# UI 状态矩阵引用（wsc-contracts@2.1.0）

> 权威来源：PLAN-WSC-1.1 §3.5 + PLAN-WSC-2.2 §3.6 + PLAN-WSC-4.1 §3.4（导入四态）。

| 页面/操作 | loading | empty | error | success |
|---|---|---|---|---|
| 总览指标/趋势/流/分布 | 骨架或 spinner | 无数据占位文案 | 接口失败可重试提示 | — |
| 目录列表/筛选 | 列表 loading | 无结果明确空态文案 | 筛选失败提示 | — |
| 产品提交（新增/编辑） | 提交中禁用按钮 | — | 校验/业务错误码映射提示 | 成功反馈并进入目录/详情 |
| 分类维护保存 | 保存中 | — | 失败/禁止删除原因 | 成功反馈；索引刷新 |
| 目录维护列表 | 列表 loading | 无条目/筛选空态 | 加载失败 | 单条/批量保存成功反馈 |
| 批量导入（提交中） | 上传/解析中（提交控件禁用） | — | 见导入四态 | 见导入四态 |
| 上链列表/快照 | 加载中 | 无记录空态 | 加载失败 | — |
| 壳层未开放菜单 | — | — | — | 明确「本版本未开放」提示 |

## 导入结果四态（PLAN-WSC-2.2 §3.6 / PLAN-WSC-4.1 §3.4）

| 态 | 判定 | UI 最低要求 |
|---|---|---|
| ① 全成功 | `successCount>0` 且 `failureCount=0` | 可关闭/可重传；无报告亦可 |
| ② 部分成功 | `successCount>0` 且 `failureCount>0` | 同时可观测两计数 + 错误报告入口 + 可关闭/重传 |
| ③ 全失败（行级） | `successCount=0` 且 `failureCount>0` | 失败计数 + 报告 + 可关闭/重传 |
| ④ 文件级拒绝 | 请求级错误（超限/非法格式/**旧模板或非 v0729 表头**） | 停留上传步；映射 `ERR_IMPORT_FILE_TOO_LARGE` / `ERR_IMPORT_FORMAT_INVALID` / **`ERR_IMPORT_TEMPLATE_UNSUPPORTED`**；不伪装成行级结果态 |

## 实现约束

- 异步页必须提供 loading / empty / error / success（按上表适用列）。
- 导入 UI 载体：目录页弹窗（`#import-modal`）；关闭后回到目录浏览表面。
- `ERR_IMPORT_TEMPLATE_UNSUPPORTED` 仅用于可解析但列名行集合 ≠ v0729 权威列名行的整单拒绝；不得与 `ERR_IMPORT_FORMAT_INVALID` 互换。
