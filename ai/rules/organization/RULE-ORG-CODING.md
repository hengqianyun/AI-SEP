---
id: RULE-ORG-CODING
version: 0.1.0
status: draft
owner: architecture-committee
override: allowed-with-adr
appliesTo:
  roles: ["developer", "codeReviewer", "apiDataDesigner"]
reviewBy: TBD
---

# 编码与 API 约定

> 填写请复制同目录 [`CODING-TEMPLATE.md`](./CODING-TEMPLATE.md)。
> **人类必填**。

## API

- 风格：_REST / RPC / GraphQL（择一或说明边界）_
- 错误响应结构：_待填（字段名、错误码体系）_
- 鉴权头 / Token 约定：_待填_
- 分页约定：_待填_

## 代码

- 命名：_待填_
- 分层：_待填（如 controller → service → repo）_
- 日志必填字段：_待填（requestId、userId 是否允许等）_

## 审查时不可因「个人风格」阻塞的事项

- _待填（例如引号风格若已由 formatter 统一）_
