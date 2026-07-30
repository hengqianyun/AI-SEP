# PRD

填写模板：[`PRD-TEMPLATE.md`](./PRD-TEMPLATE.md)。

| 文档 | 说明 |
|---|---|
| [`v1.0.md`](./v1.0.md) | **AI-SEP 平台本体** PRD（多 Agent 交付流水线 / 项目模板） |
| [`wsc-v1.0.md`](./wsc-v1.0.md) | WSC 接入端工作台 V1.0（已关联 SNAP-WSC-001） |
| [`wsc-v1.1.md`](./wsc-v1.1.md) | WSC V1.1（DRAFT；原型见 [`design/prototypes/wsc-v1.1/`](../../design/prototypes/wsc-v1.1/)） |

## 版本约定

- **按文件名版本化**，不按 `prd/v1.0/` 分子目录：`wsc-v1.0.md`、`wsc-v1.1.md`。
- 新版本 **新建文件**，保留旧版；规划/快照用完整路径引用（`sourcePrd`）。
- 可交互原型放 `design/prototypes/<产品>-<版本>/`，PRD 内链接引用；源稿可暂存 `temp/product/prd/`，定稿后晋升到本目录。
- 需求变更须新建 `product/requirements/SNAP-*`，不得静默改写已引用快照。
- **明确新的 PRD 版本时，必须起草新 Run**（`ai/runs/RUN-{PROJECT}-{NNN}/`），见 [`ai/workflow/start.md`](../../ai/workflow/start.md)；禁止在已完成 Run 上续跑新版本。

启动流程前在 [`PROJECT-CUSTOMIZATION.md`](../../PROJECT-CUSTOMIZATION.md) §7 勾选。
