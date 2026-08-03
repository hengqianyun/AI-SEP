# 任务包 TASK-WSC-502（HOTFIX）

```yaml
taskId: TASK-WSC-502
runId: RUN-WSC-007
status: VERIFIED
wave: HOTFIX
kind: HOTFIX
goal: |
  无挂载/导入产品统一归入浏览分节「未分类数据」；
  禁止将 industryCategory 作为目录节点或分节标题。
reqs: [REQ-CAT-002, REQ-CAT-008]
dependsOn: [TASK-WSC-501]
blocksRelease: true
actorInstance: developer-wsc-502
decision: DEC-WSC-006
```

## PO 澄清

- 导入的产品应分到 **未分类数据**
- 行业分类不能作为节点（亦不得作为浏览 L3/分节伪节点）

## 验收

1. `groupProductsByL3`（或等价）：无 `l3CategoryId` 的产品全部进入单一分节，标题为 **「未分类数据」**（不得按「建筑业」等门类名拆节）
2. `industryCategory` 仍为产品字段（列表列/详情可展示），不生成 `ic:*` 伪节点 id
3. Vitest 覆盖上述分桶；相关浏览测通过
4. 落盘 `DEV-TASK-WSC-502.md`

## writeSet

- `frontend/src/features/catalog/browse/**`
- 若后端列表分组有对应逻辑则 `backend/.../catalog/browse/**`（最小改动）
- `design/decisions/DEC-WSC-006.md`（补充澄清）
- `planning/tasks/DEV-TASK-WSC-502.md`

## denyModify

- `contracts/**`（除非必须）
- `ai/runs/**/state.yaml`、`events.jsonl`
- 无关 editor/import 大改（除非浏览共享工具函数）

## 派发

- 2026-08-03T13:00:00+08:00 PO：导入应进未分类数据；行业分类不作节点
