---
id: RULE-GLOBAL-FRONTEND
version: 1.0.0
status: active
owner: platform
override: allowed-with-adr
appliesTo:
  roles: ["developer", "codeReviewer", "tester", "uxUiPlanner", "planEditor"]
  paths: ["frontend/**"]
reviewBy: 2027-08-19
rollbackTo: none
source:
  productInput: temp/product/prd/wsc-v1.7-transaction-orders-draft.md
---

# 前端交互底线

适用于工作台及同类 Vue 前端。与具体业务页无关；新列表、新写接口默认遵守。例外须 ADR，并在任务包中写明。

## 必须

### 1. 写接口成功须 toast

新增、修改、删除（含状态变更类写操作，如取消、启用、停用）的接口调用**成功**后，必须给出 toast（或同等的短时成功提示）。

- 失败走既有错误提示，不得出现成功 toast
- 只读查询成功不必 toast

### 2. 重要操作须二次确认

删除、禁用/停用、取消等不易撤销的操作，在发出请求前必须二次确认。用户取消确认则不发请求、不改数据。

### 3. 分页须支持跳页与更换每页条数

凡使用分页的列表：

- 支持页码输入跳转（quick jumper）
- 支持更换 `pageSize`
- 默认档位 **10 / 20 / 50 / 100**（须落在服务端允许范围内）；缺省前端 **10**
- 更换 `pageSize` 后回到第 **1** 页并重新拉取；仅翻页不改变 `pageSize`
- 须兼容组件库同一次操作可能连续触发「改 size」与「翻页」事件，最终请求仍为新 size 且 `page=1`

## 禁止

- 写成功既不提示、也不说明结果
- 删除 / 禁用 / 取消仅靠一次点击即提交
- 分页只有「上一页 / 下一页」、无法跳页或无法改每页条数
- 改 `pageSize` 后停留在可能越界或未刷新的旧页码
