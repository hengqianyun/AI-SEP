# 任务包 TASK-WSC-501（HOTFIX）

```yaml
taskId: TASK-WSC-501
runId: RUN-WSC-007
status: VERIFIED
wave: HOTFIX
kind: HOTFIX
goal: |
  产品「行业分类」改为 GB/T 4754 门类产品字段（非树节点）；
  新增/编辑/导入枚举对齐 v0729 模板下拉；弱化 L3 挂载必填；修复模板样例行导入失败。
reqs: [REQ-CAT-004, REQ-CAT-005, REQ-CAT-008]
dependsOn: []
blocksRelease: true
actorInstance: developer-wsc-501
decision: DEC-WSC-006
```

## PO 决策（权威）

- **行业分类并非节点，只是产品字段。**
- **枚举字段参考导入模板对应列的下拉选项。**
- 目录挂载方案：**A** — 编辑/导入只保留 GB/T「行业分类」；挂载树弱化。

证据：`import-error-report-rep-9385ecd8008c.csv`（建筑业等 → `ERR_CATEGORY_LEAF_REQUIRED`）。

## 权威枚举（v0729 Sheet1 dataValidation）

| 列 | 字段 | 选项（中文原文） |
|---|---|---|
| B | 产品类型 | 数据接口,数据集,数据报告,其他数据产品 |
| C | 行业分类 | 农、林、牧、渔业;采矿业;制造业;电力、热力、燃气及水生产和供应业;建筑业;批发和零售业;交通运输、仓储和邮政业;住宿和餐饮业;信息传输、软件和信息技术服务业;金融业;房地产业;租赁和商务服务业;科学研究和技术服务业;水利、环境和公共设施管理业;居民服务、修理和其他服务业;教育;卫生和社会工作;文化、体育和娱乐业;公共管理、社会保障和社会组织;国际组织 |
| E | 交付方式 | API,文件传输,数据沙箱,隐私保护计算 |
| G | 地域范围 | 国际,全国,省级,市级,区县级,区县级以下 |
| H | 更新频率 | 实时,每日,每周,每月,每年,按需,不更新 |
| M | 数据形态 | 图片,文本,视频,音频,表格,其他 |

模板副本：`product/assets/import/product-import-template-v0729.xlsx`

## 验收

1. 导入样例行（建筑业 / 交通运输、仓储和邮政业 / 信息传输、软件和信息技术服务业）**不再**因 `ERR_CATEGORY_LEAF_REQUIRED` 失败；非法门类 → 行级失败并提示枚举。
2. `ProductWrite`/详情可读 `industryCategory`；创建/编辑必填该字段；**不再强制**空间/行业/子类三级级联。
3. 编辑页上述枚举下拉选项与模板一致（展示中文；存盘可用既有英文码映射处保持兼容，但行业分类存中文门类原文）。
4. 无 `l3CategoryId` 的产品在目录浏览仍可见（未分类/按行业分类展示，择一并在 DEV 说明）。
5. 相关单测/集成测/Vitest 更新并通过；落盘 `DEV-TASK-WSC-501.md`。

## writeSet

- `contracts/**`（OpenAPI/`VERSION` 等与本语义相关的最小变更）
- `design/decisions/DEC-WSC-006.md`（已有则可引用；勿改无关 DEC）
- `backend/app/data-chain-service/**`（product import/editor/browse 模型与校验）
- `frontend/src/features/catalog/{editor,detail,browse,import}/**`；必要时 `frontend/src/api/**`
- `tests/fixtures/import/**`（category-mismatch 等语义调整）
- `planning/tasks/DEV-TASK-WSC-501.md`

## denyModify

- `ai/runs/**/state.yaml`、`events.jsonl`（编排独占）
- `planning/approved/**`（除非契约升版强制同步且任务需要）
- 无关 portal / UX 布局大改

## 派发

- 2026-08-03T11:39:00+08:00 orchestrator → developer-wsc-501
