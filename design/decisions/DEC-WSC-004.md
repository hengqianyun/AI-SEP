---
decisionId: DEC-WSC-004
title: 后端运行时改为 Java 17 LTS
status: APPROVED
date: 2026-07-28
owner: techLead
source: human-confirmed-2026-07-28（用户要求使用 JDK 17）
closes: []
affects: [RULE-ORG-STACK, TASK-WSC-001, PLAN-WSC-1.1]
snapshotRefs: [SNAP-WSC-001]
supersedes: []
---

# DEC-WSC-004 后端运行时改为 Java 17 LTS

## 背景

试点默认栈原定为 Java 21；本机与交付环境统一为 JDK 17，TASK-WSC-001 后端 compile 曾因此 BLOCKED。

## 决策

- 组织/项目后端基线改为 **Java 17 LTS**。
- `backend/pom.xml` 的 `java.version`、`RULE-ORG-STACK`、相关 Skill 与批准计划中的栈表述同步为 17。
- ~~仍使用 Spring Boot 3.3.x（官方支持 Java 17）。~~ **已由 [DEC-WSC-005](./DEC-WSC-005.md) 取代**：后端框架改为 Spring Boot 2.7.18；本 DEC 仅保留 **Java 17 LTS** 约束。

## 未选择方案

| 方案 | 未采用原因 |
|---|---|
| 继续要求 JDK 21 | 与当前交付环境不一致，阻塞 VERIFIED |

## 影响

- 后续任务与 CI 必须以 JDK 17 验证后端。
- 重评触发：若需启用仅 Java 21+ 的语言特性，须新 DEC。
